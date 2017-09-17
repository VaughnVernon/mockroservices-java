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

public class EventJournalReader {
  private final EventJournal eventJournal;
  private final String name;
  private int readSequence;
  
  public void acknowledge(final int id) {
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

  public StoredEvent readNext() {
    if (readSequence <= eventJournal.greatestId()) {
      return new StoredEvent(readSequence, eventJournal.eventValueAt(readSequence));
    }

    return new StoredEvent(-1, new EventValue("", -1, "", "", ""));
  }

  public void reset() {
    readSequence = 0;
  }

  protected EventJournalReader(final String name, final EventJournal eventJournal) {
    this.name = name;
    this.eventJournal = eventJournal;
    this.readSequence = 0;
  }
}
