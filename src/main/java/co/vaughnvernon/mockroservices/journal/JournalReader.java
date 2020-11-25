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

public class JournalReader {
  private final Journal journal;
  private final String name;
  private int readSequence;

  public void acknowledge(final long id) {
    if (id == readSequence) {
        ++readSequence;
    } else if (id > readSequence) {
        throw new IllegalArgumentException("The id is out of range.");
    } else if (id < readSequence) {
        // allow for this case; don't throw IllegalArgumentException("The id has already been acknowledged.");
    }
  }

  public String name() {
    return name;
  }

  public StoredSource readNext() {
    if (readSequence <= journal.greatestId()) {
      return new StoredSource(readSequence, journal.entryValueAt(readSequence));
    }

    return new StoredSource(StoredSource.NO_ID, new EntryValue("", EntryValue.NO_STREAM_VERSION, "", "", ""));
  }

  public void reset() {
    readSequence = 0;
  }

  protected JournalReader(final String name, final Journal journal) {
    this.name = name;
    this.journal = journal;
    this.readSequence = 0;
  }
}
