import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import akka.actor.{Terminated, Actor, Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestActorRef}
import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Test a knock knock joke
 */
class TellJokeSpec(_system: ActorSystem)
  extends TestKit(_system)
  with ImplicitSender
  with Matchers
  with FlatSpecLike
  with BeforeAndAfterAll {

  val timeoutShutdown = 11.seconds

  def this() = this(ActorSystem("knock-knock-test"))

  override def afterAll: Unit = {
    val t: Terminated = Await.result(system.terminate(), timeoutShutdown)
    println(t)
  }

  "A KnockKnockJokeParticipant" should "reset feeling state" in {
    val victim = TestActorRef(Props[KnockKnockJokeParticipant])
    victim ! ResetFeeling
    victim.underlyingActor.asInstanceOf[KnockKnockJokeParticipant].feeling should be("happy")
    victim ! HowAreYouFeeling
    val x = expectMsgType[String]
    x should be("happy")
  }

  "A KnockKnockJokeParticipant" should "play along with a knock knock joke" in {
    val victim = TestActorRef(Props[KnockKnockJokeParticipant])

    println("Knock Knock!")
    victim ! KnockKnock
    val r1 = expectMsgType[String]
    println(r1)
    r1 should be("Who's There?")

    println("Orange")
    victim ! KnockKnockWho(s"Orange")
    val r2 = expectMsgType[String]
    println(r2)
    r2 should be("Orange who?")

    val pl = "Orange you glad I told this joke?"
    println(pl)
    victim ! KnockKnockPunchLine(pl)
    val r3 = expectMsgType[String]
    println(r3)
    r3 should be("That was bad.")

    victim.underlyingActor.asInstanceOf[KnockKnockJokeParticipant].feeling should be("annoyed")

  }

  "TellJoke" should "knockKnock" in {
    println()
    println("From the object...")
    println()
    TellJoke.knockKnock(system)
    true should be(true)
  }

}
