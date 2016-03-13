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
    val receiverActor = TestActorRef(Props(new TcpBoundReceiver(Models.RECEIVER_HOST, Models.RECEIVER_PORT)))
    assert(receiverActor.underlyingActor.asInstanceOf[TcpBoundReceiver].host == Models.RECEIVER_HOST)
    assert(receiverActor.underlyingActor.asInstanceOf[TcpBoundReceiver].port == Models.RECEIVER_PORT)
    println(s"TEST TcpReceiver Actor path:  ${receiverActor.path}")
    assert(receiverActor.path.toString.contains(testSystemName))
  }


}
