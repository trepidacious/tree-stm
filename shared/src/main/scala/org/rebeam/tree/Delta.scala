package org.rebeam.tree

import cats.Monad
import cats.implicits._
import monocle.{Lens, Prism}

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

object Delta {

  case class ValueDelta[A](a: A) extends Delta[A] {
    def apply[F[_]: Monad](a: A)(implicit stm: STMOps[F]): F[A] = stm.pure(a)
  }

  case class LensDelta[A, B](lens: Lens[A, B], delta: Delta[B]) extends Delta[A] {
    override def apply[F[_] : Monad](a: A)(implicit stm: STMOps[F]): F[A] =
      delta[F](lens.get(a)).map(lens.set(_)(a))
  }

  case class PrismDelta[A, B](prism: Prism[A, B], delta: Delta[B]) extends Delta[A] {
    override def apply[F[_] : Monad](a: A)(implicit stm: STMOps[F]): F[A] = {
      prism.getOption(a).fold(
        stm.pure(a)
      )(
        delta[F](_).map(prism.set(_)(a))
      )
    }
  }

}