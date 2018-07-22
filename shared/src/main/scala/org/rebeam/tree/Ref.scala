package org.rebeam.tree

import io.circe.{Decoder, DecodingFailure, Encoder, Json}
import org.rebeam.tree.Guid.{SessionId, SessionTransactionId, TransactionClock}

import scala.util.Try
import scala.util.matching.Regex

/**
  * A reference to a data item with a known Id.
  * @param id The Id of the data item
  * @tparam A The type of data item
  */
case class Ref[+A](id: Id[A]) extends Identified[A] {
  override def toString: String = Ref.toString(this)
}

object Ref {
  val regex: Regex = "([Rr][Ee][Ff]-[0-9a-fA-F]+-[0-9a-fA-F]+-[0-9a-fA-F]+)".r
  val regexGrouped: Regex = "[Rr][Ee][Ff]-([0-9a-fA-F]+)-([0-9a-fA-F]+)-([0-9a-fA-F]+)".r

  private def hex(x: String): Long = java.lang.Long.parseUnsignedLong(x, 16)

  def fromString[A](s: String): Option[Ref[A]] = s match {
    case regexGrouped(clientId, clientDeltaId, id) =>
      Try {
        Ref[A](Id(Guid(SessionId(hex(clientId)), SessionTransactionId(hex(clientDeltaId)), TransactionClock(hex(id)))))
      }.toOption
    case _ => None
  }

  def toString[A](r: Ref[A]): String = f"ref-${r.id.guid.sessionId.id}%x-${r.id.guid.sessionTransactionId.id}%x-${r.id.guid.transactionClock.id}%x"

  //Encoder and decoder using plain string format for ref
  implicit def decodeRef[A]: Decoder[Ref[A]] = Decoder.instance(
    c => c.as[String].flatMap(string => fromString[A](string).fold[Either[DecodingFailure, Ref[A]]](Left(DecodingFailure("Ref invalid string", c.history)))(Right(_)))
  )
  implicit def encodeRef[A]: Encoder[Ref[A]] = Encoder.instance(
    r => Json.fromString(toString(r))
  )
}






