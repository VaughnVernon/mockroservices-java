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

import java.util.ArrayList;
import java.util.List;

import co.vaughnvernon.mockroservices.model.DomainEvent;
import co.vaughnvernon.mockroservices.serialization.Serialization;

public abstract class Repository {
  protected <T> EntryBatch toBatch(final List<T> sources) {
    final EntryBatch batch = new EntryBatch(sources.size());
    for (final T source : sources) {
      final String sourceType = source.getClass().getName();
      final String sourceBody = Serialization.serialize(source);
      batch.addEntry(sourceType, sourceBody);
    }
    return batch;
  }

  protected <T extends DomainEvent> List<T> toSourceStream(final List<EntryValue> stream) {
    return toSourceStream(stream, Long.MAX_VALUE);
  }

  @SuppressWarnings("unchecked")
  protected <T extends DomainEvent> List<T> toSourceStream(final List<EntryValue> stream, long validOn) {
    final List<T> sourceStream = new ArrayList<>(stream.size());
    try {
      for (final EntryValue value : stream) {
        final Class<T> type = (Class<T>) Class.forName(value.type);
        final T source = Serialization.deserialize(value.body, type);
        if(source.validOn <= validOn) {
          sourceStream.add(source);
        }
      }
    } catch (Exception e) {
      // TODO: handle
    }
    sourceStream.sort((s1, s2) -> Long.compare(s1.validOn, s2.validOn));
    return sourceStream;
  }
}
