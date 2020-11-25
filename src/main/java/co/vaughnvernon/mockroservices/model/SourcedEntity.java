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

import co.vaughnvernon.mockroservices.journal.EntryValue;

public abstract class SourcedEntity<Source> {
  private static final String MUTATOR_METHOD_NAME = "when";
  private static final Map<String, Method> mutatorMethods = new HashMap<String, Method>();

  //readonly
  public final List<Source> applied;
  public final int currentVersion;

  public List<Source> applied() {
    return applied;
  }

  public int currentVersion() {
    return currentVersion;
  }

  public int nextVersion() {
    return currentVersion() + 1;
  }

  protected SourcedEntity() {
    this.applied = new ArrayList<>(2);
    this.currentVersion = EntryValue.NO_STREAM_VERSION;
  }

  protected SourcedEntity(final List<Source> stream, final int streamVersion) {
    this.applied = new ArrayList<Source>(2);
    this.currentVersion = streamVersion;

    for (final Source source : stream) {
      mutateWhen(source);
    }
  }

  @SuppressWarnings("unchecked")
  protected void apply(final Source... sources) {
    for (final Source source : sources) {
      applied.add(source);
      mutateWhen(source);
    }
  }

  protected void apply(final Collection<Source> sources) {
    for (final Source source : sources) {
      applied.add(source);
      mutateWhen(source);
    }
  }

  protected void mutateWhen(final Source source) {
    final Class<?> rootType = this.getClass();
    final Class<?> sourceType = source.getClass();
    final String key = rootType.getName() + ":" + sourceType.getName();
    Method mutatorMethod = mutatorMethods.get(key);

    if (mutatorMethod == null) {
      mutatorMethod = this.cacheMutatorMethodFor(key, rootType, sourceType);
    }

    try {
      mutatorMethod.invoke(this, source);
    } catch (InvocationTargetException e) {
      if (e.getCause() != null) {
        throw new RuntimeException("Method " + MUTATOR_METHOD_NAME + "(" + sourceType.getSimpleName()
            + ") failed. See cause: " + e.getMessage(), e.getCause());
      }

      throw new RuntimeException(
          "Method " + MUTATOR_METHOD_NAME + "(" + sourceType.getSimpleName() + ") failed. See cause: " + e.getMessage(),
          e);

    } catch (IllegalAccessException e) {
      throw new RuntimeException("Method " + MUTATOR_METHOD_NAME + "(" + sourceType.getSimpleName()
          + ") failed because of illegal access. See cause: " + e.getMessage(), e);
    }
  }

  private Method cacheMutatorMethodFor(
      final String key,
      final Class<?> rootType,
      final Class<?> sourceType) {

    synchronized (mutatorMethods) {
      try {
        final Method method = this.hiddenOrPublicMethod(rootType, sourceType);

        method.setAccessible(true);

        mutatorMethods.put(key, method);

        return method;

      } catch (Exception e) {
        throw new IllegalArgumentException("I do not understand " + MUTATOR_METHOD_NAME + "("
            + sourceType.getSimpleName() + ") because: " + e.getClass().getSimpleName() + ">>>" + e.getMessage(), e);
      }
    }
  }

  private Method hiddenOrPublicMethod(
      final Class<?> rootType,
      final Class<?> sourceType)
  throws Exception {

    Method method = null;

    try {
      // assume protected or private...
      method = rootType.getDeclaredMethod(MUTATOR_METHOD_NAME, sourceType);
    } catch (Exception e) {
      // then public...
      method = rootType.getMethod(MUTATOR_METHOD_NAME, sourceType);
    }

    return method;
  }
}
