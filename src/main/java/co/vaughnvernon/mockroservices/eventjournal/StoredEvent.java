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

public final class StoredEvent {
  public final EventValue eventValue;
  public final long id;
  
  @Override
  public int hashCode() {
    return Long.hashCode(id) + eventValue.hashCode();
  }

  @Override
  public boolean equals(final Object other) {
    if (other == null || other.getClass() != StoredEvent.class) {
      return false;
    }

    final StoredEvent otherStoredEvent = (StoredEvent) other;
    
    return this.id == otherStoredEvent.id && this.eventValue.equals(otherStoredEvent.eventValue);
  }

  @Override
  public String toString() {
    return "StoredEvent[id=" + id + " eventValue=" + eventValue + "]";
  }

  protected StoredEvent(final long id, final EventValue eventValue) {
    this.id = id;
    this.eventValue = eventValue;
  }
}
