package re.pescato.chat

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import com.typesafe.config.ConfigFactory

import java.net.InetSocketAddress
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

/**
 * Represents the entry point of the application.
 * <br/>
 * Implements a Chat Server built using the TCP features of Akka 2.6
 */
object ChatServer {

  //setting up resources
  implicit val actorSystem = ActorSystem("actor-system")
  val messageBroadcasterHandler: ActorRef = actorSystem.actorOf(Props[Broadcaster])

  //defining main actor implementing the TCP server
  class Server(address: InetSocketAddress) extends Actor with ActorLogging {
    IO(Tcp) ! Bind(self, address)

    def receive: Receive = {
      case b @ Bound(localAddress) => {
        log.info(s"Server started listening on ${b.localAddress}")
      }
      case CommandFailed(_: Bind) => {
        log.info("Server stopping, see you!")
        context stop self
      }
      case c @ Connected(remote, local) => {
        log.info("New client connected!")
        val connection = sender()
        messageBroadcasterHandler ! BroadcastSubscription(connection)
        connection ! Register(messageBroadcasterHandler)
      }
      case Received(data) => {
        log.debug(s"Received data: $data")
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val serverHost = config.getString("chatServer.host")
    val serverPort = config.getInt("chatServer.port")
    actorSystem.actorOf(Props(new Server(new InetSocketAddress(serverHost, serverPort))))
  }
}
