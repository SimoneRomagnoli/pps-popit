package controller

object Messages {
  trait Message
  trait Render extends Message
  trait Input extends Message
  trait Update extends Message
}
