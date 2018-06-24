package org.rebeam.tree

import cats.Monad

/**
  * A Delta will take a data value, and produce a new data
  * value using STMOps.
  * This is essentially a Transaction that requires a data item
  * to operate on. Converted to a Transaction using a DeltaCursor.
 *
  * @tparam A The data value
  */
trait Delta[A] {
  def apply[F[_]: Monad](a: A)(implicit stm: STMOps[F]): F[A]
}
