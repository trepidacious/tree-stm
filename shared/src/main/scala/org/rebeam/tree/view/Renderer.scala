package org.rebeam.tree.view

import cats.Monad
import org.rebeam.tree.ViewOps

trait Renderer[A, S, R]{
  def apply[F[_]: Monad](a: A, state: S)(implicit stm: ViewOps[F]): F[R]
}

object TextTest {

  case class Person(name: String, age: Int)

  sealed trait TextNode
  object TextNode {
    case class Text(s: String) extends TextNode
    case class All(l: List[TextNode]) extends TextNode
    case class Suspend[A, S](a: A, r: TextRenderer[A, S]) extends TextNode
  }

  import TextNode._

  type TextRenderer[A, S] = Renderer[A, S, TextNode]

//  object TextRenderer {
//    def apply[A, S]() = new Renderer[A, S, TextNode] {
//      def apply[F[_]: Monad](a: A, state: S)(implicit stm: ViewOps[F]): F[TextNode] = {
//
//      }
//    }
//  }


  val stringR: TextRenderer[String, Unit] = new TextRenderer[String, Unit] {
    def apply[F[_]: Monad](a: String, state: Unit)(implicit stm: ViewOps[F]): F[TextNode] = {
      stm.pure(Text(a))
    }
  }

  val intR: TextRenderer[Int, Unit] = new TextRenderer[Int, Unit] {
    def apply[F[_]: Monad](a: Int, state: Unit)(implicit stm: ViewOps[F]): F[TextNode] = {
      stm.pure(Text(a.toString))
    }
  }

  val personR: TextRenderer[Person, Unit] = new TextRenderer[Person, Unit] {
    def apply[F[_]: Monad](a: Person, state: Unit)(implicit stm: ViewOps[F]): F[TextNode] = {
      stm.pure(
        TextNode.All(
          List(
            Text("Person("),
            Suspend(a.name, stringR),
            Text(", "),
            Suspend(a.age, intR),
            Text(")")
          )


        )
      )
    }
  }


}