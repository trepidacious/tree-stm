package org.rebeam.tree

import cats.Monad
import cats.implicits._

/**
  * A Transaction can produce an effect and a result using STMOps. This represents
  * an atomic operation performed using the STMOps, getting/setting data, etc.
  * Using a wrapper allows for use of different effects, and serialisation.
  */
trait Transaction {

  def apply[F[_]: Monad](implicit stm: STMOps[F]): F[Unit]

  // The following are useful when we have a parametric return type A for the transaction instead of
  // unit, but not serialisable
//  def map[B](f: A => B): Transaction[B] = {
//    val t = this
//    new Transaction[B] {
//      override def apply[F[_] : Monad](implicit stm: STMOps[F]): F[B] = t[F].map(f)
//    }
//  }
//
//  def flatMap[B](f: A => Transaction[B]): Transaction[B] = {
//    val t = this
//    new Transaction[B] {
//      override def apply[F[_] : Monad](implicit stm: STMOps[F]): F[B] = t[F].flatMap(a => f(a)[F])
//    }
//  }

}

object Transaction {

  /**
    * Applies a delta to the data at an Id in STM
    * @param id     The Id
    * @param delta  The delta to apply to data at Id
    * @tparam A     The type of data
    */
  case class DeltaAtId[A](id: Id[A], delta: Delta[A]) extends Transaction {
    def apply[F[_]: Monad](implicit stm: STMOps[F]): F[Unit] =
      stm.modifyF(id, (a: A) => delta[F](a)).map(_ => ())
  }

}