package org.rebeam.tree.stm

import org.rebeam.tree.MapStateSTM._
import cats.Monad
import cats.implicits._
import monocle.macros.Lenses
import org.rebeam.tree._

object STMMain {

  @Lenses
  case class Task(id: Id[Task], name: String, done: Boolean)

  @Lenses
  case class TaskList(id: Id[TaskList], name: String, tasks: List[Task])

  case class ThingWithId(id: Id[ThingWithId], name: String)

  case class AddRandom(times: Int) extends Delta[Int] {
    def apply[F[_]: Monad](a: Int)(implicit stm: STMOps[F]): F[Int] = {
      import stm._
      for {
        r <- randomInt
      } yield a + r * times
    }
  }

  def example[F[_]: Monad](implicit stm: STMOps[F]): F[Option[ThingWithId]] = {
    import stm._
    for {
      thing <- put[ThingWithId](id => ThingWithId(id, "I'm a thing!"))
      _ <- putF[Int](_ => randomInt)
      _ <- putF[Int](_ => randomInt)
      _ <- put[ThingWithId](id => ThingWithId(id, "I'm a thing!"))
      a <- get(thing.id)
      _ <- modify[ThingWithId](thing.id, _.copy(name = "I'm a modified thing!"))
    } yield a
  }

  def createTaskList[F[_]: Monad](implicit stm: STMOps[F]): F[TaskList] = {
    import stm._
    for {
      task1 <- put[Task](Task(_, "task 1", done = false))
      task2 <- put[Task](Task(_, "task 2", done = true))
      taskList <- put[TaskList](TaskList(_, "Task List", List(task1, task2)))
    } yield taskList
  }

  def main(args: Array[String]): Unit = {

    val (s1, taskList) = createTaskList[S].run(emptyState).value

    println(taskList)

    // Cursor at the name of task list
    val taskListName = DeltaCursor
      .AtId(taskList.id)
      .zoom(TaskList.name)

    val t1 = taskListName.set("The list of tasks")

    println(t1)

    val (s2, _) = t1[S].run(s1).value

    println(s2)

    // Cursor at the name of the first task
    val firstTaskName = DeltaCursor
      .AtId(taskList.id)
      .zoom(TaskList.tasks)
      .zoomIndex(0)
      .zoom(Task.name)

    val t2 = firstTaskName.set("The First Task")

    println(t2)

    val (s3, _) = t2[S].run(s2).value

    println(s3)


    //    val (state, result) = example[S].run(emptyState).value
//    println(state)
//    println(result)
  }


}
