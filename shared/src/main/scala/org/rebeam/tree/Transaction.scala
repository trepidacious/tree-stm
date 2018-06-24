package org.rebeam.tree

import cats.Monad
import cats.implicits._
import monocle.{Lens, Prism}

/**
  * A Transaction can produce an effect using STMOps. This represents
  * an atomic operation performed using the STMOps, getting/setting data, etc.
  * Using a wrapper allows for use of different effects, and serialisation.
  */

trait Transaction {
  def apply[F[_]: Monad](implicit stm: STMOps[F]): F[Unit]
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

  /**
    * Applies a delta to part of the data at a parent cursor, using a Lens
    * @param parent The parent cursor
    * @param lens   The lens to navigate to data for Delta
    * @param delta  The delta to apply to data
    * @tparam P     The type of data in the parent cursor
    * @tparam A     The type of data for the Delta
    */
  case class DeltaAtLens[P, A](parent: DeltaCursor[P], lens: Lens[P, A], delta: Delta[A]) extends Transaction {

    def apply[F[_]: Monad](implicit stm: STMOps[F]): F[Unit] = {

      //Produce a delta of our parent, using the lens to extract `a`, apply the delta,
      //and then using the lens again to produce a delta of our parent.
      val parentDelta = new Delta[P] {
        override def apply[G[_] : Monad](p: P)(implicit stm: STMOps[G]): G[P] = {
          val a: A = lens.get(p)
          val newAF: G[A] = delta[G](a)
          newAF.map((newA: A) => lens.set(newA)(p))
        }
      }

      //Use parent TransactionCursor to transact the parent delta
      parent.transact(parentDelta).apply[F]
    }

  }

  /**
    * Applies a delta to a view of the data at a parent cursor, using a Prism
    * @param parent The parent cursor
    * @param prism  The prism to navigate to data for Delta
    * @param delta  The delta to apply to data
    * @tparam P     The type of data in the parent cursor
    * @tparam A     The type of data for the Delta
    */
  case class DeltaAtPrism[P, A](parent: DeltaCursor[P], prism: Prism[P, A], delta: Delta[A]) extends Transaction {

    def apply[F[_]: Monad](implicit stm: STMOps[F]): F[Unit] = {

      //Produce a delta of our parent, using the prism to extract `a`, apply the delta,
      //and then using the lens again to produce a delta of our parent. If prism does not
      //apply, results in no change to parent.
      val parentDelta = new Delta[P] {
        override def apply[G[_] : Monad](p: P)(implicit stm: STMOps[G]): G[P] = {
          val oa: Option[A] = prism.getOption(p)
          oa.fold(
            stm.pure(p)
          )(
            a => {
              val newAF: G[A] = delta[G](a)
              newAF.map((newA: A) => prism.set(newA)(p))
            }
          )
        }
      }

      //Use parent TransactionCursor to transact the parent delta
      parent.transact(parentDelta).apply[F]
    }

  }

}