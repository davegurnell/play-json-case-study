package code

import play.api.libs.json._
import play.api.libs.functional.syntax._

// ----------------------------------------------

import java.util.UUID

case class MessageId(value: Long = -1L)

object MessageId {
  // Define a Format[Message]
  // that serializes a message to a string
  implicit val format: Format[MessageId] =
    implicitly[Format[Long]].inmap(MessageId(_), _.value)
}

// ----------------------------------------------

import java.time.ZonedDateTime

case class Message(
  author: String,
  text: String,
  posted: ZonedDateTime = ZonedDateTime.now,
  id: MessageId = MessageId(),
)

object Message {
  // Define an OFormat[Message]
  // that serializes a message to an object
  implicit val format: OFormat[Message] =
    Json.format
}

// ----------------------------------------------

sealed abstract class TrafficLight
final case object Red extends TrafficLight
final case object Amber extends TrafficLight
final case object Green extends TrafficLight

object TrafficLight {
  // Define a Format[TrafficLight]
  // that serializes a value to an integer
  implicit val format: Format[TrafficLight] =
    new Format[TrafficLight] {
      override def reads(in: JsValue): JsResult[TrafficLight] =
        in match {
          case JsNumberAsInt(0) => JsSuccess(Red)
          case JsNumberAsInt(1) => JsSuccess(Amber)
          case JsNumberAsInt(2) => JsSuccess(Green)
          case _                => JsError("Badness!")
        }

      override def writes(in: TrafficLight): JsValue =
        in match {
          case Red   => JsNumber(0)
          case Amber => JsNumber(1)
          case Green => JsNumber(2)
        }
    }

  private object JsNumberAsInt {
    def unapply(value: JsValue): Option[Int] =
      value match {
        case JsNumber(num) => Some(num.toInt)
        case _             => None
      }
  }
}

// ----------------------------------------------

import java.awt.Color

object ColorFormats {
  // Define an OFormat[Color]
  // that serializes a Color to an object
  // with fields for red, green, and blue
  implicit val format: OFormat[Color] = (
    (__ \ "red").format[Int] ~
    (__ \ "green").format[Int] ~
    (__ \ "blue").format[Int]
  )(create, extract)

  def create(r: Int, g: Int, b: Int): Color =
    new Color(r, g, b)

  def extract(c: Color): (Int, Int, Int) =
    (c.getRed, c.getGreen, c.getBlue)
}

// ----------------------------------------------

sealed abstract class Animal
final case class Dog(name: String) extends Animal
final case class Insect(legs: Int) extends Animal
final case class Swallow(maxLoad: Int) extends Animal

object Animal {
  // Define an OFormat[Animal]
  // that serializes an animal to an object
  // with a "type" discriminator field
  // and other fields as appropriate
  val dogFormat: OFormat[Dog] = Json.format
  val insectFormat: OFormat[Insect] = Json.format
  val swallowFormat: OFormat[Swallow] = Json.format

  implicit val format: Format[Animal] =
    new Format[Animal] {
      def reads(in: JsValue) = (in \ "type") match {
        case JsDefined(JsString("Dog"))     => dogFormat.reads(in)
        case JsDefined(JsString("Insect"))  => insectFormat.reads(in)
        case JsDefined(JsString("Swallow")) => swallowFormat.reads(in)
        case _                              => JsError("Badness")
      }

      def writes(in: Animal) = in match {
        case in: Dog     => dogFormat.writes(in)     ++ Json.obj("type" -> "Dog")
        case in: Insect  => insectFormat.writes(in)  ++ Json.obj("type" -> "Insect")
        case in: Swallow => swallowFormat.writes(in) ++ Json.obj("type" -> "Swallow")
      }
    }
}

// ----------------------------------------------

object Main extends App {
  def roundTrip[A: Format](value: A): Unit = {
    println("Original : " + value)
    println("Written  : " + Json.toJson(value))
    println("Reread   : " + Json.toJson(value).validate[A])
  }

  println("MessageId")
  roundTrip(MessageId())
  println(JsNull.validate[MessageId])

  println("Message")
  roundTrip(Message("Author", "Text"))
  println(JsNull.validate[Message])

  println("Color")
  import ColorFormats._
  roundTrip(new Color(0, 127, 255))
  println(JsNull.validate[Color])

  println("TrafficLight")
  roundTrip(Red : TrafficLight)
  roundTrip(Amber : TrafficLight)
  roundTrip(Green : TrafficLight)
  println(JsNull.validate[TrafficLight])

  println("Animal")
  roundTrip(Dog("Sparky") : Animal)
  roundTrip(Insect(6) : Animal)
  roundTrip(Swallow(100) : Animal)
  println(JsNull.validate[Animal])
}
