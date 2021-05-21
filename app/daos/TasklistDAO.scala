package daos

import model.Tasklist
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TasklistDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                           (implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val Tasklists = TableQuery[TasklistTable]

  def all(): Future[Seq[Tasklist]] = db.run(Tasklists.result)

  def insert(task: Tasklist): Future[Unit] = {
    db.run(Tasklists.insertOrUpdate(task)).map { _ => () }
  }

  private class TasklistTable(tag: Tag) extends Table[Tasklist](tag, "TASKLIST") {

    def title = column[String]("TITLE", O.PrimaryKey)
    def body = column[String]("BODY")

    def * = (title, body) <> (Tasklist.tupled, Tasklist.unapply)
  }
}