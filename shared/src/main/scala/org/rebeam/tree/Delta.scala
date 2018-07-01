package org.rebeam.tree

import cats.{Monad, Traverse}
import cats.implicits._
import monocle.{Lens, Optional, Prism}

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

  case class ValueDelta[A](newA: A) extends Delta[A] {
    def apply[F[_]: Monad](a: A)(implicit stm: STMOps[F]): F[A] = stm.pure(newA)
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

//  case class SeqIndexDelta[A](index: Int, delta: Delta[A]) extends Delta[Seq[A]] {
//    override def apply[F[_] : Monad](s: Seq[A])(implicit stm: STMOps[F]): F[Seq[A]] =
//      s.lift(index).fold(
//        stm.pure(s)
//      )(
//        delta[F](_).map(a => s.updated(index, a))
//      )
//  }

  case class TraversableIndexDelta[T[_]: Traverse, A](index: Int, delta: Delta[A]) extends Delta[T[A]] {
    override def apply[F[_] : Monad](s: T[A])(implicit stm: STMOps[F]): F[T[A]] =
      s.traverseWithIndexM {
        case (a, i) if i == index => delta[F](a)
        case (a, _) => stm.pure(a)
      }
  }

  def transform[F[_] : Traverse, A](oldList: F[A], modify: A => A, predicate: Int => Boolean): F[A] = {
    oldList.mapWithIndex {
      case (a, i) if predicate(i) => modify(a)
      case (a, i) => a
    }
  }
}