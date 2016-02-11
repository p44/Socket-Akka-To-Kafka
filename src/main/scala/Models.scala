/**
 * Created by markwilson on 2/11/16.
 */
object Models {

  val RECEIVER_HOST = "localhost"
  val RECEIVER_PORT = 11111

  val KAFKA_METADATA_BROKER_LIST = "localhost:9092"
  val KAFKA_TOPIC_NAME = "socket-akka-to-kafka"

  def displayReceiverHostPortDefaults(): String = { displayHostPort(RECEIVER_HOST, RECEIVER_PORT) }
  def displayHostPort(host: String, port: Int): String = { s"Host:Port = ${host}:${port}" }

}
