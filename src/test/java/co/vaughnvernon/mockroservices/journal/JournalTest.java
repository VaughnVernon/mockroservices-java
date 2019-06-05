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

package co.vaughnvernon.mockroservices.journal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import co.vaughnvernon.mockroservices.journal.EntryValue;
import co.vaughnvernon.mockroservices.journal.EntryBatch;
import co.vaughnvernon.mockroservices.journal.EntryStream;
import co.vaughnvernon.mockroservices.journal.EntryStreamReader;
import co.vaughnvernon.mockroservices.journal.Journal;
import co.vaughnvernon.mockroservices.journal.JournalReader;
import co.vaughnvernon.mockroservices.journal.StoredSource;

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
    journal.write("name123", 1, EntryBatch.of("type1", "type1_instance1"));
    journal.write("name456", 1, EntryBatch.of("type2", "type2_instance1"));
    final JournalReader reader = journal.reader("test_reader");
    assertEquals(new StoredSource(0, new EntryValue("name123", 1, "type1", "type1_instance1", "")), reader.readNext());
    reader.acknowledge(0);
    assertEquals(new StoredSource(1, new EntryValue("name456", 1, "type2", "type2_instance1", "")), reader.readNext());
    reader.acknowledge(1);
    assertEquals(new StoredSource(-1, new EntryValue("", -1, "", "", "")), reader.readNext());

    journal.close();
  }

  @Test
  public void testWriteReadStream() throws Exception {
    final Journal journal = Journal.open("test");
    journal.write("name123", 1, EntryBatch.of("type1", "type1_instance1"));
    journal.write("name456", 1, EntryBatch.of("type2", "type2_instance1"));
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
    journal.write("name123", 1, EntryBatch.of("type1", "type1_instance1", "SNAPSHOT123-1"));
    journal.write("name456", 1, EntryBatch.of("type2", "type2_instance1", "SNAPSHOT456-1"));
    journal.write("name123", 2, EntryBatch.of("type1-1", "type1-1_instance1", "SNAPSHOT123-2"));
    journal.write("name123", 3, EntryBatch.of("type1-2", "type1-2_instance1"));
    journal.write("name456", 2, EntryBatch.of("type2-1", "type2-1_instance1", "SNAPSHOT456-2"));
    
    final EntryStreamReader streamReader = journal.streamReader();
    
    final EntryStream eventStream123 = streamReader.streamFor("name123");
    assertEquals("name123", eventStream123.streamName);
    assertEquals(3, eventStream123.streamVersion);
    assertEquals(1, eventStream123.stream.size());
    assertEquals("SNAPSHOT123-2", eventStream123.snapshot);
    assertEquals(new EntryValue("name123", 3, "type1-2", "type1-2_instance1", ""), eventStream123.stream.get(0));
    
    final EntryStream eventStream456 = streamReader.streamFor("name456");
    assertEquals("name456", eventStream456.streamName);
    assertEquals(2, eventStream456.streamVersion);
    assertEquals(0, eventStream456.stream.size());
    assertEquals("SNAPSHOT456-2", eventStream456.snapshot);

    journal.close();
  }
}
