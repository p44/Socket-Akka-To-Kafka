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

/**
 * Created by markwilson on 2/11/16.
 *
 * Hard coded:
 * RECEIVER_HOST = "localhost"
 * RECEIVER_PORT = 11111
 * KAFKA_METADATA_BROKER_LIST = "localhost:9092"
 * KAFKA_TOPIC_NAME = "socket-akka-to-kafka"
 */
object Models {

  val RECEIVER_HOST = "localhost"
  val RECEIVER_PORT = 11111

  val KAFKA_METADATA_BROKER_LIST = "localhost:9092"
  val KAFKA_TOPIC_NAME = "socket-akka-to-kafka"

  def displayReceiverHostPortDefaults(): String = { displayHostPort(RECEIVER_HOST, RECEIVER_PORT) }
  def displayHostPort(host: String, port: Int): String = { s"Host:Port = ${host}:${port}" }

}
