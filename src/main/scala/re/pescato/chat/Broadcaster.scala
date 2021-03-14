package re.pescato.chat

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.Tcp.{PeerClosed, Received, Write}
import akka.util.ByteString
import scala.collection.mutable.ListBuffer


/**
 * ADT representing broadcaster messages
 */
sealed trait BroadcasterMessage
case class BroadcastSubscription(i: ActorRef) extends BroadcasterMessage

/**
 * Implements the broadcasting logic holding an inner list buffer of all the client connections.
 */
class Broadcaster extends Actor with ActorLogging {

  //mutable since encapsulated in this actor only
  var subscribers = new ListBuffer[ActorRef]()

  def receive: Receive = {
    case BroadcastSubscription(i) => {
      log.info(s"${i.path.name} client subscribed!")
      subscribers.append(i)
    }

    case Received(data) => {
      log.debug(s"Received data: ${data.utf8String}")
      broadcast(data, sender())
    }

    case PeerClosed => {
      log.info(s"Client ${sender().path.name} disconnected.")
      subscribers = subscribers.filter(_ != sender())
    }
  }

  /**
   * Broadcasts current message to all subscribers but itself.
   */
  private def broadcast(message: ByteString, from: ActorRef): Unit = {
    val messageContent = s"Client ${from.path.name} says > ${message.utf8String}"
    log.debug(messageContent)
    subscribers.filter(_ != from).foreach(_ ! Write(ByteString(messageContent)))
  }
}
