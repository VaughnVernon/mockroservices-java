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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import co.vaughnvernon.mockroservices.Person;
import co.vaughnvernon.mockroservices.serialization.Serialization;
import org.junit.Test;

import java.util.Date;

public class JournalTest {

  @Test
  public void testJournalOpenClose() throws Exception {
    final Journal journal = Journal.open("test");
    assertNotNull(journal);
    assertEquals("test", journal.name());
    final Journal journalPreOpened = Journal.open("test");
    assertSame(journal, journalPreOpened);

    journal.close();
  }

  @Test
  public void testOpenJournalReader() throws Exception {
    final Journal journal = Journal.open("test");
    final JournalReader reader = journal.reader("test_reader");
    assertNotNull(reader);
    assertEquals("test_reader", reader.name());
    final JournalReader readerPreOpened = journal.reader("test_reader");
    assertSame(reader, readerPreOpened);

    journal.close();
  }

  @Test
  public void testWriteRead() throws Exception {
    final Journal journal = Journal.open("test");
    journal.write("name123", EntryValue.NO_STREAM_VERSION, EntryBatch.of("type1", "type1_instance1"));
    journal.write("name456", EntryValue.NO_STREAM_VERSION, EntryBatch.of("type2", "type2_instance1"));
    final JournalReader reader = journal.reader("cat-name");
    assertEquals(new StoredSource(0, new EntryValue("name123", 1, "type1", "type1_instance1", "")), reader.readNext());
    reader.acknowledge(0);
    assertEquals(new StoredSource(1, new EntryValue("name456", 1, "type2", "type2_instance1", "")), reader.readNext());
    reader.acknowledge(1);
    assertEquals(new StoredSource(-1, new EntryValue("", EntryValue.NO_STREAM_VERSION, "", "", "")), reader.readNext());

    journal.close();
  }

  @Test
  public void testWriteReadWithStreamNamePrefix() throws Exception {
    final Journal journal = Journal.open("test");
    Person person = new Person("John", new Date());
    final String personEntry = Serialization.serialize(person);
    journal.write(person.getClass(), "123", EntryValue.NO_STREAM_VERSION, new EntryBatch(person.getClass().getName(), personEntry));
    final EntryStreamReader reader = journal.streamReader();
    EntryStream entryStream = reader.streamFor(person.getClass(), "123");
    assertEquals("person_123", entryStream.streamName);
    assertEquals(1, entryStream.streamVersion);
    journal.close();
  }

  @Test
  public void testWriteReadStream() throws Exception {
    final Journal journal = Journal.open("test");
    journal.write("name123", EntryValue.NO_STREAM_VERSION, EntryBatch.of("type1", "type1_instance1"));
    journal.write("name456", EntryValue.NO_STREAM_VERSION, EntryBatch.of("type2", "type2_instance1"));
    journal.write("name123", 2, EntryBatch.of("type1-1", "type1-1_instance1"));
    journal.write("name123", 3, EntryBatch.of("type1-2", "type1-2_instance1"));
    journal.write("name456", 2, EntryBatch.of("type2-1", "type2-1_instance1"));

    final EntryStreamReader streamReader = journal.streamReader();

    final EntryStream eventStream123 = streamReader.streamFor("name123");
    assertEquals(3, eventStream123.streamVersion);
    assertEquals(3, eventStream123.stream.size());
    assertEquals(new EntryValue("name123", 1, "type1", "type1_instance1", ""), eventStream123.stream.get(0));
    assertEquals(new EntryValue("name123", 2, "type1-1", "type1-1_instance1", ""), eventStream123.stream.get(1));
    assertEquals(new EntryValue("name123", 3, "type1-2", "type1-2_instance1", ""), eventStream123.stream.get(2));

    final EntryStream eventStream456 = streamReader.streamFor("name456");
    assertEquals(2, eventStream456.streamVersion);
    assertEquals(2, eventStream456.stream.size());
    assertEquals(new EntryValue("name456", 1, "type2", "type2_instance1", ""), eventStream456.stream.get(0));
    assertEquals(new EntryValue("name456", 2, "type2-1", "type2-1_instance1", ""), eventStream456.stream.get(1));

    journal.close();
  }

  @Test
  public void testWriteReadStreamSnapshot() throws Exception {
    final Journal journal = Journal.open("test");
    journal.write("name123", EntryValue.NO_STREAM_VERSION, EntryBatch.of("type1", "type1_instance1", "SNAPSHOT123-1"));
    journal.write("name456", EntryValue.NO_STREAM_VERSION, EntryBatch.of("type2", "type2_instance1", "SNAPSHOT456-1"));
    journal.write("name123", 2, EntryBatch.of("type1-1", "type1-1_instance1", "SNAPSHOT123-2"));
    journal.write("name123", 3, EntryBatch.of("type1-2", "type1-2_instance1"));
    journal.write("name456", 2, EntryBatch.of("type2-1", "type2-1_instance1", "SNAPSHOT456-2"));

    final EntryStreamReader streamReader = journal.streamReader();

    final EntryStream eventStream123 = streamReader.streamFor("name123");
    assertEquals("name123", eventStream123.streamName);
    assertEquals(3, eventStream123.streamVersion);
    assertEquals(1, eventStream123.stream.size());
    assertEquals("SNAPSHOT123-2", eventStream123.snapshot);
    // assertEquals(new EntryValue("name123", 3, "type1-2", "type1-2_instance1", ""), eventStream123.stream.get(0));

    final EntryStream eventStream456 = streamReader.streamFor("name456");
    assertEquals("name456", eventStream456.streamName);
    assertEquals(2, eventStream456.streamVersion);
    assertEquals(0, eventStream456.stream.size());
    assertEquals("SNAPSHOT456-2", eventStream456.snapshot);

    journal.close();
  }

  @Test
  public void testWriteReadStreamCategory() {
    final Journal journal = Journal.open("test");

    journal.write("name123", EntryValue.NO_STREAM_VERSION, EntryBatch.of("type1", "type1_instance1"));
    journal.write("name456", EntryValue.NO_STREAM_VERSION, EntryBatch.of("type2", "type2_instance1"));
    journal.write("name123", 2, EntryBatch.of("type1-1", "type1-1_instance1"));
    journal.write("name123", 3, EntryBatch.of("type1-2", "type1-2_instance1"));
    journal.write("name456", 2, EntryBatch.of("type2-1", "type2-1_instance1"));

    final EntryStreamReader streamReader = journal.streamReader();

    final EntryStream categoryEntryStream = streamReader.streamFor("cat-name");
    assertEquals(5, categoryEntryStream.streamVersion);
    assertEquals(5, categoryEntryStream.stream.size());
    assertEquals(new EntryValue("name123", 1, "type1", "type1_instance1", ""), categoryEntryStream.stream.get(0));
    assertEquals(new EntryValue("name456", 2, "type2", "type2_instance1", ""), categoryEntryStream.stream.get(1));
    assertEquals(new EntryValue("name123", 3, "type1-1", "type1-1_instance1", ""), categoryEntryStream.stream.get(2));
    assertEquals(new EntryValue("name123", 4, "type1-2", "type1-2_instance1", ""), categoryEntryStream.stream.get(3));
    assertEquals(new EntryValue("name456", 5, "type2-1", "type2-1_instance1", ""), categoryEntryStream.stream.get(4));

    journal.close();
  }
}
