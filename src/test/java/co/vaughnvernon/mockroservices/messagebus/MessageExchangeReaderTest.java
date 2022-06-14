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

package co.vaughnvernon.mockroservices.messagebus;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import co.vaughnvernon.mockroservices.model.TestableDomainEvent;
import co.vaughnvernon.mockroservices.serialization.Serialization;

public class MessageExchangeReaderTest {

  @Test
  public void testExchangeRoundTrip() throws Exception {
    final TestableDomainEvent domainEvent = new TestableDomainEvent(1, "something");
    final String serializedDomainEvent = Serialization.serialize(domainEvent);
    final Message eventMessage = new Message(String.valueOf(domainEvent.id), domainEvent.getClass().getName(), serializedDomainEvent);
    final MessageExchangeReader reader = MessageExchangeReader.from(eventMessage);
    assertEquals(eventMessage.id, reader.id());
    assertEquals(eventMessage.type, reader.type());
    assertEquals(domainEvent.id, (long) reader.payloadLongValue("id"));
    assertEquals(domainEvent.name, reader.payloadStringValue("name"));
    assertEquals(domainEvent.eventVersion, (int) reader.payloadIntegerValue("eventVersion"));
    assertEquals((Long)domainEvent.occurredOn, reader.payloadLongValue("occurredOn"));
    assertEquals((Long)domainEvent.validOn, reader.payloadLongValue("validOn"));
  }
}
