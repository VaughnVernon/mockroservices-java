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

package co.vaughnvernon.mockroservices.journal;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import co.vaughnvernon.mockroservices.messagebus.Message;
import co.vaughnvernon.mockroservices.messagebus.MessageBus;
import co.vaughnvernon.mockroservices.messagebus.Subscriber;
import co.vaughnvernon.mockroservices.messagebus.Topic;

public class JournalPublisherTest {

  @Test
  public void testSingleMessageJournalPublisher() throws Exception {
    final Journal journal = Journal.open("test-journal-single");
    final MessageBus messageBus = MessageBus.start("test-bus-single");
    final Topic topic1 = messageBus.openTopic("test-topic1-single");
    JournalPublisher journalPublisher1 = JournalPublisher.using(journal.name(), messageBus.name(), topic1.name());

    final TestSubscriber subscriber1 = new TestSubscriber();
    topic1.subscribe(subscriber1);

    final EntryBatch batch1 = new EntryBatch();
    batch1.addEntry("test1type-1", "test1instance1");
    journal.write("test1-1", EntryValue.NO_STREAM_VERSION, batch1);

    subscriber1.waitForExpectedMessages(1);
    topic1.close();
    journalPublisher1.close();

    assertEquals(1, subscriber1.handledMessages.size());
  }

  @Test
  public void testJournalPublisher() throws Exception {
    final Journal journal = Journal.open("test-journal");
    final MessageBus messageBus = MessageBus.start("test-bus");
    final Topic topic1 = messageBus.openTopic("test-topic1");
    final Topic topic2 = messageBus.openTopic("test-topic2");
    JournalPublisher journalPublisher1 = JournalPublisher.using(journal.name(), messageBus.name(), topic1.name());
    JournalPublisher journalPublisher2 = JournalPublisher.using(journal.name(), messageBus.name(), topic2.name());

    final TestSubscriber subscriber1 = new TestSubscriber();
    topic1.subscribe(subscriber1);

    final EntryBatch batch1 = new EntryBatch();
    for (int idx = 0; idx < 3; ++idx) {
      batch1.addEntry("test1type-" + idx, "test1instance" + idx);
    }
    journal.write("test1-1", EntryValue.NO_STREAM_VERSION, batch1);

    subscriber1.waitForExpectedMessages(3);
    topic1.close();
    journalPublisher1.close();

    assertEquals(3, subscriber1.handledMessages.size());
    
    final TestSubscriber subscriber2 = new TestSubscriber();
    topic2.subscribe(subscriber2);

    final EntryBatch batch2 = new EntryBatch();
    for (int idx = 0; idx < 3; ++idx) {
      batch2.addEntry("test2type-" + idx, "test2instance" + idx);
    }
    journal.write("test2-1", EntryValue.NO_STREAM_VERSION, batch2);

    subscriber2.waitForExpectedMessages(6);
    topic2.close();
    journalPublisher2.close();

    assertEquals(6, subscriber2.handledMessages.size());
  }

  @Test
  public void testJournalPublisherForCategory() throws Exception {
    final Journal journal = Journal.open("test-ej-cat");
    final MessageBus messageBus = MessageBus.start("test-bus-cat");
    final Topic topic = messageBus.openTopic("cat-test");
    JournalPublisher journalPublisher = JournalPublisher.using(journal.name(), messageBus.name(), topic.name());

    final TestSubscriber subscriber = new TestSubscriber();
    topic.subscribe(subscriber);

    final EntryBatch batch1 = new EntryBatch();
    for (int idx = 0; idx < 3; ++idx) {
      batch1.addEntry("test1type", "test1instance" + idx);
    }
    journal.write("test_1", EntryValue.NO_STREAM_VERSION, batch1);

    final EntryBatch batch2 = new EntryBatch();
    for (int idx = 0; idx < 3; ++idx) {
      batch2.addEntry("test1type", "test1instance" + idx);
    }
    journal.write("test_2", EntryValue.NO_STREAM_VERSION, batch2);

    subscriber.waitForExpectedMessages(6);
    topic.close();
    journalPublisher.close();

    assertEquals(6, subscriber.handledMessages.size());
  }

  public static class TestSubscriber implements Subscriber {
    public final List<Message> handledMessages = new ArrayList<>();

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
