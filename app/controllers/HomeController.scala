package controllers

import daos.{LoginDAO, TasklistDAO}
import model.{GetDelete, Login, LoginFunc, Tasklist}
import slick.jdbc.PostgresProfile.api._

import javax.inject._
import play.api._
import play.api.data.Form
import play.api.data.Forms.{date, default, mapping, number, sqlDate, text}
import play.api.mvc._

import javax.inject._
import play.api.mvc._
import play.api.i18n._
import play.api.libs.json._
import models._
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import java.time.LocalDate
import java.sql.Date
import scala.concurrent.Future
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeController @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(tasklistDao: TasklistDAO, loginDao: LoginDAO, cc: ControllerComponents)
                               (implicit executionContext: ExecutionContext) extends AbstractController(cc)
                                with HasDatabaseConfigProvider[JdbcProfile] {

  def index(): Action[AnyContent] = Action.async { implicit request =>
    val usernameS: String = request.session.get("username").getOrElse("Unauthorized")
    val matches = (usernameS != "Unauthorized")
        if (matches) tasklistDao.allOfUser(usernameS).map{ case (tasks) => Ok(views.html.index(tasks, usernameS)) }
        else Future.successful(Redirect(routes.HomeController.login()).withNewSession)
    }

  def login() = Action { implicit request =>
    Ok(views.html.login())
  }

  def logout() = Action { implicit request =>
    Redirect(routes.HomeController.login()).withNewSession
  }

  def env() = Action { implicit request: Request[AnyContent] =>
    Ok("Nothing to see here")
    //Ok(System.getenv("JDBC_DATABASE_URL"))
  }

  val loginForm = Form(
    mapping(
      "username" -> text(),
      "password" -> text())(Login.apply)(Login.unapply))

  private val model = new LoginFunc(loginDao, db)

  def validateLogin = Action.async { implicit request =>
    val login: Login = loginForm.bindFromRequest().get
    val matches = model.validateLogin(login.username, login.password)
    matches.map { usersName =>
      usersName match {
        case Some(login.username) =>
          Redirect(routes.HomeController.index()).withSession("username" -> login.username)
        case None =>
          Redirect(routes.HomeController.login())
      }
    }
  }

  val createForm = Form(
    mapping(
      "username2" -> text(),
      "password2" -> text())(Login.apply)(Login.unapply))

  def insertLogin = Action.async { implicit request =>
    val createLogin: Login = createForm.bindFromRequest().get
    val matches = model.validateCreation(createLogin.username)

    matches.map { usersName =>
      usersName match {
        case Some(createLogin.username) =>
          Redirect(routes.HomeController.login())
        case None =>
          loginDao.insert(createLogin)
          Redirect(routes.HomeController.index).withSession("username" -> createLogin.username)
      }
    }
  }

  val taskForm = Form(
    mapping(
      "idAuto" -> number(),
      "title" -> text(),
      "body" -> text(),
      "username" -> text())(Tasklist.apply)(Tasklist.unapply)
  )

  def insertTask = Action.async { implicit request =>
    val task: Tasklist = taskForm.bindFromRequest.get
    tasklistDao.insert(task).map(_ => Redirect(routes.HomeController.index))
  }

  val readDelete = Form(
    mapping(
      "numberToDelete" -> number())(GetDelete.apply)(GetDelete.unapply))

  def deleteTask() = Action.async { implicit request =>
    val num: GetDelete = readDelete.bindFromRequest.get
    tasklistDao.remove(num.numberToDelete).map(_ => Redirect(routes.HomeController.index))
  }
}
