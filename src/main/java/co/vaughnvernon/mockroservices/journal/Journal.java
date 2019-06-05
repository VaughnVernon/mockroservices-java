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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Journal {
  private static final Map<String, Journal> journals = new HashMap<String, Journal>();

  private final String name;
  private final Map<String, JournalReader> readers;
  private final List<EntryValue> store;

  public static Journal open(final String name) {
    final Journal openJournal = journals.get(name);
    
    if (openJournal != null) {
      return openJournal;
    }
    
    final Journal journal = new Journal(name);

    journals.put(name, journal);

    return journal;
  }

  public void close() {
    store.clear();
    readers.clear();
    journals.remove(name);
  }

  public String name() {
    return name;
  }

  public JournalReader reader(final String name) {
    final JournalReader openReader = readers.get(name);
    
    if (openReader != null) {
      return openReader;
    }
    
    final JournalReader reader = new JournalReader(name, this);
    
    readers.put(name, reader);
    
    return reader;
  }

  public EntryStreamReader streamReader() {
    return new EntryStreamReader(this);
  }

  public void write(final EntryBatch batch) {
    synchronized (store) {
      for (final EntryBatch.Entry entry : batch.entries) {
        store.add(new EntryValue("", 0, entry.type, entry.body, ""));
      }
    }
  }

  public void write(final String streamName, final int streamVersion, final EntryBatch batch) {
    synchronized (store) {
      for (final EntryBatch.Entry entry : batch.entries) {
        store.add(new EntryValue(streamName, streamVersion, entry.type, entry.body, entry.snapshot));
      }
    }
  }

  protected Journal(final String name) {
    this.name = name;
    this.readers = new HashMap<String, JournalReader>();
    this.store = new ArrayList<EntryValue>();
  }

  protected EntryValue entryValueAt(final int id) {
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

  protected EntryStream readStream(final String streamName) {
    synchronized (store) {
      final List<EntryValue> values = new ArrayList<EntryValue>();
      final List<EntryValue> storeCopy = new ArrayList<EntryValue>(store);
      EntryValue latestSnapshotValue = null;
      
      for (final EntryValue value : storeCopy) {
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
      
      return new EntryStream(streamName, streamVersion, values, latestSnapshotValue == null ? "" : latestSnapshotValue.snapshot);
    }
  }
}
