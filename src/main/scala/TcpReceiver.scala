/*
 * Mark C Wilson Inc
 * Apache 2 License
 *
 * http://doc.akka.io/docs/akka/2.4.1/scala/io-tcp.html
 */

import akka.actor._

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.io.{ IO, Tcp}
import akka.util.ByteString
import java.net.InetSocketAddress

/**
 * Handle ByteStrings (do some work on the data received) fed from a TCP receiver
 * This actor self terminates upon receiving Tcp.Received(data) or Tcp.PeerClosed
 *
 * TODO Bytes to JSON to Kafka
 */
class TcpDataHandler extends Actor with ActorLogging {

  def receive = {
    case Tcp.Received(data) => {
      if (log.isDebugEnabled) log.debug("handler received raw: " + data)

      // do something with the data (turn it into a string)
      val ba = data.toArray
      val s: String = (for (b <- ba) yield { b.toChar }).mkString("")

      // TODO String to JSON to Kafka

      if (log.isInfoEnabled) log.info(s"Consider it handled! ${s}")

      // my work here is done bye bye, be sure to create a new handler if you need something else done
      context stop self
    }
    case Tcp.PeerClosed => {
      if (log.isDebugEnabled) log.debug("Tcp.PeerClosed. Stopping self")
      context stop self
    }
  }
}


/**
 * Binds to host:port and receives tcp packets, passes data to a new instance of TcpDataHandler
 * which terminates itself after handling the data
 *
 */
class TcpBoundReceiver extends Actor with ActorLogging {

  import context.system // implicitly used by IO(Tcp)

  if (log.isDebugEnabled) log.debug("Binding to " + TcpReceiver.displayHostPort())

  // This will instruct the TCP manager to listen for TCP connections on a particular InetSocketAddress
  val manager: ActorRef = IO(Tcp)
  manager ! Tcp.Bind(self, new InetSocketAddress(TcpReceiver.host, TcpReceiver.port))

  /**
   *
   */
  def receive = {
    case b @ Tcp.Bound(localAddress) => {
      if (log.isDebugEnabled) log.debug("Tcp.Bound to " + localAddress)
    }

    case Tcp.CommandFailed(_: Tcp.Bind) => {
      if (log.isWarningEnabled) log.warning("Tcp.CommandFailed! Stopping self")
      context stop self
    }

    case c @ Tcp.Connected(remote, local) => {
      val connection = sender()
      try {
        if (log.isDebugEnabled) log.debug(s"Tcp.Connected local ${local}, remote ${remote}")
        val handler = context.actorOf(Props[TcpDataHandler])
        if (log.isDebugEnabled) log.debug("Sending Tcp.Register to handler")
        connection ! Tcp.Register(handler)
      } finally {
        if (log.isDebugEnabled) log.debug("Closing the connection")
        connection ! Tcp.Close
      }
    }
  }

}

/**
 *
 */
object TcpReceiver {

  val host = "localhost"
  val port = 10119
  def displayHostPort(): String = { s"Host:Port = ${host}:${port}" }

}

object TcpReceiverApp extends App {

  val timeoutShutdown = 11.seconds
  val system = ActorSystem("tcp-receiver-system")

  try {
    println("TcpReceiverApp preparing to listen ")
    val receiverActor: ActorRef = system.actorOf(Props[TcpBoundReceiver])

    // How to send something
    println("Send a message by netcat (nc),  e.g. ")
    println("echo -n \"Hello Akka TCP Receiver\" | nc localhost 10119")

    // Sleep for a bit - the actor is started, bound to the port and receiving
    val twoMin = 120000L
    println("TcpReceiverApp Listening for 2 minutes.... ")
    Thread.sleep(twoMin)
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
