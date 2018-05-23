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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventJournal {
  private static final Map<String, EventJournal> eventJournals = new HashMap<String, EventJournal>();

  private final String name;
  private final Map<String, EventJournalReader> readers;
  private final List<EventValue> store;

  public static EventJournal open(final String name) {
    final EventJournal openJournal = eventJournals.get(name);
    
    if (openJournal != null) {
      return openJournal;
    }
    
    final EventJournal eventJournal = new EventJournal(name);

    eventJournals.put(name, eventJournal);

    return eventJournal;
  }

  public void close() {
    store.clear();
    readers.clear();
    eventJournals.remove(name);
  }

  public String name() {
    return name;
  }

  public EventJournalReader reader(final String name) {
    final EventJournalReader openReader = readers.get(name);
    
    if (openReader != null) {
      return openReader;
    }
    
    final EventJournalReader reader = new EventJournalReader(name, this);
    
    readers.put(name, reader);
    
    return reader;
  }

  public EventStreamReader streamReader() {
    return new EventStreamReader(this);
  }

  public void write(final EventBatch batch) {
    synchronized (store) {
      for (final EventBatch.Entry entry : batch.entries) {
        store.add(new EventValue("", 0, entry.type, entry.body, ""));
      }
    }
  }

  public void write(final String streamName, final int streamVersion, final EventBatch batch) {
    synchronized (store) {
      for (final EventBatch.Entry entry : batch.entries) {
        store.add(new EventValue(streamName, streamVersion, entry.type, entry.body, entry.snapshot));
      }
    }
  }

  protected EventJournal(final String name) {
    this.name = name;
    this.readers = new HashMap<String, EventJournalReader>();
    this.store = new ArrayList<EventValue>();
  }

  protected EventValue eventValueAt(final int id) {
    synchronized (store) {
      if (id >= store.size()) {
        throw new IllegalArgumentException("The id does not exist: " + id);
      }
      
      return store.get(id);
    }
  }

  protected int greatestId() {
    return store.size() - 1;
  }

  protected EventStream readStream(final String streamName) {
    synchronized (store) {
      final List<EventValue> values = new ArrayList<EventValue>();
      final List<EventValue> storeCopy = new ArrayList<EventValue>(store);
      EventValue latestSnapshotValue = null;
      
      for (final EventValue value : storeCopy) {
        if (value.streamName.equals(streamName)) {
          if (value.hasSnapshot()) {
            values.clear();
            latestSnapshotValue = value;
          } else {
            values.add(value);
          }
        }
      }
      
      final int snapshotVersion = latestSnapshotValue == null ? 0 : latestSnapshotValue.streamVersion;
      final int streamVersion = values.isEmpty() ? snapshotVersion : values.get(values.size() - 1).streamVersion;
      
      return new EventStream(streamName, streamVersion, values, latestSnapshotValue == null ? "" : latestSnapshotValue.snapshot);
    }
  }
}
