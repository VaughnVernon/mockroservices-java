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

import java.util.HashMap;
import java.util.Map;

public class MessageBus {
  private static final Map<String, MessageBus> messageBuses = new HashMap<String, MessageBus>();

  public final String name;
  private final Map<String, Topic> topics;

  public static synchronized MessageBus start(final String name) {
    final MessageBus openMessageBus = messageBuses.get(name);
    if (openMessageBus != null) {
      return openMessageBus;
    }

    final MessageBus messageBus = new MessageBus(name);
    messageBuses.put(name, messageBus);

    return messageBus;
  }

  public String name() {
    return name;
  }

  public Topic openTopic(final String name) {
    synchronized (topics) {
      final Topic openTopic = topics.get(name);
      if (openTopic != null) {
        return openTopic;
      }

      final Topic topic = new Topic(name);
      topics.put(name, topic);

      return topic;
    }
  }

  protected MessageBus(final String name) {
    this.name = name;
    this.topics = new HashMap<String, Topic>();
  }
}
