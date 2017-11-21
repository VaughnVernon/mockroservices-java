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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class EventSourcedRootEntity {
  private static final String MUTATOR_METHOD_NAME = "when";
  private static final Map<String, Method> mutatorMethods = new HashMap<String, Method>();

  private final List<DomainEvent> mutatingEvents;
  private final int unmutatedVersion;

  public int mutatedVersion() {
    return unmutatedVersion() + 1;
  }

  public List<DomainEvent> mutatingEvents() {
    return mutatingEvents;
  }

  public int unmutatedVersion() {
    return unmutatedVersion;
  }

  protected EventSourcedRootEntity(final List<DomainEvent> eventStream, final int streamVersion) {
    this.mutatingEvents = new ArrayList<DomainEvent>(2);
    this.unmutatedVersion = streamVersion;

    for (final DomainEvent event : eventStream) {
      mutateWhen(event);
    }
  }

  protected EventSourcedRootEntity() {
    this.mutatingEvents = new ArrayList<DomainEvent>(2);
    this.unmutatedVersion = 0;
  }

  protected void apply(final DomainEvent... domainEvents) {
    for (final DomainEvent domainEvent : domainEvents) {
      mutatingEvents().add(domainEvent);
      mutateWhen(domainEvent);
    }
  }

  protected void apply(final Collection<DomainEvent> domainEvents) {
    for (final DomainEvent domainEvent : domainEvents) {
      mutatingEvents().add(domainEvent);
      mutateWhen(domainEvent);
    }
  }

  protected void mutateWhen(final DomainEvent domainEvent) {
    final Class<? extends EventSourcedRootEntity> rootType = this.getClass();
    final Class<? extends DomainEvent> eventType = domainEvent.getClass();
    final String key = rootType.getName() + ":" + eventType.getName();
    Method mutatorMethod = mutatorMethods.get(key);

    if (mutatorMethod == null) {
      mutatorMethod = this.cacheMutatorMethodFor(key, rootType, eventType);
    }

    try {
      mutatorMethod.invoke(this, domainEvent);
    } catch (InvocationTargetException e) {
      if (e.getCause() != null) {
        throw new RuntimeException("Method " + MUTATOR_METHOD_NAME + "(" + eventType.getSimpleName()
            + ") failed. See cause: " + e.getMessage(), e.getCause());
      }

      throw new RuntimeException(
          "Method " + MUTATOR_METHOD_NAME + "(" + eventType.getSimpleName() + ") failed. See cause: " + e.getMessage(),
          e);

    } catch (IllegalAccessException e) {
      throw new RuntimeException("Method " + MUTATOR_METHOD_NAME + "(" + eventType.getSimpleName()
          + ") failed because of illegal access. See cause: " + e.getMessage(), e);
    }
  }

  private Method cacheMutatorMethodFor(
      final String key,
      final Class<? extends EventSourcedRootEntity> rootType,
      final Class<? extends DomainEvent> eventType) {

    synchronized (mutatorMethods) {
      try {
        final Method method = this.hiddenOrPublicMethod(rootType, eventType);

        method.setAccessible(true);

        mutatorMethods.put(key, method);

        return method;

      } catch (Exception e) {
        throw new IllegalArgumentException("I do not understand " + MUTATOR_METHOD_NAME + "("
            + eventType.getSimpleName() + ") because: " + e.getClass().getSimpleName() + ">>>" + e.getMessage(), e);
      }
    }
  }

  private Method hiddenOrPublicMethod(
      final Class<? extends EventSourcedRootEntity> rootType,
      final Class<? extends DomainEvent> eventType)
  throws Exception {

    Method method = null;

    try {
      // assume protected or private...
      method = rootType.getDeclaredMethod(MUTATOR_METHOD_NAME, eventType);
    } catch (Exception e) {
      // then public...
      method = rootType.getMethod(MUTATOR_METHOD_NAME, eventType);
    }

    return method;
  }
}
