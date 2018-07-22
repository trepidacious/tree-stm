package org.rebeam.tree.codec

import io.circe.Json
import org.rebeam.tree.Transaction

trait TransactionEncoder { self =>
  /**
    * Convert a value to Some(JSON) if possible, None if not.
    */
  def apply(t: Transaction)(idCodecs: IdCodecs): Option[Json]

  /**
    * Choose the first succeeding encoder.
    */
  final def or(e: TransactionEncoder): TransactionEncoder = new TransactionEncoder {
    override def apply(t: Transaction)(idCodecs: IdCodecs): Option[Json] =
      self(t)(idCodecs).orElse(e(t)(idCodecs))
  }
}
