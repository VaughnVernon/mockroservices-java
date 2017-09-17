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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Test;

import co.vaughnvernon.mockroservices.serialization.Serialization;

public class SerializationTest {

  @Test
  public void testSerializationDeserialization() throws Exception {
    final Person person1 = new Person("First Middle Last, Jr.", new Date());
    assertNotNull(person1);
    final String jsonPerson1 = Serialization.serialize(person1);
    final Person person2 = Serialization.deserialize(jsonPerson1, Person.class);
    assertNotNull(person2);
    assertEquals(person1, person2);
    final String jsonPerson2 = Serialization.serialize(person2);
    assertEquals(jsonPerson1, jsonPerson2);
  }
  
  @Test
  public void testDeserializationToClientClass() throws Exception {
    final Person person = new Person("First Middle Last, Jr.", new Date());
    final String jsonPerson = Serialization.serialize(person);
    final ClientPerson clientPerson = Serialization.deserialize(jsonPerson, ClientPerson.class);
    assertEquals(person.name, clientPerson.name);
    assertEquals(person.birthDate, clientPerson.birthDate);
  }
  
  public static class Person {
    public final Date birthDate;
    public final String name;
    
    @Override
    public boolean equals(final Object other) {
      if (other == null || other.getClass() != Person.class) {
        return false;
      }
      
      final Person otherPerson = (Person) other;
      
      return this.name.equals(otherPerson.name) && this.birthDate.equals(otherPerson.birthDate);
    }

    Person(final String name, final Date birthDate) {
      this.name = name;
      this.birthDate = birthDate;
    }
  }
  
  public static class ClientPerson {
    public String name;
    public Date birthDate;
  }
}
