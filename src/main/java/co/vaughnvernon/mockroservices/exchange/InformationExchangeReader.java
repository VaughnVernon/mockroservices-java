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

package co.vaughnvernon.mockroservices.exchange;

import java.math.BigDecimal;
import java.util.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class InformationExchangeReader {
  private JsonObject representation;

  public InformationExchangeReader(final String jsonRepresentation) {
    representation = parse(jsonRepresentation);
  }

  public InformationExchangeReader(final JsonObject jsonRepresentation) {
    this.representation = jsonRepresentation;
  }

  public JsonArray array(final String... keys) {
    JsonArray array = null;

    JsonElement element = navigateTo(representation(), keys);

    if (element != null) {
      array = element.getAsJsonArray();
    }

    return array;
  }

  public BigDecimal bigDecimalValue(final String... keys) {
    final String stringValue = stringValue(keys);

    return stringValue == null ? null : new BigDecimal(stringValue);
  }

  public Boolean booleanValue(final String... keys) {
    final String stringValue = stringValue(keys);

    return stringValue == null ? null : Boolean.parseBoolean(stringValue);
  }

  public Date dateValue(final String... keys) {
    final String stringValue = stringValue(keys);

    return stringValue == null ? null : new Date(Long.parseLong(stringValue));
  }

  public Double doubleValue(final String... keys) {
    final String stringValue = stringValue(keys);

    return stringValue == null ? null : Double.parseDouble(stringValue);
  }

  public Float floatValue(final String... keys) {
    final String stringValue = stringValue(keys);

    return stringValue == null ? null : Float.parseFloat(stringValue);
  }

  public Integer integerValue(final String... keys) {
    final String stringValue = stringValue(keys);

    return stringValue == null ? null : Integer.parseInt(stringValue);
  }

  public Long longValue(final String... keys) {
    final String stringValue = stringValue(keys);

    return stringValue == null ? null : Long.parseLong(stringValue);
  }

  public String stringValue(final String... keys) {
    return stringValue(representation(), keys);
  }

  public String[] stringArrayValue(final String... keys) {
    final JsonArray array = array(keys);

    if (array != null) {
      final int size = array.size();
      final String[] stringArray = new String[size];
      for (int idx = 0; idx < size; ++idx) {
        stringArray[idx] = array.get(idx).getAsString();
      }
      return stringArray;
    }
    return new String[0];
  }

  //==============================================
  // internal implementation
  //==============================================

  protected JsonElement elementFrom(final JsonObject jsonObject, final String key) {
    JsonElement element = jsonObject.get(key);

    if (element == null) {
      element = jsonObject.get("@" + key);
    }

    return element;
  }

  protected JsonElement navigateTo(final JsonObject startingJsonObject, final String... keys) {
    if (keys.length == 0) {
      throw new IllegalArgumentException("Must specify one or more keys.");
    }

    int keyIndex = 1;

    JsonElement element = elementFrom(startingJsonObject, keys[0]);

    if (!element.isJsonNull() && !element.isJsonPrimitive() && !element.isJsonArray()) {
      JsonObject object = element.getAsJsonObject();

      for ( ; element != null && !element.isJsonPrimitive() && keyIndex < keys.length; ++keyIndex) {

        element = elementFrom(object, keys[keyIndex]);

        if (!element.isJsonPrimitive()) {

          element = elementFrom(object, keys[keyIndex]);

          if (element.isJsonNull()) {
            element = null;
          } else {
            object = element.getAsJsonObject();
          }
        }
      }
    }

    if (element != null) {
      if (!element.isJsonNull()) {
        if (keyIndex != keys.length) {
          throw new IllegalArgumentException("Last name must reference a simple value.");
        }
      } else {
        element = null;
      }
    }

    return element;
  }

  protected JsonObject parse(final String jsonRepresentation) {
    try {
      JsonParser parser = new JsonParser();
      final JsonObject jsonObject = parser.parse(jsonRepresentation).getAsJsonObject();
      return jsonObject;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  protected JsonObject representation() {
    return representation;
  }

  protected String stringValue(final JsonObject startingJsonObject, final String... keys) {
    String value = null;

    JsonElement element = navigateTo(startingJsonObject, keys);

    if (element != null) {
      value = element.getAsString();
    }

    return value;
  }
}
