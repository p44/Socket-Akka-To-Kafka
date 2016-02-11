import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import akka.actor._
import akka.testkit.{ImplicitSender, TestKit, TestActorRef}
import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by markwilson on 2/11/16.
 */
class TcpReceiverSpec(_system: ActorSystem)
  extends TestKit(_system)
  with ImplicitSender
  with Matchers
  with FlatSpecLike
  with BeforeAndAfterAll {

  val timeoutShutdown = 11.seconds

  val testSystemName = "tcp-receiver-test"
  def this() = this(ActorSystem("tcp-receiver-test"))

  override def afterAll: Unit = {
    val t: Terminated = Await.result(system.terminate(), timeoutShutdown)
    println(t)
  }

  "A TcpReceiver" should "construct with host port" in {
    val victim = TestActorRef(Props[KnockKnockJokeParticipant])
    val receiverActor = TestActorRef(Props(new TcpBoundReceiver(Models.host, Models.port)))
    receiverActor.underlyingActor.asInstanceOf[TcpBoundReceiver].host should be(Models.host)
    receiverActor.underlyingActor.asInstanceOf[TcpBoundReceiver].port should be(Models.port)
    println(s"TEST TcpReceiver Actor path:  ${receiverActor.path}")
    receiverActor.path.toString.contains(testSystemName) === true
  }


}
