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

package co.vaughnvernon.mockroservices.eventjournal;

import java.util.ArrayList;
import java.util.List;

import co.vaughnvernon.mockroservices.model.DomainEvent;
import co.vaughnvernon.mockroservices.serialization.Serialization;

public abstract class Repository {
  protected EventBatch toBatch(final List<DomainEvent> domainEvents) {
    final EventBatch batch = new EventBatch(domainEvents.size());
    for (final DomainEvent domainEvent : domainEvents) {
      final String eventType = domainEvent.getClass().getName();
      final String eventBody = Serialization.serialize(domainEvent);
      batch.addEntry(eventType, eventBody);
    }
    return batch;
  }
  
  @SuppressWarnings("unchecked")
  protected List<DomainEvent> toEvents(final List<EventValue> stream) {
    final List<DomainEvent> eventStream = new ArrayList<>(stream.size());
    try {
      for (final EventValue value : stream) {
        final Class<DomainEvent> type = (Class<DomainEvent>) Class.forName(value.type);
        final DomainEvent event = Serialization.deserialize(value.body, type);
        eventStream.add(event);
      }
    } catch (Exception e) {
      // TODO: handle
    }
    return eventStream;
  }
}
