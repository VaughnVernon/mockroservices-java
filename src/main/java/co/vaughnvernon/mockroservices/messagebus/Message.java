//   Copyright � 2017-2022 Vaughn Vernon. All rights reserved.
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

package co.vaughnvernon.mockroservices.messagebus;

import co.vaughnvernon.mockroservices.serialization.Serialization;

public class Message {
  public final String id;
  public final String payload;
  public final String type;
  
  public Message(final String id, final String type, final String payload) {
    this.id = id;
    this.type = type;
    this.payload = payload;
  }

  public String simpleTypeName() {
    return simpleTypeName(".");
  }

  public String simpleTypeName(String separator) {
    int index = type.lastIndexOf(separator);

    if (index == -1) {
      return type;
    }

    return type.substring(index + 1);
  }

  public <T> T typed(Class<T> type) {
    T typedMessage = Serialization.deserialize(payload, type);

    return typedMessage;
  }
}
