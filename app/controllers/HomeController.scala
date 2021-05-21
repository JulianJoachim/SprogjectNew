package controllers

import daos.TasklistDAO
import model.Tasklist

import javax.inject._
import play.api._
import play.api.data.Form
import play.api.data.Forms.{mapping, number, text}
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class HomeController @Inject() (tasklistDao: TasklistDAO, controllerComponents: ControllerComponents)
                               (implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents) {

  def index() = Action.async {
    tasklistDao.all().map { case (tasks) => Ok(views.html.index(tasks)) }
  }

  def env() = Action { implicit request: Request[AnyContent] =>
    Ok("Nothing to see here")
    //Ok(System.getenv("JDBC_DATABASE_URL"))
  }

  val taskForm = Form(
    mapping(
      "TITLE" -> text(),
      "BODY" -> text())(Tasklist.apply)(Tasklist.unapply))

  def insertTask = Action.async { implicit request =>
    val task: Tasklist = taskForm.bindFromRequest.get
    tasklistDao.insert(task).map(_ => Redirect(routes.HomeController.index))
  }
}
