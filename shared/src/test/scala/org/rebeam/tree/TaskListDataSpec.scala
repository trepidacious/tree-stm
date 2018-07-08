package org.rebeam.tree

import cats.implicits._
import org.rebeam.tree.Delta._
import org.rebeam.tree.TaskListData._
import org.rebeam.tree.Guid._
import org.rebeam.tree.MapStateSTM._
import org.rebeam.tree.Transaction.DeltaAtId
import org.scalatest._
import org.scalatest.prop.Checkers

class TaskListDataSpec extends WordSpec with Matchers with Checkers {

  private val taskListResult = createTaskList[S].run(emptyState).value

  private val taskListGuid = Guid(SessionId(0), SessionTransactionId(0), TransactionClock(0))

  private val taskListId = Id[TaskList](taskListGuid)

  "TaskListData" should {

    "create expected data" in {
      val (s1, taskList) = taskListResult

      assert(taskList == TaskList(taskListId, "Task List", List(Task("task 1", done = false), Task("task 2", done = true))))
      assert(s1.nextGuid == Guid(SessionId(0), SessionTransactionId(0), TransactionClock(1)))
      assert(s1.map == Map(
        taskListGuid -> taskList
      ))

    }

    "use cursor to task list name" in {

      val (s1, taskList) = taskListResult

      // Cursor at the name of task list
      val taskListName = DeltaCursor
        .AtId(taskList.id)
        .zoom(TaskList.name)

      val newName = "The list of tasks"

      val t1 = taskListName.set(newName)

      // We should have a transaction with expected delta at expected id
      assert (t1 == DeltaAtId(taskListId, LensDelta(TaskList.name, ValueDelta(newName))))

      val (s2, _) = t1[S].run(s1).value

      assert(s2.get(taskListId).map(_.name).contains(newName))

    }

    "use cursor to first task's name" in {

      val (s1, taskList) = taskListResult

      // Cursor at the name of the first task
      val firstTaskName = DeltaCursor
        .AtId(taskList.id)
        .zoom(TaskList.tasks)
        .zoomIndex(0)
        .zoom(Task.name)

      val newName = "The First Task"

      val t1 = firstTaskName.set(newName)

      // We should have a transaction with expected delta at expected id
      assert (t1 ==
        DeltaAtId(
          taskListId,
          LensDelta(
            TaskList.tasks,
            TraversableIndexDelta[List, Task](
              0,
              LensDelta(
                Task.name,
                ValueDelta(newName)
              )
            )
          )
        )
      )

      val (s2, _) = t1[S].run(s1).value

      assert(s2.get(taskListId).map(_.tasks.head.name).contains(newName))
    }
  }

  "encode and decode delta on first task's name" in {

    val newName = "The First Task"

    val delta: Delta[TaskList] = LensDelta(
      TaskList.tasks,
      TraversableIndexDelta[List, Task](
        0,
        LensDelta(
          Task.name,
          ValueDelta(newName)
        )
      )
    )

    import org.rebeam.tree.codec.syntax._

    println(delta.asJsonOption.map(_.spaces2))

  }

}
