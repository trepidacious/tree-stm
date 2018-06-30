package org.rebeam.tree

import cats.Monad
import cats.implicits._
import monocle.{Lens, Optional, Prism}
import scala.collection.immutable.Seq

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

  case class OptionalDelta[A, B](optional: Optional[A, B], delta: Delta[B]) extends Delta[A] {
    override def apply[F[_] : Monad](a: A)(implicit stm: STMOps[F]): F[A] =
      optional.getOption(a).fold(
        stm.pure(a)
      )(
        delta[F](_).map(optional.set(_)(a))
      )
  }

  case class OptionDelta[A](delta: Delta[A]) extends Delta[Option[A]] {
    override def apply[F[_] : Monad](a: Option[A])(implicit stm: STMOps[F]): F[Option[A]] =
      a.fold(
        stm.pure(a)
      )(
        delta[F](_).map(Some(_))
      )
  }

  // Seems like this might work for any Seq type, while preserving that type, but needs CanBuildFrom so leaving it for now
  case class SeqIndexDelta[A, S <: Seq[A]](index: Int, delta: Delta[A])(implicit bf: CanBuildFrom[Seq[A], A, S]) extends Delta[S] {
    override def apply[F[_] : Monad](s: S)(implicit stm: STMOps[F]): F[S] =
      s.lift(index).fold(
        stm.pure(s)
      )(
        delta[F](_).map(a => s.updated[A, S](index, a))
      )
  }

  case class SeqIndexDelta[A](index: Int, delta: Delta[A]) extends Delta[Seq[A]] {
    override def apply[F[_] : Monad](s: Seq[A])(implicit stm: STMOps[F]): F[Seq[A]] =
      s.lift(index).fold(
        stm.pure(s)
      )(
        delta[F](_).map(a => s.updated(index, a))
      )
  }

}