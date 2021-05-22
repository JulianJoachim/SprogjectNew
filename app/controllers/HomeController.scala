package controllers

import daos.{LoginDAO, TasklistDAO}
import model.{Login, Tasklist}

import javax.inject._
import play.api._
import play.api.data.Form
import play.api.data.Forms.{mapping, number, text}
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class HomeController @Inject() (tasklistDao: TasklistDAO, loginDao: LoginDAO, controllerComponents: ControllerComponents)
                               (implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents) {

  def index() = Action.async {
    tasklistDao.all().map { case (tasks) => Ok(views.html.index(tasks)) }
  }

  def login() = Action {
    Ok(views.html.login())
  }

  def env() = Action { implicit request: Request[AnyContent] =>
    //Ok("Nothing to see here")
    Ok(System.getenv("JDBC_DATABASE_URL"))
  }

  val loginForm = Form(
    mapping(
      "username" -> text(),
      "password" -> text())(Login.apply)(Login.unapply))

  def validateLogin = Action.async { implicit request =>
    val login: Login = loginForm.bindFromRequest.get
    loginDao.all().map{ case _ => Redirect(routes.HomeController.index) }
  }

  def insertLogin = Action.async { implicit request =>
    val login: Login = loginForm.bindFromRequest.get
    loginDao.insert(login).map(_ => Redirect(routes.HomeController.index))
  }

  val taskForm = Form(
    mapping(
      "title" -> text(),
      "body" -> text())(Tasklist.apply)(Tasklist.unapply))

  def insertTask = Action.async { implicit request =>
    val task: Tasklist = taskForm.bindFromRequest.get
    tasklistDao.insert(task).map(_ => Redirect(routes.HomeController.index))
  }


}
