package org.rebeam.tree

//import cats.Monad
//import cats.implicits._
//import monocle.macros.Lenses

object ExampleData {
//
//  @Lenses
//  case class Task(id: Id[Task], name: String, done: Boolean)
//
//  @Lenses
//  case class TaskList(id: Id[TaskList], name: String, tasks: List[Task])
//
//  // Add x times a random number to an Int
//  case class AddRandom(times: Int) extends Delta[Int] {
//    def apply[F[_]: Monad](a: Int)(implicit stm: STMOps[F]): F[Int] = {
//      import stm._
//      for {
//        r <- randomInt
//      } yield a + r * times
//    }
//  }
//
//  def createTaskList[F[_]: Monad](implicit stm: STMOps[F]): F[TaskList] = {
//    import stm._
//    for {
//      task1 <- put[Task](Task(_, "task 1", done = false))
//      task2 <- put[Task](Task(_, "task 2", done = true))
//      taskList <- put[TaskList](TaskList(_, "Task List", List(task1, task2)))
//    } yield taskList
//  }

}
