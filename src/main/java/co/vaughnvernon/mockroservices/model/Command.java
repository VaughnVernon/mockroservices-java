//   Copyright © 2017-2022 Vaughn Vernon. All rights reserved.
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

public abstract class Command implements SourceType {
  public final long occurredOn;
  public final long validOn;
  public final int commandVersion;

  public static Command NULL = new NullCommand();

  public static List<Command> all(final Command... commands) {
    return all(Arrays.asList(commands));
  }

  public static List<Command> all(final List<Command> commands) {
    final List<Command> all = new ArrayList<>(commands.size());

    for (final Command command : commands) {
      if (!command.isNull()) {
        all.add(command);
      }
    }
    return all;
  }

  public static List<Command> none() {
    return Collections.emptyList();
  }

  public boolean isNull() {
    return false;
  }

  protected Command() {
    this(1);
  }

  protected Command(final int commandVersion) {
    this.occurredOn = System.currentTimeMillis();
    this.validOn = System.currentTimeMillis();
    this.commandVersion = commandVersion;
  }

  protected Command(final long validOn, final int commandVersion) {
    this.occurredOn = System.currentTimeMillis();
    this.validOn = validOn;
    this.commandVersion = commandVersion;
  }

  private static class NullCommand extends Command {
    @Override
    public boolean isNull() {
      return true;
    }
  }
}
