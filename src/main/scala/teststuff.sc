/*def base[T : ClassTag, U : BaseColumnType](tmap: T => U, tcomap: U => T): BaseColumnType[T]*/

def base[T](fun: T => Boolean) = {
  fun
}


base((b: Int) => true)

base( {case b: Int => true} )


List(41, 91).collect({case i if i > 40 => i + 1})


List(41, 91).map(_ + 1)
List(41, 91).collect({case i: Int => i + 1})


val names: List[Option[String]] = List(Some("Johanna"), None, Some("Daniel"))

names.flatMap(xs => xs.map(_.toUpperCase))

val numz: List[Int] = List(1, 30, 41)

numz.find(_ % 2 == 0).map(_ + 1)

numz.filter(_ % 2 == 0).map(_ + 1)

numz.map(xs => xs + 1)



var z = Option[Int](1)

z.foreach(_ == 2)
z.exists(_ == 1)


// __sid=f067eeaeec2b09db7e3336e43b77efb25a2d99c7-id%3A1; __rid=1:8a5a8435-7ed5-432b-a6e3-f26a472c63cd:df5f47e7-b954-4e7c-926a-3854abe96972
// f067eeaeec2b09db7e3336e43b77efb25a2d99c7-id%3A1

// a4c1995adf6ea74bab3b90c124448a3f3921818a-id%3A2

// _sid=a4c1995adf6ea74bab3b90c124448a3f3921818a-id%3A2;



/*"com.jolbox" % "bonecp" % "0.8.0",
"org.reflections" % "reflections" % "0.9.8"*/



//val words = List("one", "two", "one", "three", "four", "two", "one")
//val wordSize = words.groupBy(_.length)
//val counts = words.groupBy(w => w).mapValues(_.size).map({case(_, size) => size})
//val counts2 = words.groupBy(w => w)



import java.time.temporal.{ChronoField, ChronoUnit, TemporalField}
import java.util
import java.util.{UUID, NoSuchElementException, InvalidPropertiesFormatException}


import com.typesafe.config.ConfigFactory
import util.Crypto.Position._
import spray._
import spray.http.DateTime
import spray.json._
import DefaultJsonProtocol._
import spray.routing.AuthenticationFailedRejection.CredentialsRejected
import spray.routing.{ValidationRejection, AuthenticationFailedRejection}

import scala.Some
import scala.collection.immutable.Map



case class School(name: String)

object Schoolz extends DefaultJsonProtocol  {
  implicit val schoolFormat = jsonFormat1(School)
}

import Schoolz._

case class Student(id: Option[Int], name: String)

object Studentz extends DefaultJsonProtocol   {
  implicit val studentFormat = jsonFormat2(Student)
}

import Studentz._

case class Desk(student: Student, school: School)

object Deskz extends DefaultJsonProtocol  {
  implicit val deskFormat = jsonFormat2(Desk)
}

import Deskz._

val hackSchool = School("hacktors")
val goodStudent = Student(Some(21), "Hacker")
val badStudent = Student(Some(41), "BadHacker")
val studentDesk = Desk(goodStudent, hackSchool)
val students = List(badStudent, goodStudent)
students.map(_.id)

val deskAst = studentDesk.toJson

deskAst.prettyPrint

deskAst.convertTo[Desk]

"""[{
  |                 "student": {
  |                   "id": 21,
  |                   "name": "Hacker"
  |                 },
  |                 "school": {
  |                   "name": "hacktors"
  |                 }
  |               },
  |               {
  |                 "student": {
  |                   "id": 21,
  |                   "name": "Hacker"
  |                 },
  |                 "school": {
  |                   "name": "hacktors"
  |                 }
  |               }]""".stripMargin

val inputz = """[{
                 "student": {
                   "id": 21,
                   "name": "Hacker"
                 },
                 "school": {
                   "name": "hacktors"
                 }
               },
               {
                 "student": {
                   "id": 21,
                   "name": "Hacker"
                 },
                 "school": {
                   "name": "hacktors"
                 }
               }]"""

inputz.parseJson.toString()

inputz.parseJson.convertTo[List[Desk]]

JsString("zzz")

val inputz2 = """[]"""

inputz2.parseJson.convertTo[List[Desk]]



val source = """{ "some": "JSON source" }"""
val jsonAst = JsonParser(source)
val jsonObject = deskAst.asJsObject
jsonObject.fields
jsonObject.getFields("student")



Map(("desk" -> studentDesk)).toJson

val obj1 = JsObject("name" -> JsString("zz"))

JsObject(
  "desk" -> studentDesk.toJson,
  "desk2" -> obj1
)

JsObject(
  "lotto" ->
    JsObject(
      "lotto-id" -> JsNumber(21),
           "lotto-numbers" -> studentDesk.toJson,
           "winners" ->
            JsArray(JsObject("winner-id" -> JsNumber(21), "numbers" -> List(2,45,34,23,3,5).toJson),
                    JsObject("winner-id" -> JsNumber(41), "numbers" -> List(2,45,34,23,3,5).toJson)
            )
      )

  ).prettyPrint









val name: String = null
Option(name).exists(!_.isEmpty)

Option(name) match {
  case Some(b) => b
  case None => "piss offf"
}

trait SomeType {}

object ClearType {
    def apply(bb: String) = new SomeType {

    }
}

val clear = ClearType("21")

def someFunc(b: String, z: String => Int): Int = z(b)





val div: (Double, Double) => Double = {
  case (x, y) if y != 0 => x / y
}

div(21, 41)



val b = "zzz"

val c = "zzz"

b == c

case class Test(zz: Int)

class B(b: Int) extends Test(b)

case class C(b: Int) extends Test(b)


def multiply(m: Int)(n: Int): Int = m * n


val timesFour = multiply(5)(_:Int)
timesFour(2)



def multiply2(m: Int, b: Int) = m * b
val curriedFun = (multiply2 _).curried
curriedFun(2)(4)


val cascade: Option[Option[Int]] = Option(Option(21))

cascade.flatMap(_.map(_ + 2))

val cascade2: Option[Int] = Option(21)




// =======================
// service interfaces
trait OnOffDevice {
  def on(): Unit
  def off(): Unit
}
trait SensorDevice {
  def isCoffeePresent: Boolean
}

// =======================
// service implementations
class Heater extends OnOffDevice {
  def on() = println("heater.on")
  def off() = println("heater.off")
}
class PotSensor extends SensorDevice {
  def isCoffeePresent = true
}

// =======================
// service declaring two dependencies that it wants injected,
// is using structural typing to declare its dependencies
class Warmer(env: {
  val potSensor: SensorDevice
  val heater: OnOffDevice
}) {
  def trigger = {
    if (env.potSensor.isCoffeePresent) env.heater.on
    else env.heater.off
  }
}

class Client(env : { val warmer: Warmer }) {
  env.warmer.trigger
}

// =======================
// instantiate the services in a configuration module
object Config {
  lazy val potSensor = new PotSensor
  lazy val heater = new Heater
  lazy val warmer = new Warmer(this) // this is where injection happens
}

new Client(Config)

object Position extends Enumeration {
  val HmacSignature = Value(0)
  val EncryptedMessage = Value(1)
  val EncryptedIv = Value(41)
  val TimeStamp = Value(3)
}

val data = "2%001405182233000%00Email%28onur.zdg%40gmail.com%29"

data.split("\u0000").map(_.split(":")).map(p => p(0) -> p.drop(1).mkString(":"))

Position.EncryptedIv.id
Position.TimeStamp.id

val someString = "zzzz"
val someString2 = "zzzz"

if(someString == someString2) {
  println("they are equal")
}

someString eq someString2
if(someString.equals(someString2)) {
  println("they are equa2l")
}

1405346786000L < (3600000 + 1405346786000L )




val bza = "222222"
bza.split("op")

val someMap = Map.empty

if(someMap eq Map.empty) println("yes empty")


Option(None).get


case class SessionCookie(
                          data: Map[String, String] = Map.empty[String, String],
                          domain: Option[String] = None,
                          path: Option[String] = None,
                          secure: Boolean = true,
                          httpOnly: Boolean = true,
                          extension: Option[String] = None) {

  def get(key: String) = data.get(key)
  def isEmpty: Boolean = data.isEmpty
  def +(kv: (String, String)) = copy(data + kv)
  def -(key: String) = copy(data - key)
  def apply(key: String) = data(key)
}

val cookie: SessionCookie = SessionCookie()

val cookies: List[SessionCookie] = cookie :: Nil

cookies.map {
  case a  if a.data.nonEmpty => a
  case _ => None
}



DateTime.now.clicks


import java.time.LocalDateTime
import java.time.LocalDate
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.sql.Timestamp
import spray.http.DateTime

val ids = ZoneId.getAvailableZoneIds()
ids.contains("GMT")

DateTime.now.clicks
val now: Instant = Instant.now()
val UTC: DateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("UTC"))
UTC.format(now)

val GMT: DateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("GMT"))
GMT.format(now)

val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.systemDefault())


// Sun, 06 Nov 1994 08:49:37 GMT
dateFormatter.format(Instant.now())
Instant.MIN

val now1: Instant = Instant.now


now1.toEpochMilli
DateTime(now1.toEpochMilli).clicks


new Timestamp(454444L).toLocalDateTime


import java.util.concurrent.TimeUnit
TimeUnit.MILLISECONDS.convert(10L, TimeUnit.MINUTES)

TimeUnit.SECONDS.convert(60000, TimeUnit.MILLISECONDS)
TimeUnit.valueOf("hours".toUpperCase)




val beepz = List(41, 31, 21)

beepz match {
case d@ List(_, _*) =>d
case _ => Nil
}


import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

val eventualString: Future[String] = Future {
  "sdfsdfsdf"
}


val empty: Option[String] = Option.empty[String]
empty.orElse(Option("asdsd"))
empty.exists( (a) => a.charAt(0) == 'c')
empty.foreach(_.charAt(2))

val maybeMaybeString: Option[Option[String]] = Option(Option("21323"))




val eventualMaybeNone: Future[Option[String]] = Future(Option.empty)

def foop(str: String) = Future {
  str + 41
}

val eventualInt: Future[Int] = Future {
  21
}
import java.util.{NoSuchElementException, InvalidPropertiesFormatException}
val combinedFuture = for{
  a <- eventualString
  pp <- foop(a)
} yield {
  pp.charAt(2)
}
import spray.routing.AuthenticationFailedRejection.CredentialsRejected
import spray.routing.{ValidationRejection, AuthenticationFailedRejection}
combinedFuture.onSuccess({
  case char => "Sdfsdffd" + char
})
combinedFuture.onFailure({
  case e => e match {
    case b:NoSuchElementException =>  AuthenticationFailedRejection(CredentialsRejected, List())
    case z:NullPointerException =>  AuthenticationFailedRejection(CredentialsRejected, List())
  }
})


val xs = List("date", "since", "other1", "other2")


xs.foreach { str =>

  str match {
    case "date"  => println("Match Date")
    case "since" => println("Match Since")
    case unknow  => println("Others")
  }

  println("Put your post step here")
}

"asdsd".split(".")

import java.time.ZonedDateTime
import java.time.LocalDateTime
import java.time.Clock
import java.time.Instant
import java.time.temporal.{ChronoUnit, ChronoField, TemporalField}


ZonedDateTime.now()
Instant.now()

import storage.RememberMeCookieTokenStorageComponent

classOf[ZonedDateTime]


import shapeless._
import spray.routing._
import Directives._

import spray.routing.HttpService
import spray.testkit.Specs2RouteTest

val intParameter: Directive1[Int] = parameter('a.as[Int])

val myDirective: Directive1[Int] =
  intParameter.flatMap {
    case a if a > 0 => provide(2 * a)
    case _ => reject
  }

val i =  "/?a=21"
myDirective(i => complete(i.toString))



Instant.now.toEpochMilli.toString

import java.util.UUID


UUID.randomUUID().toString

trait Zz {
  val className = this.getClass.getSimpleName
}

class Pp extends  Zz {
  println(className)
}

new Pp

"""{"email":"zzz@asdsd.com", "password":"zzzzz", "rememberMe": false }""".trim().replaceAll(" +", " ")


"""{"success": false, "errors": ["send the header loginCsfrToken"]}""".replaceAll(" +", "")

"""{
  "success": false,
  "errors": ["send the header loginCsfrToken"]
}""".filter(_ >= ' ').trim().replaceAll(" +", "")




object Email {
  // The injection method (optional)
  def apply(user: String, domain: String) = user +"@"+ domain
  // The extraction method (mandatory)
  def unapply(str: String): Option[(String, String)] = {
    val parts = str split "@"
    if (parts.length == 2) Some(parts(0), parts(1)) else None
  }
}

val email: String = Email("Assd", "Asdasdasdsd")


val A = "a"
val bz = "b"
"a" match {
  case `bz` => println("b")
  case A => println("A")
}




/*
Option(namez) match {
  case Some(b) => b
  case None => "piss offf"
}*/



import java.time.ZoneOffset
import java.time.LocalDateTime
LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)