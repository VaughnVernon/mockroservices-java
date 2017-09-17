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

package co.vaughnvernon.mockroservices.serialization;

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class Serialization {
  private final static Gson gson;
  
  static {
    gson = new GsonBuilder()
        .registerTypeAdapter(Date.class, new DateSerializer())
        .registerTypeAdapter(Date.class, new DateDeserializer()).create();
  }

  public static <T extends Object> T deserialize(String serialization, final Class<T> type) {
    T instance = gson.fromJson(serialization, type);
    return instance;
  }

  public static <T extends Object> T deserialize(String serialization, final Type type) {
    T instance = gson.fromJson(serialization, type);
    return instance;
  }

  public static String serialize(final Object instance) {
    final String serialization = gson.toJson(instance);
    return serialization;
  }

  private static class DateSerializer implements JsonSerializer<Date> {
    public JsonElement serialize(Date source, Type typeOfSource, JsonSerializationContext context) {
        return new JsonPrimitive(Long.toString(source.getTime()));
    }
  }

  private static class DateDeserializer implements JsonDeserializer<Date> {
    public Date deserialize(JsonElement json, Type typeOfTarget, JsonDeserializationContext context) throws JsonParseException {
        long time = Long.parseLong(json.getAsJsonPrimitive().getAsString());
        return new Date(time);
    }
  }
}
