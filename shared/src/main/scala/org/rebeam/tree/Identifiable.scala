package org.rebeam.tree

/**
  * Typeclass for getting an Id from a data item
 *
  * @tparam A Type of the identified item
  */
trait Identifiable[A] {
  /**
    * @return The Guid
    */
  def id(a: A): Id[A]
}
object Identifiable {
  /**
    * Identified data is trivially identifiable
    */
  implicit def identified2Identifiable[A <: Identified[A]]: Identifiable[A] = _.id
}