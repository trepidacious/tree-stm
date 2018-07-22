package org.rebeam.tree.codec

import io.circe.Decoder.Result
import io.circe._
import org.rebeam.tree.Transaction.DeltaAtId
import org.rebeam.tree._


object TransactionCodec {


  trait TransactionEncoder {
    /**
      * Convert a value to Some(JSON) if possible, None if not.
      */
    def apply(t: Transaction)(idCodecs: IdCodecs): Option[Json]
  }

  trait TransactionDecoder {
    /**
      * Decode the given [[HCursor]] as a [[Transaction]]
      */
    def apply(c: HCursor)(idCodecs: IdCodecs): Decoder.Result[Transaction]
  }

  trait TransactionCodec {
    def encoder: TransactionEncoder
    val decoder: TransactionDecoder
  }

  trait IdCodecs {
    def codecFor[A](id: Id[A]): Option[IdCodec[A]]
  }

  private implicit val decodeIdAny: Decoder[Id[Any]] = Id.decodeId[Any]

  val DeltaAtIdCodec: TransactionCodec = new TransactionCodec {
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

