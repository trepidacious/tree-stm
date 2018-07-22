package org.rebeam.tree

import io.circe.{Decoder, DecodingFailure, Encoder, Json}
import org.rebeam.tree.Guid.{SessionId, SessionTransactionId, TransactionClock}

import scala.util.Try
import scala.util.matching.Regex

/**
  * An identifier for an item of data of a known type, using a Guid
  *
  * @param guid The Guid
  * @tparam A Type of the identified item
  */
case class Id[+A](guid: Guid) {
  override def toString: String = Id.toString(this)
  def toJson: Json = Json.fromString(toString)
}

object Id {
  val regex: Regex = "([Ii][Dd]-[0-9a-fA-F]+-[0-9a-fA-F]+-[0-9a-fA-F]+)".r
  val regexGrouped: Regex = "[Ii][Dd]-([0-9a-fA-F]+)-([0-9a-fA-F]+)-([0-9a-fA-F]+)".r

  private def hex(x: String): Long = java.lang.Long.parseUnsignedLong(x, 16)

  def fromString[A](s: String): Option[Id[A]] = s match {
    case regexGrouped(clientId, clientDeltaId, id) =>
      Try {
        Id[A](Guid(SessionId(hex(clientId)), SessionTransactionId(hex(clientDeltaId)), TransactionClock(hex(id))))
      }.toOption
    case _ => None
  }

  def toString[A](r: Id[A]): String = f"id-${r.guid.sessionId.id}%x-${r.guid.sessionTransactionId.id}%x-${r.guid.transactionClock.id}%x"

  //Encoder and decoder using plain string format for id
  implicit def decodeId[A]: Decoder[Id[A]] = Decoder.instance(
    c => c.as[String].flatMap(string => fromString[A](string).fold[Either[DecodingFailure, Id[A]]](Left(DecodingFailure("Ref invalid string", c.history)))(Right(_)))
  )
  implicit def encodeId[A]: Encoder[Id[A]] = Encoder.instance(
    r => Json.fromString(toString(r))
  )
}






