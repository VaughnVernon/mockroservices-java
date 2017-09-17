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

package co.vaughnvernon.mockroservices.model;

import java.util.Date;

public class TestableDomainEvent implements DomainEvent {

  public final int eventVersion;
  public final long id;
  public final String name;
  public final Date occurredOn;

  public TestableDomainEvent(final long id, final String name) {
    this.id = id;
    this.name = name;
    this.eventVersion = 1;
    this.occurredOn = new Date();
  }

  public int eventVersion() {
    return eventVersion;
  }

  public Date occurredOn() {
    return occurredOn;
  }
}
