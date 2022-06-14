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

import co.vaughnvernon.mockroservices.messagebus.Message;
import co.vaughnvernon.mockroservices.messagebus.MessageBus;
import co.vaughnvernon.mockroservices.messagebus.Topic;

public class JournalPublisher extends Thread {
  private volatile boolean closed;
  private final JournalReader reader;
  private final Topic topic;

  public static JournalPublisher using(
      final String journalName,
      final String messageBusName,
      final String topicName) {

    return new JournalPublisher(journalName, messageBusName, topicName);
  }

  public void close() {
    closed = true;
  }

  @Override
  public void run() {
    while (!closed) {
      final StoredSource source = reader.readNext();

      if (source.isValid()) {
        final Message message =
            new Message(
                String.valueOf(source.id),
                source.entryValue.type,
                source.entryValue.body);

        topic.publish(message);

        reader.acknowledge(source.id);

      } else {
        try {
          Thread.sleep(10L);
        } catch (Exception e) {
          // ignore
        }
      }
    }
  }

  protected JournalPublisher(
      final String journalName,
      final String messageBusName,
      final String topicName) {

    this.reader = Journal.open(journalName).reader(topicName);
    this.topic = MessageBus.start(messageBusName).openTopic(topicName);

    start();
  }
}
