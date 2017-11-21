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

public final class EventBatch {
  public final List<Entry> entries;
  
  public static EventBatch of(final String type, final String body) {
    return new EventBatch(type, body);
  }
  
  public static EventBatch of(final String type, final String body, final String snapshot) {
    return new EventBatch(type, body, snapshot);
  }
  
  public EventBatch() {
    this.entries = new ArrayList<>();
  }
  
  public EventBatch(final String type, final String body) {
    this(type, body, "");
  }
  
  public EventBatch(final String type, final String body, final String snapshot) {
    this();
    
    this.addEntry(type, body, snapshot);
  }
  
  public void addEntry(final String type, final String body) {
    this.entries.add(new Entry(type, body));
  }
  
  public void addEntry(final String type, final String body, final String snapshot) {
    this.entries.add(new Entry(type, body, snapshot));
  }
  
  public final class Entry {
    public final String body;
    public final String type;
    public final String snapshot;
    
    private Entry(final String type, final String body) {
      this(type, body, "");
    }
    
    private Entry(final String type, final String body, final String snapshot) {
      this.type = type;
      this.body = body;
      this.snapshot = snapshot;
    }
  }
}
