/*
 * Mark C Wilson Inc
 * Apache 2 License
 *
 * http://doc.akka.io/docs/akka/2.4.1/scala/io-tcp.html
 */

import akka.actor.{Terminated, ActorSystem}

import scala.concurrent.Await
import scala.concurrent.duration._


/**
 * Main App
 * Starts socket listeners which produces to kafka a string from bytes to char
 * Listens indefinitely until killed.  Terminates the akka system on kill
 *
 */
object MainX extends App {

  val system = ActorSystem("socket-listening-hero")
  val timeoutShutdown = 11.seconds

  try {
    // Tell a joke
    //TellJoke.knockKnock(system)

    // TCP bind receiver
    TcpReceiver.listen(system, Models.RECEIVER_HOST, Models.RECEIVER_PORT)
    println("*** Start TCP Listening ***")

  } catch {
    case e: Exception => {
      println("Something unexpected happened.")
      e.printStackTrace()
    }
  } finally {
    sys.addShutdownHook {
      println("*** End TCP Listening ***")
      println("Terminating Akka System")
      val t: Terminated = Await.result(system.terminate(), timeoutShutdown)
      println(t)
    }
  }

}
