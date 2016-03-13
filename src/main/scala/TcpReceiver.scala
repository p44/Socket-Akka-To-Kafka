/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import akka.actor._

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.io.{ IO, Tcp}
import akka.util.ByteString
import java.net.InetSocketAddress

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Handle ByteStrings (do some work on the data received) fed from a TCP receiver
 * This actor self terminates upon receiving Tcp.Received(data) or Tcp.PeerClosed
 *
 * Bytes to String, sends to Kafka
 * http://doc.akka.io/docs/akka/2.4.1/scala/io-tcp.html
 */
class TcpDataHandler(remote: InetSocketAddress) extends Actor with ActorLogging {

  val kafkaTopic = Models.KAFKA_TOPIC_NAME
  val kafkaProducer = KafkaProducer.PRODUCER
  val remoteAddress = remote.getAddress.getHostAddress

  def receive = {
    case Tcp.Received(data) => {
      if (log.isDebugEnabled) log.debug(s"handler from ${remoteAddress} received raw: ${data} ")

      // do something with the data (turn it into a string)
      val ba = data.toArray
      val s: String = (for (b <- ba) yield {
        b.toChar
      }).mkString("")

      // send to Kafka
      KafkaProducer.produceMessageToTopic(kafkaProducer, kafkaTopic, s) match {
        case true => {
          if (log.isInfoEnabled) log.info(s"Delivered! [${s}]")
          context stop self
        }
        case false => {
          if (log.isInfoEnabled) log.info(s"Trouble reaching Kafka for [${s}]")
          context stop self
        }
      }
    }
    case Tcp.PeerClosed => {
      if (log.isDebugEnabled) log.debug("Tcp.PeerClosed. Stopping self")
      context stop self
    }
  }
}


/**
 * Binds to host:port and receives tcp packets, passes data to
 * a new instance of TcpDataHandler which terminates itself after handling the data
 *
 * @param h host
 * @param p port
 */
class TcpBoundReceiver(h: String, p: Int) extends Actor with ActorLogging {

  val host = h
  val port = p

  import context.system // implicitly used by IO(Tcp)

  if (log.isDebugEnabled) log.debug("Binding to " + Models.displayHostPort(host, port))

  // This will instruct the TCP manager to listen for TCP connections on a particular InetSocketAddress
  val manager: ActorRef = IO(Tcp)
  manager ! Tcp.Bind(self, new InetSocketAddress(host, port))

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
        val handler = context.actorOf(Props(new TcpDataHandler(remote)))
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

  /**
   * Creates an actor TcpBoundReceiver which receives messages on host port.
   * Is is the responsibility of the caller to terminate the akka system.
   *
   * @param system
   * @param host
   * @param port
   */
  def listen(system: ActorSystem, host: String, port: Int): Unit = {
    println("TcpReceiverApp preparing to listen at " +  Models.displayHostPort(host, port))
    val receiverActor: ActorRef = system.actorOf(Props(new TcpBoundReceiver(host, port)))

    // display how to send something from a linux or mac command line
    println("Send a message by netcat (nc),  e.g. ")
    println("echo -n \"Hello Akka TCP Receiver\" | nc localhost 11111")
  }

  /**
   * Creates an actor TcpBoundReceiver which receives messages on host port.
   * Calls listen()
   * Is is the responsibility of the caller to terminate the akka system.
   *
   * @param listenTimeMillis
   * @param system
   * @param host
   * @param port
   */
  def listenForThisLong(listenTimeMillis: Long, system: ActorSystem, host: String, port: Int): Unit = {
    listen(system: ActorSystem, host: String, port: Int)

    // Sleep for a bit - the actor is started, bound to the port and receiving
    val secs = (listenTimeMillis/1000L).toLong
    println(s"TcpReceiverApp Listening for ${secs} seconds.... ")
    Thread.sleep(listenTimeMillis)
  }

  /**
   * Calls ListenForThisLong then terminates the system provided.
   * Use this for a one-off akka system for the TCP receiving to terminate after use
   *
   * @param listenTimeMillis
   */
  def listenForThisLongThenTerminate(listenTimeMillis: Long, system: ActorSystem, host: String, port: Int): Unit = {
    val timeoutShutdown = 11.seconds

    try {
      listenForThisLong(listenTimeMillis, system, host, port)
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

}
