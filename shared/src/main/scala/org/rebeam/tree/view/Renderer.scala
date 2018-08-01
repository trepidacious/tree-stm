package org.rebeam.tree.view

import cats.Monad
import org.rebeam.tree.ViewOps

trait Renderer[A, S, R]{
  def apply[F[_]: Monad](a: A, state: S)(implicit stm: ViewOps[F]): F[R]
}