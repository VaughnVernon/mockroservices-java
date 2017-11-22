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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Topic extends Thread {
  private boolean closed;
  private final String name;
  private final ConcurrentLinkedQueue<Message> queue;
  private final Set<Subscriber> subscribers;

  public void close() {
    while (queue.size() > 0) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // ignore
      }
    }
    
    closed = true;
  }

  public String name() {
    return name;
  }

  public void publish(final Message message) {
    queue.add(message);
  }

  public void subscribe(final Subscriber subscriber) {
    synchronized (subscribers) {
      subscribers.add(subscriber);

      if (subscribers.size() == 1) {
          start();
      }
    }
  }

  @Override
  public void run() {
    while (!closed) {
      final Message message = queue.poll();
      
      if (message != null) {
        synchronized (subscribers) {
          for (final Subscriber subscriber : subscribers) {
            try {
              subscriber.handle(message);
            } catch (Exception e) {
              System.out.println("MessageBus/Topic: " + name + " error: " + e.getMessage());
              e.printStackTrace();
            }
          }
        }
      } else {
        try {
          Thread.sleep(100L);
        } catch (InterruptedException e) {
          // ignore
        }
      }
    }
  }

  protected Topic(final String name) {
    this.name = name;
    this.closed = false;
    this.queue = new ConcurrentLinkedQueue<>();
    this.subscribers = new HashSet<Subscriber>();
  }
}
