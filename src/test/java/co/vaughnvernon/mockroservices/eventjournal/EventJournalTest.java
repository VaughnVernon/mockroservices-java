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

package co.vaughnvernon.mockroservices.eventjournal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import co.vaughnvernon.mockroservices.eventjournal.EventJournal;
import co.vaughnvernon.mockroservices.eventjournal.EventJournalReader;
import co.vaughnvernon.mockroservices.eventjournal.EventStream;
import co.vaughnvernon.mockroservices.eventjournal.EventStreamReader;
import co.vaughnvernon.mockroservices.eventjournal.EventValue;
import co.vaughnvernon.mockroservices.eventjournal.StoredEvent;

public class EventJournalTest {

  @Test
  public void testEventJournalOpenClose() throws Exception {
    final EventJournal journal = EventJournal.open("test");
    assertNotNull(journal);
    assertEquals("test", journal.name());
    final EventJournal journalPreOpened = EventJournal.open("test");
    assertSame(journal, journalPreOpened);

    journal.close();
  }

  @Test
  public void testOpenEventJournalReader() throws Exception {
    final EventJournal journal = EventJournal.open("test");
    final EventJournalReader reader = journal.reader("test_reader");
    assertNotNull(reader);
    assertEquals("test_reader", reader.name());
    final EventJournalReader readerPreOpened = journal.reader("test_reader");
    assertSame(reader, readerPreOpened);

    journal.close();
  }

  @Test
  public void testWriteRead() throws Exception {
    final EventJournal journal = EventJournal.open("test");
    journal.write("name123", 1, EventBatch.of("type1", "type1_instance1"));
    journal.write("name456", 1, EventBatch.of("type2", "type2_instance1"));
    final EventJournalReader reader = journal.reader("test_reader");
    assertEquals(new StoredEvent(0, new EventValue("name123", 1, "type1", "type1_instance1", "")), reader.readNext());
    reader.acknowledge(0);
    assertEquals(new StoredEvent(1, new EventValue("name456", 1, "type2", "type2_instance1", "")), reader.readNext());
    reader.acknowledge(1);
    assertEquals(new StoredEvent(-1, new EventValue("", -1, "", "", "")), reader.readNext());

    journal.close();
  }

  @Test
  public void testWriteReadStream() throws Exception {
    final EventJournal journal = EventJournal.open("test");
    journal.write("name123", 1, EventBatch.of("type1", "type1_instance1"));
    journal.write("name456", 1, EventBatch.of("type2", "type2_instance1"));
    journal.write("name123", 2, EventBatch.of("type1-1", "type1-1_instance1"));
    journal.write("name123", 3, EventBatch.of("type1-2", "type1-2_instance1"));
    journal.write("name456", 2, EventBatch.of("type2-1", "type2-1_instance1"));
    
    final EventStreamReader streamReader = journal.streamReader();
    
    final EventStream eventStream123 = streamReader.streamFor("name123");
    assertEquals(3, eventStream123.streamVersion);
    assertEquals(3, eventStream123.stream.size());
    assertEquals(new EventValue("name123", 1, "type1", "type1_instance1", ""), eventStream123.stream.get(0));
    assertEquals(new EventValue("name123", 2, "type1-1", "type1-1_instance1", ""), eventStream123.stream.get(1));
    assertEquals(new EventValue("name123", 3, "type1-2", "type1-2_instance1", ""), eventStream123.stream.get(2));
    
    final EventStream eventStream456 = streamReader.streamFor("name456");
    assertEquals(2, eventStream456.streamVersion);
    assertEquals(2, eventStream456.stream.size());
    assertEquals(new EventValue("name456", 1, "type2", "type2_instance1", ""), eventStream456.stream.get(0));
    assertEquals(new EventValue("name456", 2, "type2-1", "type2-1_instance1", ""), eventStream456.stream.get(1));

    journal.close();
  }

  @Test
  public void testWriteReadStreamSnapshot() throws Exception {
    final EventJournal journal = EventJournal.open("test");
    journal.write("name123", 1, EventBatch.of("type1", "type1_instance1", "SNAPSHOT123-1"));
    journal.write("name456", 1, EventBatch.of("type2", "type2_instance1", "SNAPSHOT456-1"));
    journal.write("name123", 2, EventBatch.of("type1-1", "type1-1_instance1", "SNAPSHOT123-2"));
    journal.write("name123", 3, EventBatch.of("type1-2", "type1-2_instance1"));
    journal.write("name456", 2, EventBatch.of("type2-1", "type2-1_instance1", "SNAPSHOT456-2"));
    
    final EventStreamReader streamReader = journal.streamReader();
    
    final EventStream eventStream123 = streamReader.streamFor("name123");
    assertEquals("name123", eventStream123.streamName);
    assertEquals(3, eventStream123.streamVersion);
    assertEquals(1, eventStream123.stream.size());
    assertEquals("SNAPSHOT123-2", eventStream123.snapshot);
    assertEquals(new EventValue("name123", 3, "type1-2", "type1-2_instance1", ""), eventStream123.stream.get(0));
    
    final EventStream eventStream456 = streamReader.streamFor("name456");
    assertEquals("name456", eventStream456.streamName);
    assertEquals(2, eventStream456.streamVersion);
    assertEquals(0, eventStream456.stream.size());
    assertEquals("SNAPSHOT456-2", eventStream456.snapshot);

    journal.close();
  }
}
