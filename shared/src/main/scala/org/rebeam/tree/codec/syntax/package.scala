package org.rebeam.tree.codec

import io.circe.Json

package object syntax {
  implicit final class CodecOps[A](val a: A) extends AnyVal {
    def asJsonOption(implicit codec: Codec[A]): Option[Json] = codec.encoder(a)
  }
}
