package co.vaughnvernon.mockroservices.eventjournal;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import co.vaughnvernon.mockroservices.messagebus.Message;
import co.vaughnvernon.mockroservices.messagebus.MessageBus;
import co.vaughnvernon.mockroservices.messagebus.Subscriber;
import co.vaughnvernon.mockroservices.messagebus.Topic;

public class EventJournalPublisherTest {

  @Test
  public void testEventJournalPublisher() throws Exception {
    final EventJournal eventJournal = EventJournal.open("test-ej");
    final MessageBus messageBus = MessageBus.start("test-bus");
    final Topic topic = messageBus.openTopic("test-topic");
    EventJournalPublisher journalPublisher =
        EventJournalPublisher.using(eventJournal.name(), messageBus.name(), topic.name());
    
    final TestSubscriber subscriber = new TestSubscriber();
    topic.subscribe(subscriber);
    
    for (int idx = 0; idx < 3; ++idx) {
      eventJournal.write("test1", idx, "test1type", "test1instance" + idx);
    }
    
    for (int idx = 0; idx < 3; ++idx) {
      eventJournal.write("test2", idx, "test2type", "test2instance" + idx);
    }
    
    subscriber.waitForExpectedMessages(6);
    
    topic.close();

    journalPublisher.close();
    
    assertEquals(6, subscriber.handledMessages.size());
  }

  private class TestSubscriber implements Subscriber {
    private final List<Message> handledMessages = new ArrayList<Message>();
    
    public void handle(final Message message) {
      handledMessages.add(message);
    }
    
    public void waitForExpectedMessages(final int count) throws Exception {
      for (int idx = 0; idx < 100; ++idx) {
        if (handledMessages.size() == count) {
          break;
        } else {
          Thread.sleep(100L);
        }
      }
    }
  }
}
