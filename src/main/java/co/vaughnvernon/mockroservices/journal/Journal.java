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

    synchronized (journals) {
      journals.put(name, journal);
    }

    return journal;
  }

  public void close() {
    synchronized (store) {
      store.clear();
      readers.clear();
    }
    synchronized (journals) {
      journals.remove(name);
    }
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
      int offsetingStreamVersion = 1;
      for (final EntryBatch.Entry entry : batch.entries) {
        store.add(new EntryValue("", offsetingStreamVersion++, entry.type, entry.body, ""));
      }
    }
  }

  public void write(final String streamName, final int streamVersion, final EntryBatch batch) {
    synchronized (store) {
      tryCanWrite(streamName, streamVersion);
      int offsetingStreamVersion = streamVersion == EntryValue.NO_STREAM_VERSION ? 1 : streamVersion;
      for (final EntryBatch.Entry entry : batch.entries) {
        store.add(new EntryValue(streamName, offsetingStreamVersion++, entry.type, entry.body, entry.snapshot));
      }
    }
  }

  public <T> void write(final Class<T> streamClass, final String streamName, final int streamVersion, final EntryBatch batch) {
    synchronized (store) {
      tryCanWrite(streamName, streamVersion);
      int offsetingStreamVersion = streamVersion == EntryValue.NO_STREAM_VERSION ? 1 : streamVersion;
      for (final EntryBatch.Entry entry : batch.entries) {
        store.add(new EntryValue(StreamNameBuilder.buildStreamNameFor(streamClass, streamName), offsetingStreamVersion++, entry.type, entry.body, entry.snapshot));
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
      // if (id > greatestId()) {
      // throw new IllegalArgumentException("The id does not exist: " + id);
      // }
      return store.get(id);
    }
  }

  protected int greatestId() {
    return store.size() - 1;
  }

  protected int greatestId(String streamName) {
    synchronized (store) {
      String maybeCatStreamName = maybeCategoryStreamName(streamName);
      if (isCategoryStream(streamName)) {
        return Math.toIntExact(store.stream().filter(e -> e.streamName.startsWith(maybeCatStreamName)).count() - 1);
      }
      return Math.toIntExact(store.stream().filter(e -> e.streamName.equals(maybeCatStreamName)).count() - 1);
    }
  }

  protected EntryStream readStream(final String streamName) {
    synchronized (store) {
      final List<EntryValue> values = new ArrayList<EntryValue>();
      final List<EntryValue> storeCopy = new ArrayList<EntryValue>(store);
      EntryValue latestSnapshotValue = null;

      int globalIndex = 1;
      for (final EntryValue value : storeCopy) {
        if (canRead(value, streamName)) {
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

      return new EntryStream(streamName, streamVersion, values,
              latestSnapshotValue == null ? "" : latestSnapshotValue.snapshot);
    }
  }

  private void tryCanWrite(String streamName, int expectedStreamVersion) {
    int currentVersion = EntryValue.NO_STREAM_VERSION;
    boolean canWrite = false;
    EntryValue lastEntry = lastOrDefault(streamName);
    if (lastEntry != null) {
      currentVersion = expectedStreamVersion;
      canWrite = lastEntry.streamVersion == (expectedStreamVersion - 1);
    } else if (expectedStreamVersion == EntryValue.NO_STREAM_VERSION || expectedStreamVersion == 1) {
      currentVersion = 1;
      canWrite = true;
    }

    if (!canWrite) {
      throw new IndexOutOfBoundsException("Cannot write to stream " + streamName + " with expected version "
              + expectedStreamVersion + " because the current version is " + currentVersion);
    }
  }

  private boolean canRead(EntryValue entryValue, String streamName) {
    String maybeCatStreamName = maybeCategoryStreamName(streamName);
    if (isCategoryStream(streamName)) {
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
    List<EntryValue> filtered = store.stream().filter(e -> e.streamName.equals(streamName))
            .collect(Collectors.toList());
    if (!filtered.isEmpty()) {
      return filtered.get(filtered.size() - 1);
    } else {
      return null;
    }
  }
}
