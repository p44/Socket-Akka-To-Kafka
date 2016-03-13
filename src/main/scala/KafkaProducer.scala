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

import java.util.Properties

import kafka.producer.KeyedMessage
import kafka.producer.Producer
import kafka.producer.Producer
import kafka.producer.ProducerConfig

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * http://kafka.apache.org/documentation.html#producerapi
 * Some hard coded stuff for now - can move to props when used for real
 *
 * From Models:
 *
 * kafka "localhost:9092"
 * zookeeper "localhost:2181"
 * topic "socket-akka-to-kafka"
 *
 */
object KafkaProducer {

  // Our producer with default props
  val PRODUCER = initProducer(defaultProps)

  def defaultProps: Properties = {
    val p = new Properties()
    p.put("metadata.broker.list", Models.KAFKA_METADATA_BROKER_LIST)
    p.put("request.required.acks", "1")
    p.put("request.timeout.ms", "1000")
    p.put("producer.type", "sync")
    p.put("compression.codec", "none")
    p.put("message.send.max.retries", "1")
    p.put("retry.backoff.ms", "100")
    p.put("topic.metadata.refresh.interval.ms", "300000")
    p.put("send.buffer.bytes", "102400")
    p
  }

  /**
   * Makes a kafka producer by
   * new Producer[AnyRef, AnyRef](new ProducerConfig(props))
   *
   * @param props
   * @return
   */
  def initProducer(props: Properties): Producer[AnyRef, AnyRef] = {
    new Producer[AnyRef, AnyRef](new ProducerConfig(props))
  }

  /**
   *
   * @param producer you can use KafkaProducer.PRODUCER for convenience
   * @param topic
   * @param m
   * @return
   */
  def produceMessageToTopic(producer: Producer[AnyRef, AnyRef], topic: String, m: String): Boolean = {
    println(s"produceMessageToTopic ${topic}  ${m}")
    val keyed: KeyedMessage[AnyRef, AnyRef] = new KeyedMessage(topic, m.getBytes("UTF8"))
    sendMessage(producer, topic, m, keyed)
  }

  /**
   * Tries producer.send(keyed)
   *
   * @param producer
   * @param topic
   * @param m
   * @param keyed
   * @return
   */
  def sendMessage(producer: Producer[AnyRef, AnyRef], topic: String, m: String, keyed: KeyedMessage[AnyRef, AnyRef]):Boolean = {
    try {
      producer.send(keyed) // Unit
      true
    } catch {
      case t: Throwable => {
        println("There was a problem sending a message to kafka [" + topic + "] " + m, t)
        t.printStackTrace()
        false
      }
    }
  }


}
