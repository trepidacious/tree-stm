package org.rebeam.tree.view

import org.rebeam.tree._

object View {

  case class ViewResult[A, S, R](a: A, state: S, r: R, idRevisions: Map[Guid, Guid])

}