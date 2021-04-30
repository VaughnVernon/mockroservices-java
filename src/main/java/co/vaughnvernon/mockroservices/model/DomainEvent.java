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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class DomainEvent implements SourceType {
  public final long occurredOn;
  public final long validOn;
  public final int eventVersion;

  public static DomainEvent NULL = new NullDomainEvent();

  public static List<DomainEvent> all(final DomainEvent... domainEvents) {
    return all(Arrays.asList(domainEvents));
  }

  public static List<DomainEvent> all(final List<DomainEvent> domainEvents) {
    final List<DomainEvent> all = new ArrayList<>(domainEvents.size());

    for (final DomainEvent domainEvent : domainEvents) {
      if (!domainEvent.isNull()) {
        all.add(domainEvent);
      }
    }
    return all;
  }

  public static List<DomainEvent> none() {
    return Collections.emptyList();
  }

  public boolean isNull() {
    return false;
  }

  protected DomainEvent() {
    this(1);
  }

  protected DomainEvent(final int eventVersion) {
    this.occurredOn = System.currentTimeMillis();
    this.validOn = System.currentTimeMillis();
    this.eventVersion = eventVersion;
  }

  protected DomainEvent(final long validOn, final int eventVersion) {
    this.occurredOn = System.currentTimeMillis();
    this.validOn = validOn;
    this.eventVersion = eventVersion;
  }

  private static class NullDomainEvent extends DomainEvent {
    @Override
    public boolean isNull() {
      return true;
    }
  }
}
