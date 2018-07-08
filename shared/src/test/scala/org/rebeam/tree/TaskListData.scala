package org.rebeam.tree

import cats.Monad
import cats.implicits._
import monocle.macros.Lenses
import org.rebeam.tree.codec.Codec
import org.rebeam.tree.codec.Codec._

object TaskListData {

  @Lenses
  case class Task(name: String, done: Boolean)

  @Lenses
  case class TaskList(id: Id[TaskList], name: String, tasks: List[Task])

  // Delta codecs

  implicit val taskDeltaCodec: Codec[Delta[Task]] = lens("name", Task.name) or lens("done", Task.done)

  // Can edit any list of Tasks using index (not the best approach - better to use a list of Refs, then edit using Id)
  implicit val tasksDeltaCodec: DeltaCodec[List[Task]] = listIndex[Task]

  implicit val taskListDeltaCodec: Codec[Delta[TaskList]] = lens("name", TaskList.name) or lens("tasks", TaskList.tasks)

  def createTaskList[F[_]: Monad](implicit stm: STMOps[F]): F[TaskList] = {
    import stm._
    for {
      taskList <- put[TaskList](
        TaskList(
          _,
          "Task List",
          List(
            Task("task 1", done = false),
            Task("task 2", done = true)
          )
        )
      )
    } yield taskList
  }

}
