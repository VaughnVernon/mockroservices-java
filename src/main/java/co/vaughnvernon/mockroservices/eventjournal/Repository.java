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

import co.vaughnvernon.mockroservices.serialization.Serialization;

public abstract class Repository {
  protected <T> EventBatch toBatch(final List<T> sources) {
    final EventBatch batch = new EventBatch(sources.size());
    for (final T source : sources) {
      final String eventType = source.getClass().getName();
      final String eventBody = Serialization.serialize(source);
      batch.addEntry(eventType, eventBody);
    }
    return batch;
  }
  
  @SuppressWarnings("unchecked")
  protected <T> List<T> toSourceStream(final List<EventValue> stream) {
    final List<T> sourceStream = new ArrayList<>(stream.size());
    try {
      for (final EventValue value : stream) {
        final Class<T> type = (Class<T>) Class.forName(value.type);
        final T source = Serialization.deserialize(value.body, type);
        sourceStream.add(source);
      }
    } catch (Exception e) {
      // TODO: handle
    }
    return sourceStream;
  }
}
