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

public class EventValue {
  public final String body;
  public final String snapshot;
  public final String streamName;
  public final int streamVersion;
  public final String type;

  public boolean hasSnapshot() {
    return !snapshot.isEmpty();
  }
  
  @Override
  public int hashCode() {
    return this.streamName.hashCode() + Integer.hashCode(this.streamVersion);
  }

  @Override
  public boolean equals(Object other) {
    if (other == null || other.getClass() != EventValue.class) {
      return false;
    }
    
    final EventValue otherEventValue = (EventValue) other;
    
    return this.streamName.equals(otherEventValue.streamName) &&
        this.streamVersion == otherEventValue.streamVersion &&
        this.type.equals(otherEventValue.type) &&
        this.body.equals(otherEventValue.body) &&
        this.snapshot.equals(otherEventValue.snapshot);
  }

  @Override
  public String toString() {
    return "EventValue[streamName=" + streamName + " streamVersion=" + streamVersion +
        " type=" + type + " body=" + body + " snapshot=" + snapshot + "]";
  }

  protected EventValue(
      final String streamName,
      final int streamVersion,
      final String type,
      final String body,
      final String snapshot) {
    
    this.streamName = streamName;
    this.streamVersion = streamVersion;
    this.type = type;
    this.body = body;
    this.snapshot = snapshot;
  }
}
