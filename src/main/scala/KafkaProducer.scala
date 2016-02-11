/*
 * Mark C Wilson Inc
 * Apache 2 License
 *
 * http://kafka.apache.org/documentation.html#producerapi
 */

import java.util.Properties

import kafka.producer.KeyedMessage
import kafka.producer.Producer
import kafka.producer.Producer
import kafka.producer.ProducerConfig

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Some hard coded stuff for now - can move to props when used for real
 *
 * kafka "localhost:9092"
 * zookeeper "localhost:2181"
 * topic "socket-akka-to-kafka"
 *
 */
object KafkaProducer {

  val KAFKA_METADATA_BROKER_LIST = "localhost:9092"
  val KAFKA_ZOOKEEPER_CONN = "localhost:2181"
  val KAFKA_TOPIC_NAME = "socket-akka-to-kafka"

  // Our producer with default props
  val PRODUCER = initProducer(defaultProps)

  def defaultProps: Properties = {
    val p = new Properties()
    p.put("metadata.broker.list", KAFKA_METADATA_BROKER_LIST)
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
