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
    assert(victim.underlyingActor.asInstanceOf[KnockKnockJokeParticipant].feeling == "happy")
    victim ! HowAreYouFeeling
    val x = expectMsgType[String]
    assert(x == "happy")
  }

  "A KnockKnockJokeParticipant" should "play along with a knock knock joke" in {
    val victim = TestActorRef(Props[KnockKnockJokeParticipant])

    println("Knock Knock!")
    victim ! KnockKnock
    val r1 = expectMsgType[String]
    println(r1)
    assert(r1 == "Who's There?")

    println("Orange")
    victim ! KnockKnockWho(s"Orange")
    val r2 = expectMsgType[String]
    println(r2)
    assert(r2 == "Orange who?")

    val pl = "Orange you glad I told this joke?"
    println(pl)
    victim ! KnockKnockPunchLine(pl)
    val r3 = expectMsgType[String]
    println(r3)
    assert(r3 == "That was bad.")

    assert(victim.underlyingActor.asInstanceOf[KnockKnockJokeParticipant].feeling == "annoyed")

  }

  "TellJoke" should "knockKnock" in {
    println()
    println("From the object...")
    println()
    TellJoke.knockKnock(system)
  }

}
