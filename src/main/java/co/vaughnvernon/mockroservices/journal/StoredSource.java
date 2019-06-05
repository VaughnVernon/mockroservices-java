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

public final class StoredSource {
  public static final long NO_ID = -1L;
  
  public final EntryValue entryValue;
  public final long id;

  public boolean isValid() {
    return id != NO_ID;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(id) + entryValue.hashCode();
  }

  @Override
  public boolean equals(final Object other) {
    if (other == null || other.getClass() != StoredSource.class) {
      return false;
    }

    final StoredSource otherStoredSource = (StoredSource) other;
    
    return this.id == otherStoredSource.id && this.entryValue.equals(otherStoredSource.entryValue);
  }

  @Override
  public String toString() {
    return "StoredSource[id=" + id + " entryValue=" + entryValue + "]";
  }

  protected StoredSource(final long id, final EntryValue entryValue) {
    this.id = id;
    this.entryValue = entryValue;
  }
}
