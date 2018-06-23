package org.rebeam.tree

/**
  * Indicates a data type has an Id
  * @tparam A Type of the identified item
  */
trait Identified[+A] {
  /**
    * @return The id
    */
  def id: Id[A]
}
