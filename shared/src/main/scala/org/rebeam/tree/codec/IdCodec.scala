package org.rebeam.tree.codec

import io.circe.{Decoder, Encoder}
import org.rebeam.tree.codec.Codec.DeltaCodec

/**
  * Represents a type of data referenced by [[org.rebeam.tree.Id]] in an STM
  *
  * @param name   The name of the type, must only be associated with one real type in a given STM.
  */
case class IdType(name: String) extends AnyVal

/**
  * Typeclass covering everything needed to handle a type of data referenced by [[org.rebeam.tree.Id]] in an STM
  * @tparam A     The type of data
  */
trait IdCodec[A] {
  def idType: IdType
  def encoder: Encoder[A]
  def decoder: Decoder[A]
  def deltaCodec: DeltaCodec[A]
}

object IdCodec {
  def apply[A](idType: String)
    (implicit encoder: Encoder[A], decoder: Decoder[A], deltaCodec: DeltaCodec[A]): IdCodec[A] =
    IdCodecBasic(IdType(idType), encoder, decoder, deltaCodec)
}

case class IdCodecBasic[A](idType: IdType, encoder: Encoder[A], decoder: Decoder[A], deltaCodec: DeltaCodec[A]) extends IdCodec[A]
