/**
 * Created by markwilson on 2/11/16.
 */
object Models {

  val host = "localhost"
  val port = 11111

  def displayHostPortDefaults(): String = { displayHostPort(host, port) }
  def displayHostPort(host: String, port: Int): String = { s"Host:Port = ${host}:${port}" }

}
