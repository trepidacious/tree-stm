package org.rebeam.tree.codec

import io.circe.{Decoder, HCursor}
import org.rebeam.tree.Transaction

trait TransactionDecoder { self =>
  /**
    * Decode the given [[HCursor]] as a [[Transaction]]
    */
  def apply(c: HCursor)(idCodecs: IdCodecs): Decoder.Result[Transaction]

  /**
    * Choose the first succeeding decoder.
    */
  final def or(d: => TransactionDecoder): TransactionDecoder = new TransactionDecoder {
    final def apply(c: HCursor)(idCodecs: IdCodecs): Decoder.Result[Transaction] =
      self(c)(idCodecs) match {
        case r @ Right(_) => r
        case Left(_) => d(c)(idCodecs)
      }
  }
}