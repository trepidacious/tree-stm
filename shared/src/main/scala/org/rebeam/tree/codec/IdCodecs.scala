package org.rebeam.tree.codec

import org.rebeam.tree.Id

trait IdCodecs {
  def codecFor[A](id: Id[A]): Option[IdCodec[A]]
}