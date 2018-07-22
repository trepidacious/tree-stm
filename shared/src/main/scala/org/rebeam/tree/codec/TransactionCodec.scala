package org.rebeam.tree.codec

import io.circe.Decoder.Result
import io.circe._
import org.rebeam.tree.Transaction.DeltaAtId
import org.rebeam.tree._

import scala.reflect.ClassTag

trait TransactionCodec {
  def encoder: TransactionEncoder
  def decoder: TransactionDecoder

  final def or(c: => TransactionCodec): TransactionCodec = new TransactionCodec {
    override def encoder: TransactionEncoder = encoder or c.encoder

    override val decoder: TransactionDecoder = decoder or c.decoder
  }
}

object TransactionCodec {

  private implicit val decodeIdAny: Decoder[Id[Any]] = Id.decodeId[Any]

  /**
    * A [[TransactionCodec]] for a simple [[Transaction]] that can be encoded and decoded
    * by plain [[Encoder]] and [[Decoder]] instances.
    * @param name     Name of the transaction, used as a type tag
    * @param ct       Classtag to identify transaction when encoding - best not to use generic transactions
    * @param encodeA  Encoder for A
    * @param decodeA  Decoder for A
    * @tparam A       The type of [[Transaction]]
    * @return         A [[TransactionCodec]] for A
    */
  def transactionCodec[A <: Transaction](name: String)(implicit ct: ClassTag[A], encodeA: Encoder[A], decodeA: Decoder[A]): TransactionCodec = new TransactionCodec {
    override def encoder: TransactionEncoder = new TransactionEncoder {
      override def apply(t: Transaction)(idCodecs: IdCodecs): Option[Json] = for {
        a <- ct.unapply(t)
      } yield Json.obj(
        "Transaction" -> Json.obj(
          name -> encodeA(a)
        )
      )
    }

    override def decoder: TransactionDecoder = new TransactionDecoder {
      override def apply(c: HCursor)(idCodecs: IdCodecs): Result[Transaction] =
        c.downField("Transaction").downField(name).as[A](decodeA)
    }
  }

  val deltaAtIdCodec: TransactionCodec = new TransactionCodec {
    override val encoder: TransactionEncoder = new TransactionEncoder {
      override def apply(t: Transaction)(idCodecs: IdCodecs): Option[Json] = {
        t match {
          // Note that we know that the id will retrieve an IdCodec for the correct
          // data type
          case DeltaAtId(id, delta) =>
            for {
              idCodec <- idCodecs.codecFor(id)
              deltaJson <- idCodec.deltaCodec.encoder(delta)
            } yield {
              Json.obj(
                "DeltaAtId" -> Json.obj(
                  "id" -> id.toJson,
                  "delta" -> deltaJson
                )
              )
            }
          case _ => None: Option[Json]
        }
      }
    }

    override val decoder: TransactionDecoder = new TransactionDecoder {
      override def apply(c: HCursor)(idCodecs: IdCodecs): Result[Transaction] = {
        val obj = c.downField("DeltaAtId")
        for {
          id <- obj.downField("id").as[Id[Any]]
          idCodec <- idCodecs.codecFor(id).toRight(DecodingFailure("No IdCodec for id " + id, obj.history))
          delta <- idCodec.deltaCodec.decoder.tryDecode(obj.downField("delta"))
        } yield DeltaAtId(id, delta)
      }
    }
  }

}

