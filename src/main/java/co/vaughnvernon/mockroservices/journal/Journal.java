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
import java.util.stream.Collectors;

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
      tryCanWrite("", EntryValue.NO_STREAM_VERSION);
      int offset = 1;
      for (final EntryBatch.Entry entry : batch.entries) {
        store.add(new EntryValue("", 0 + offset, entry.type, entry.body, ""));
        offset++;
      }
    }
  }

  public void write(final String streamName, final int streamVersion, final EntryBatch batch) {
    synchronized (store) {
      tryCanWrite(streamName, streamVersion);
      int offset = 1;
      for (final EntryBatch.Entry entry : batch.entries) {
        store.add(new EntryValue(streamName, streamVersion + offset, entry.type, entry.body, entry.snapshot));
        offset++;
      }
    }
  }

  public <T> void write(final Class<T> streamClass, final String streamName, final int streamVersion, final EntryBatch batch) {
    synchronized (store) {
      tryCanWrite(streamName, streamVersion);
      int offset = 1;
      for (final EntryBatch.Entry entry : batch.entries) {
        store.add(new EntryValue(StreamNameBuilder.buildStreamNameFor(streamClass, streamName), streamVersion + offset, entry.type, entry.body, entry.snapshot));
        offset++;
      }
    }
  }

  protected Journal(final String name) {
    this.name = name;
    this.readers = new HashMap<String, JournalReader>();
    this.store = new ArrayList<EntryValue>();
  }
//TODO: maybe
  protected EntryValue entryValueAt(final int id) {
    synchronized (store) {
      if (id >= store.size()) {
        throw new IllegalArgumentException("The id does not exist: " + id);
      }

      return store.get(id);
    }
  }
//TODO: maybe
  protected int greatestId() {
    return store.size() - 1;
  }

  protected EntryStream readStream(final String streamName) {
    synchronized (store) {
      final List<EntryValue> values = new ArrayList<EntryValue>();
      final List<EntryValue> storeCopy = new ArrayList<EntryValue>(store);
      EntryValue latestSnapshotValue = null;

      int globalIndex = 0;
      for (final EntryValue value : storeCopy) {
        if (canRead(value, streamName)) { // if(value.streamName.equals(streamName))
          if (value.hasSnapshot()) {
            values.clear();
            latestSnapshotValue = value;
          } else {
            values.add(isCategoryStream(streamName) ? value.withStreamVersion(globalIndex) : value);
          }
        }
        globalIndex++;
      }

      final int snapshotVersion = latestSnapshotValue == null ? 0 : latestSnapshotValue.streamVersion;
      final int streamVersion = values.isEmpty() ? snapshotVersion : values.get(values.size() - 1).streamVersion;

      return new EntryStream(streamName, streamVersion, values, latestSnapshotValue == null ? "" : latestSnapshotValue.snapshot);
    }
  }

  private void tryCanWrite(String streamName, int expectedStreamVersion) {
    int currentVersion = EntryValue.NO_STREAM_VERSION;
    boolean canWrite = false;
    EntryValue lastEntry = lastOrDefault(streamName);
    if (lastEntry != null) {
      currentVersion = expectedStreamVersion;
      canWrite = lastEntry.streamVersion == expectedStreamVersion;
    } else if (expectedStreamVersion == EntryValue.NO_STREAM_VERSION) {
      currentVersion = EntryValue.NO_STREAM_VERSION;
      canWrite = true;
    }

    if(!canWrite) {
      throw new IndexOutOfBoundsException("Cannot write to stream " + streamName +
      " with expected version " + expectedStreamVersion +
      " because the current version is " + currentVersion);
    }
  }

  private boolean canRead(EntryValue entryValue, String streamName) {
    String maybeCatStreamName = maybeCategoryStreamName(streamName);
    if(isCategoryStream(streamName)) {
      return entryValue.streamName.startsWith(maybeCatStreamName);
    }
    return entryValue.streamName.equals(maybeCatStreamName);
  }

  private String maybeCategoryStreamName(String streamName) {
    if (isCategoryStream(streamName)) {
        return streamName.split("-")[1];
    }
    return streamName;
  }

  private boolean isCategoryStream(String streamName) {
    return streamName.startsWith("cat-");
  }

  private EntryValue lastOrDefault(String streamName) {
    List<EntryValue> filtered = store.stream().filter(e -> e.streamName == streamName).collect(Collectors.toList());
    if(filtered.size()>0) {
      return filtered.get(filtered.size()-1);
    } else {
      return null;
    }
  }
}
