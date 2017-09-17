//   Copyright © 2017 Vaughn Vernon. All rights reserved.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package co.vaughnvernon.mockroservices.messagebus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import co.vaughnvernon.mockroservices.messagebus.Message;
import co.vaughnvernon.mockroservices.messagebus.MessageBus;
import co.vaughnvernon.mockroservices.messagebus.Subscriber;
import co.vaughnvernon.mockroservices.messagebus.Topic;

public class MessageBusTest {

  @Test
  public void testMessageBusStart() throws Exception {
    final MessageBus messageBus = MessageBus.start("test_bus");
    assertNotNull(messageBus);
  }

  @Test
  public void testTopicOpen() throws Exception {
    final MessageBus messageBus = MessageBus.start("test_bus");
    assertNotNull(messageBus);
    final Topic topic = messageBus.openTopic("test_topic");
    assertNotNull(topic);
    final Topic topicAgain = messageBus.openTopic("test_topic");
    assertSame(topic, topicAgain);
    topic.close();
  }

  @Test
  public void testTopicPubSub() throws Exception {
    final MessageBus messageBus = MessageBus.start("test_bus");
    final Topic topic = messageBus.openTopic("test_topic");
    final TestSubscriber subscriber = new TestSubscriber();
    topic.subscribe(subscriber);
    topic.publish(new Message("1", "type1", "test1"));
    topic.publish(new Message("2", "type2", "test2"));
    topic.publish(new Message("3", "type3", "test3"));
    
    topic.close();
    
    assertEquals(3, subscriber.handledMessages.size());
    assertEquals("1", subscriber.handledMessages.get(0).id);
    assertEquals("type1", subscriber.handledMessages.get(0).type);
    assertEquals("test1", subscriber.handledMessages.get(0).payload);
    assertEquals("2", subscriber.handledMessages.get(1).id);
    assertEquals("type2", subscriber.handledMessages.get(1).type);
    assertEquals("test2", subscriber.handledMessages.get(1).payload);
    assertEquals("3", subscriber.handledMessages.get(2).id);
    assertEquals("type3", subscriber.handledMessages.get(2).type);
    assertEquals("test3", subscriber.handledMessages.get(2).payload);
  }

  private class TestSubscriber implements Subscriber {
    private final List<Message> handledMessages = new ArrayList<Message>();
    
    public void handle(final Message message) {
      handledMessages.add(message);
    }
  }
}
