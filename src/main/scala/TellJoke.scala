import akka.actor.{ ActorRef, ActorSystem, Props, Actor, Inbox }
import scala.concurrent.duration._

case object ResetFeeling
case object HowAreYouFeeling
case object KnockKnock
case class KnockKnockWho(s: String)
case class KnockKnockPunchLine(s: String)

/**
 *
 */
class KnockKnockJokeParticipant extends Actor {
	
  val happy: String = "happy"
  val annoyed: String  = "annoyed"
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
 * Tests basic Akka functionality litmus for build
 */
object TellJoke extends App {

  val system = ActorSystem("knockknock")
  val victim = system.actorOf(Props[KnockKnockJokeParticipant], "victim")
  val joker = Inbox.create(system)

  println("Knock Knock!")
  joker.send(victim, KnockKnock)
  val r1 = joker.receive(4.seconds)
  println(r1)

  println("Orange.")
  joker.send(victim, KnockKnockWho(s"Orange"))
  val r2 = joker.receive(4.seconds)
  println(r2)

  val pl = "Orange you glad I told this joke?"
  println(pl)
  joker.send(victim, KnockKnockPunchLine(pl))
  val r3 = joker.receive(4.seconds)
  println(r3)
  
  system.shutdown()
  system.awaitTermination(11.seconds)
  
}
