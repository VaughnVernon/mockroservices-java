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

package co.vaughnvernon.mockroservices.messagebus;

import java.math.BigDecimal;
import java.util.Date;

import com.google.gson.JsonObject;

import co.vaughnvernon.mockroservices.exchange.InformationExchangeReader;

public class MessageExchangeReader extends InformationExchangeReader {
  private JsonObject payload;
  
  public static MessageExchangeReader from(final String jsonRepresentation) {
    return new MessageExchangeReader(jsonRepresentation);
  }
  
  public static MessageExchangeReader from(final JsonObject jsonRepresentation) {
    return new MessageExchangeReader(jsonRepresentation);
  }
  
  public MessageExchangeReader(final String jsonRepresentation) {
    super(jsonRepresentation);
    payload = parse(stringValue(representation(), "payload"));
  }

  public MessageExchangeReader(JsonObject jsonRepresentation) {
    super(jsonRepresentation);
    payload = parse(stringValue(representation(), "payload"));
  }

  //==============================================
  // message header
  //==============================================
  
  public String id() {
    String id = stringValue("id");
    return id;
  }

  public long idAsLong() {
    String id = stringValue("id");
    return Long.parseLong(id);
  }

  public String type() {
    String type = stringValue("type");
    return type;
  }
  
  //==============================================
  // message payload
  //==============================================

  public BigDecimal payloadBigDecimalValue(final String... keys) {
    String stringValue = stringValue(payload, keys);
    return stringValue == null ? null : new BigDecimal(stringValue);
  }

  public Boolean payloadBooleanValue(final String... keys) {
    String stringValue = stringValue(payload, keys);
    return stringValue == null ? null : Boolean.parseBoolean(stringValue);
  }

  public Date payloadDateValue(final String... keys) {
    String stringValue = stringValue(payload, keys);
    return stringValue == null ? null : new Date(Long.parseLong(stringValue));
  }

  public Double payloadDoubleValue(final String... keys) {
    String stringValue = stringValue(payload, keys);
    return stringValue == null ? null : Double.parseDouble(stringValue);
  }

  public Float payloadFloatValue(final String... keys) {
    String stringValue = stringValue(payload, keys);
    return stringValue == null ? null : Float.parseFloat(stringValue);
  }

  public Integer payloadIntegerValue(final String... keys) {
    String stringValue = stringValue(payload, keys);
    return stringValue == null ? null : Integer.parseInt(stringValue);
  }

  public Long payloadLongValue(final String... keys) {
    String stringValue = stringValue(payload, keys);
    return stringValue == null ? null : Long.parseLong(stringValue);
  }

  public String payloadStringValue(final String... keys) {
    String stringValue = stringValue(payload, keys);
    return stringValue;
  }
}
