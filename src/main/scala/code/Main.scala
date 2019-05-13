package code

import java.time.ZonedDateTime
import java.util.UUID

import play.api.libs.json._
import play.api.libs.functional.syntax._

// ----------------------------------------------

case class Message(
  author: String,
  text: String,
  posted: ZonedDateTime = ZonedDateTime.now,
  id: UUID,
)

object Message {
  // Define an OFormat[Message] that serializes a message to an object
}

// ----------------------------------------------

case class MessageId(value: UUID)

object MessageId {
  // Define a Format[Message] that serializes a message ID to a string
}

// ----------------------------------------------

// Now go back and modify Message so its primary key is a MessageId!
// Do you need to write any additional Formats?

// ----------------------------------------------

sealed abstract class TrafficLight
final case object Red extends TrafficLight
final case object Amber extends TrafficLight
final case object Green extends TrafficLight

object TrafficLight {
  // Define a Format[TrafficLight] that serializes a value to a string
}

// ----------------------------------------------

import java.awt.Color

object ColorFormats {
  // Define an OFormat[Color]
  // that serializes a Color to an object
  // with fields for red, green, and blue
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
}

// ----------------------------------------------

object Main extends App {
  def roundTrip[A: Format](value: A): Unit = {
    println("Original : " + value)
    println("Written  : " + Json.toJson(value))
    println("Reread   : " + Json.toJson(value).validate[A])
  }

  println("MessageId")
  // roundTrip(MessageId())
  // println(JsNull.validate[MessageId])

  println("Message")
  // roundTrip(Message("Author", "Text"))
  // println(JsNull.validate[Message])

  println("Color")
  // roundTrip(new Color(0, 127, 255))
  // println(JsNull.validate[Color])

  println("TrafficLight")
  // roundTrip(Red)
  // roundTrip(Amber)
  // roundTrip(Green)
  // println(JsNull.validate[TrafficLight])

  println("Animal")
  // roundTrip(Dog("Sparky"))
  // roundTrip(Insect(6))
  // roundTrip(Swallow(100))
  // println(JsNull.validate[Animal])
}
