package org.rebeam.tree

import cats.Monad
import cats.implicits._
import monocle.{Lens, Prism}

/**
  * A DeltaCursor provides a location in which to apply a Delta. This allows us to convert
  * a Delta into a Transaction that will apply that Delta at the location.
  * So for example the simplest DeltaCursor is DeltaCursor.AtId, which will transact a Delta
  * by using it to modify the data at a given Id.
  * However we can also provide navigation of a Cursor through the data at an Id, using lenses,
  * etc.
  */
trait DeltaCursor[A] {

  /**
    * Apply to a delta at this cursor to produce a Transaction
    * @param delta  Delta to apply at cursor
    * @return       Transaction carrying out the delta
    */
  def transact(delta: Delta[A]): Transaction

  /**
    * Produce a new TransactionCursor, operating on some portion of
    * the data at this cursor
    * @param lens Lens to operate on the data
    * @tparam B   Type of data in new cursor
    * @return     New TransactionCursor
    */
  def zoom[B](lens: Lens[A, B]): DeltaCursor[B] = DeltaCursor.Zoomed(this, lens)

}

object DeltaCursor {

  /**
    * Operates on data in STM at an Id
    * @param id The Id
    * @tparam A The type of data
    */
  case class AtId[A](id: Id[A]) extends DeltaCursor[A] {
    def transact(delta: Delta[A]): Transaction = Transaction.DeltaAtId(id, delta)
  }

  /**
    * Operates on a part of the data in a parent DeltaCursor, using a Lens
    * @param parent The parent cursor
    * @param lens   The lens on parent's data
    * @tparam P     Parent data type
    * @tparam A     Data type
    */
  case class Zoomed[P, A](parent: DeltaCursor[P], lens: Lens[P, A]) extends DeltaCursor[A] {
    def transact(delta: Delta[A]): Transaction = Transaction.DeltaAtLens(parent, lens, delta)
  }

  /**
    * Operates on a view of the data in a parent DeltaCursor, using a Prism
    * @param parent The parent cursor
    * @param prism  The prism on parent's data
    * @tparam P     Parent data type
    * @tparam A     Data type
    */
  case class Refracted[P, A](parent: DeltaCursor[P], prism: Prism[P, A]) extends DeltaCursor[A] {
    def transact(delta: Delta[A]): Transaction = Transaction.DeltaAtPrism(parent, prism, delta)
  }
}



object Test {
  case class AddRandom(times: Int) extends Delta[Int] {
    def apply[F[_]: Monad](a: Int)(implicit stm: STMOps[F]): F[Int] = {
      import stm._
      for {
        r <- randomInt
      } yield a + r * times
    }
  }


}