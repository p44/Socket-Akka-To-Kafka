/**
 * Mark C Wilson, Inc.
 * Apache 2 License
 */

import akka.actor._
import scala.concurrent.Await
import scala.concurrent.duration._

case object ResetFeeling

case object HowAreYouFeeling

case object KnockKnock

case class KnockKnockWho(s: String)

case class KnockKnockPunchLine(s: String)

/**
 * Simple Actor to verify Akka system
 */
class KnockKnockJokeParticipant extends Actor {

  val happy: String = "happy"
  val annoyed: String = "annoyed"
  var feeling = happy

  def receive = {
    case ResetFeeling => feeling = happy
    case HowAreYouFeeling => sender ! feeling
    case KnockKnock => {
      sender ! "Who's There?"
    }
    case KnockKnockWho(s) => sender ! s"$s who?"
    case KnockKnockPunchLine(s) => {
      println("...groan...")
      feeling = annoyed
      sender ! "That was bad."
    }
  }
}

/**
 * Simple app to verify Akka system
 */
object TellJoke {

  def knockKnock(system: ActorSystem): Unit = {

    val timeoutActor = 4.seconds
    val timeoutShutdown = 11.seconds

    val victim = system.actorOf(Props[KnockKnockJokeParticipant], "victim")
    val joker = Inbox.create(system)

    println("Knock Knock!")
    joker.send(victim, KnockKnock)
    val r1 = joker.receive(timeoutActor)
    println(r1)

    println("Orange.")
    joker.send(victim, KnockKnockWho(s"Orange"))
    val r2 = joker.receive(timeoutActor)
    println(r2)

    val pl = "Orange you glad I told this joke?"
    println(pl)
    joker.send(victim, KnockKnockPunchLine(pl))
    val r3 = joker.receive(timeoutActor)
    println(r3)

    // Caller is responsible to terminate the sytem
    //val t: Terminated = Await.result(system.terminate(), timeoutShutdown)
    //println(t)

  }
}
