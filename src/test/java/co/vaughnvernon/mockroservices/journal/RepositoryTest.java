package co.vaughnvernon.mockroservices.journal;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.Date;

import org.junit.Test;

import co.vaughnvernon.mockroservices.Person;
import co.vaughnvernon.mockroservices.Person.PersonES;

public class RepositoryTest {
  @Test
  public void testSaveFind()
  {
    PersonRepository repository = new PersonRepository();
    Person.PersonES person = new Person.PersonES("Joe", 40, Date.from(Instant.now()));
    repository.save(person);
    Person.PersonES joe = repository.personOfId(person.id);
    assertEquals(person, joe);
  }

  @Test
  public void testSaveWithStreamPrefixAndFind()
  {
    PersonRepository repository = new PersonRepository();
    Person.PersonES person = new Person.PersonES("Joe", 40, Date.from(Instant.now()));
    repository.save(PersonES.class, person);
    Person.PersonES joe = repository.personOfId(StreamNameBuilder.buildStreamNameFor(PersonES.class, person.id));
    assertEquals(person, joe);
  }

  @Test
  public void testSaveFindWithStreamPrefix()
  {
    PersonRepository repository = new PersonRepository();
    Person.PersonES person = new Person.PersonES("Joe", 40, Date.from(Instant.now()));
    repository.save(PersonES.class, person);
    Person.PersonES joe = repository.personOfId(PersonES.class, person.id);
    assertEquals(person, joe);
  }

  public class PersonRepository extends Repository {
        // readonly
        private Journal journal;
        // readonly
        private EntryStreamReader reader;

        public PersonES personOfId(String id)
        {
            EntryStream stream = reader.streamFor(id);
            return new PersonES(toSourceStream(stream.stream), stream.streamVersion);
        }

        public <T> PersonES personOfId(final Class<T> streamClass, String id)
        {
          EntryStream stream = reader.streamFor(streamClass, id);
          return new PersonES(toSourceStream(stream.stream), stream.streamVersion);
        }

        public void save(PersonES person) {
          journal.write(person.id, person.currentVersion, toBatch(person.applied));
        }

        public <T extends PersonES> void save(final Class<T> streamClass, T person) {
          journal.write(streamClass, person.id, person.currentVersion, toBatch(person.applied));
        }
        //internal
        private PersonRepository()
        {
            journal = Journal.open("repo-test");
            reader = journal.streamReader();
        }
  }
}
