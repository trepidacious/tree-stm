package org.rebeam.tree

import monocle.{Lens, Optional, Prism}
import org.rebeam.tree.Delta._
import scala.collection.immutable.Seq

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

  /**
    * Produce a new TransactionCursor, operating on some portion of
    * the data at this cursor
    * @param prism  Prism to operate on the data
    * @tparam B     Type of data in new cursor
    * @return       New TransactionCursor
    */
  def refract[B](prism: Prism[A, B]): DeltaCursor[B] = DeltaCursor.Refracted(this, prism)

  /**
    * Produce a new TransactionCursor, operating on some portion of
    * the data at this cursor
    * @param optional Optional to operate on the data
    * @tparam B       Type of data in new cursor
    * @return         New TransactionCursor
    */
  def zoomOptional[B](optional: Optional[A, B]): DeltaCursor[B] = DeltaCursor.OptionalCursor(this, optional)

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
    def transact(delta: Delta[A]): Transaction = parent.transact(LensDelta(lens, delta))
  }

  /**
    * Operates on a view of the data in a parent DeltaCursor, using a Prism
    * @param parent The parent cursor
    * @param prism  The prism on parent's data
    * @tparam P     Parent data type
    * @tparam A     Data type
    */
  case class Refracted[P, A](parent: DeltaCursor[P], prism: Prism[P, A]) extends DeltaCursor[A] {
    def transact(delta: Delta[A]): Transaction = parent.transact(PrismDelta(prism, delta))
  }

  /**
    * Operates on a view of the data in a parent DeltaCursor, using an Optional
    * @param parent   The parent cursor
    * @param optional The optional on parent's data
    * @tparam P       Parent data type
    * @tparam A       Data type
    */
  case class OptionalCursor[P, A](parent: DeltaCursor[P], optional: Optional[P, A]) extends DeltaCursor[A] {
    def transact(delta: Delta[A]): Transaction = parent.transact(OptionalDelta(optional, delta))
  }

  /**
    * Operates on the contents of an Option in a parent DeltaCursor
    * @param parent   The parent cursor
    * @tparam A       Data type
    */
  case class OptionCursor[A](parent: DeltaCursor[Option[A]]) extends DeltaCursor[A] {
    def transact(delta: Delta[A]): Transaction = parent.transact(OptionDelta(delta))
  }

  /**
    * Operates on an indexed element of a Seq in a parent DeltaCursor
    * @param parent   The parent cursor
    * @param index    The index
    * @tparam A       Data type in seq
    */
  case class SeqIndexCursor[A](parent: DeltaCursor[Seq[A]], index: Int) extends DeltaCursor[A] {
    def transact(delta: Delta[A]): Transaction = parent.transact(SeqIndexDelta(index, delta))
  }

  /**
    * Convenience method to zoom into contents of an Option
    * @param cursor Cursor to an Option
    * @tparam A     Type of value in Option
    */
  implicit class CursorAtOption[A](cursor: DeltaCursor[Option[A]]) {
    def zoomSome: DeltaCursor[A] = OptionCursor(cursor)
  }

  /**
    * Convenience method to zoom into contents of a Seq using an index
    * @param cursor Cursor to a Seq
    * @tparam A     Type of value in Seq
    */
  implicit class CursorAtSeq[A](cursor: DeltaCursor[Seq[A]]) {
    def zoomIndex(index: Int): DeltaCursor[A] = SeqIndexCursor(cursor, index)
  }
}
