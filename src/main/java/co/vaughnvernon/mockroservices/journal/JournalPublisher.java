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

package co.vaughnvernon.mockroservices.journal;

import java.util.HashMap;
import java.util.Map;

import co.vaughnvernon.mockroservices.messagebus.Message;
import co.vaughnvernon.mockroservices.messagebus.MessageBus;
import co.vaughnvernon.mockroservices.messagebus.Topic;

public class JournalPublisher extends Thread {
  private volatile boolean closed;
  private final Map<String, String> nameTranslationMap;
  private final JournalReader reader;
  private final Topic topic;

  public static JournalPublisher using(
      final String journalName,
      final String messageBusName,
      final String topicName) {

    return new JournalPublisher(journalName, messageBusName, topicName);
  }

  public void close() {
    closed = true;
  }

  @Override
  public void run() {
    while (!closed) {
      final StoredSource source = reader.readNext();

      if (source.isValid()) {
        final String messageType = translate(source.entryValue.type);

        final Message message =
            new Message(
                String.valueOf(source.id),
                messageType,
                source.entryValue.body);

        topic.publish(message);

        reader.acknowledge(source.id);

      } else {
        try {
          Thread.sleep(10L);
        } catch (Exception e) {
          // ignore
        }
      }
    }
  }

  /**
   * Register the {@code fromMessageType} as the original source type and {@code toMessageType}
   * as the translated target message type. The {@code fromMessageType} must be a fully-qualified
   * classname. The {@code toMessageType} may be a fully-qualified classname or the package name
   * only, in which case the package name will be the prefix of the fully-qualified translation
   * and the classname of the {@code fromMessageType} will be appended to the prefixing package name
   * of {@code toMessageType}.
   * 
   * <pre>
   * journalPublisher.registerTranslationMapping(SomeEventName.class.getName(), "com.company.contextname.SomeOtherEventName");
   *   Source example: com.company.contextname.model.aggregatename.events.SomeEventName
   *   Result: "com.company.contextname.SomeOtherClassName"
   *
   * Or:
   * 
   * journalPublisher.registerTranslationMapping(SomeEventName.class.getName(), "com.company.contextname");
   *   Source example: com.company.contextname.model.aggregatename.events.SomeEventName
   *   Result: "com.company.contextname.SomeEventName"
   * </pre>
   * 
   * @param fromMessageType the String fully-qualified classname of the original source type to be translated
   * @param toMessageType the String fully-qualified classname or only the package name that will be prepended to the primary classname of fromMessageType
   */
  public void registerTranslationMapping(String fromMessageType, String toMessageType) {
    nameTranslationMap.put(fromMessageType, toMessageType);
  }

  /**
   * Answer the {@code toMessageType} registered using {@code registerTranslationMapping()}. Primaryily used
   * for testing, but may be used for production.
   * 
   * @param sourceMessageType the String original source message type as a fully-qualified classname
   * @return the String translation as explained by registerTranslationMapping()
   */
  public String translateFrom(String sourceMessageType) {
    return translate(sourceMessageType);
  }

  protected JournalPublisher(
      final String journalName,
      final String messageBusName,
      final String topicName) {

    this.nameTranslationMap = new HashMap<>();
    this.reader = Journal.open(journalName).reader(topicName);
    this.topic = MessageBus.start(messageBusName).openTopic(topicName);

    start();
  }

  private String classnameOf(String typeName) {
    String classname = null;

    int lastDotIndexDollar = typeName.lastIndexOf(".");
    int lastDotIndexDot = typeName.lastIndexOf(".");

    if (lastDotIndexDollar >= 0 || lastDotIndexDot >= 0) {
      int lastIndex = lastDotIndexDollar >= 0 ? lastDotIndexDollar : lastDotIndexDot;
      String lastPartOfTypeName = typeName.substring(lastIndex + 1);

      char firstCharacter = lastPartOfTypeName.length() == 0 ? 0 : lastPartOfTypeName.charAt(0);

      if (Character.isAlphabetic(firstCharacter) && Character.isUpperCase(firstCharacter)) {
        classname = lastPartOfTypeName;
      }
    }

    return classname;
  }

  private boolean hasNoClassname(String translatedTypeName) {
    try {
      Class.forName(translatedTypeName);
      return false;
    } catch (Exception e) {
      return true;
    }
  }

  private String translate(String typeName) {
    String translation = nameTranslationMap.get(typeName);

    if (translation == null) {
      return typeName;
    }

    if (hasNoClassname(translation)) {
      String classname = classnameOf(typeName);
      translation = translation + "." + classname;
    }

    return translation;
  }
}
