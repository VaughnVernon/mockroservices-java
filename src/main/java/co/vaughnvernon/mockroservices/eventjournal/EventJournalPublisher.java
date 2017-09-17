package co.vaughnvernon.mockroservices.eventjournal;

import co.vaughnvernon.mockroservices.messagebus.Message;
import co.vaughnvernon.mockroservices.messagebus.MessageBus;
import co.vaughnvernon.mockroservices.messagebus.Topic;

public class EventJournalPublisher extends Thread {
  private volatile boolean closed;
  private final EventJournalReader reader;
  private final Topic topic;
  
  public static EventJournalPublisher using(
      final String eventJournalName,
      final String messageBusName,
      final String topicName) {
    
    return new EventJournalPublisher(eventJournalName, messageBusName, topicName);
  }
  
  public void close() {
    closed = true;
  }
  
  @Override
  public void run() {
    while (!closed) {
      final StoredEvent event = reader.readNext();
      
      if (event.isValid()) {
        final Message message =
            new Message(
                String.valueOf(event.id),
                event.eventValue.type,
                event.eventValue.body);

        topic.publish(message);
        
        reader.acknowledge(event.id);
        
      } else {
        try {
          Thread.sleep(100L);
        } catch (Exception e) {
          // ignore
        }
      }
    }
  }
  
  protected EventJournalPublisher(
      final String eventJournalName,
      final String messageBusName,
      final String topicName) {

    this.reader = EventJournal.open(eventJournalName).reader("topic-" + topicName + "-reader");
    this.topic = MessageBus.start(messageBusName).openTopic(topicName);
    
    start();
  }
}
