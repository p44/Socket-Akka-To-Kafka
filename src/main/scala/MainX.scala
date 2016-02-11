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
 * Starts socket listeners for only 2 minutes then terminates the akka system
 *
 */
object MainX extends App {

  val system = ActorSystem("socket-listening-hero")
  val twoMin = 120000L
  val listenTimeMillis = twoMin
  val timeoutShutdown = 11.seconds

  try {
    // Tell a joke
    //TellJoke.knockKnock(system)

    // TCP bind receiver
    TcpReceiver.listen(system, Models.host, Models.port)
    println("*** Start TCP Listening ***")

    // TODO UDP bind receiver
    //println("TODO UDP receiver")

    // Sleep for a bit - the actor is started, bound to the port and receiving
    val secs = (listenTimeMillis/1000L).toLong
    println(s"TcpReceiverApp Listening for ${secs} seconds.... ")
    Thread.sleep(listenTimeMillis)

    println("*** End Listening ***")
  } catch {
    case e: Exception => {
      println("Something unexpected happened.")
      e.printStackTrace()
    }
  } finally {
    println("Terminating")
    val t: Terminated = Await.result(system.terminate(), timeoutShutdown)
    println(t)
  }

}
