package re.pescato.chat

import akka.actor.ActorSystem
import akka.io.Tcp.{PeerClosed, Received, Write}
import akka.testkit.{DefaultTimeout, TestActorRef, TestKit, TestProbe}
import akka.util.ByteString
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Performs tests of Broadcaster actor
 */
class BroadcasterSpec
  extends TestKit(ActorSystem("BroadcasterSpec"))
    with DefaultTimeout
    with AnyFlatSpecLike
    with Matchers
    with BeforeAndAfterAll {

  "A Broadcaster" should "populate the list of connected clients" in {
      within(500 millis) {
        val broadcaster: TestActorRef[Broadcaster] = TestActorRef[Broadcaster]
        val actor = broadcaster.underlyingActor
        val client1 = TestProbe()
        val client2 = TestProbe()
        val client3 = TestProbe()
        actor.receive(BroadcastSubscription(client1.ref))
        actor.receive(BroadcastSubscription(client2.ref))
        actor.receive(BroadcastSubscription(client3.ref))
        actor.subscribers should have length(3)
      }
    }

  it should "remove clients from the list of connections when PeerClosed message arrives" in {
    within(500 millis) {
      //given
      val broadcaster: TestActorRef[Broadcaster] = TestActorRef[Broadcaster]
      val actor = broadcaster.underlyingActor
      val client1 = TestProbe()
      val client2 = TestProbe()
      val client3 = TestProbe()

      //when
      actor.receive(BroadcastSubscription(client1.ref))
      actor.receive(BroadcastSubscription(client2.ref))
      actor.receive(BroadcastSubscription(client3.ref))
      actor.subscribers should have length(3)

      //then
      broadcaster.tell(PeerClosed, client1.ref);
      broadcaster.tell(PeerClosed, client2.ref);
      broadcaster.tell(PeerClosed, client3.ref);
      actor.subscribers should have length(0)
    }
  }

  it should "broadcast a client message two the others subscribers only" in {
    within(500 millis) {
      //given
      val broadcaster: TestActorRef[Broadcaster] = TestActorRef[Broadcaster]
      val actor = broadcaster.underlyingActor
      val messageSender = TestProbe("client1")
      val senderName = messageSender.ref.path.name;

      val client2 = TestProbe("client2")
      val client3 = TestProbe("client3")
      val client4 = TestProbe("client4")

      actor.receive(BroadcastSubscription(messageSender.ref))
      actor.receive(BroadcastSubscription(client2.ref))
      actor.receive(BroadcastSubscription(client3.ref))
      actor.receive(BroadcastSubscription(client4.ref))

      //when
      broadcaster.tell(Received(ByteString("Hello")), messageSender.ref)

      //then
      val expectedBroadcastOutcome = Write(ByteString(s"Client ${senderName} says > Hello"))
      messageSender.expectNoMessage()   //we should expect no message to itself
      client2.expectMsg(expectedBroadcastOutcome)
      client3.expectMsg(expectedBroadcastOutcome)
      client4.expectMsg(expectedBroadcastOutcome)
    }
  }


}
