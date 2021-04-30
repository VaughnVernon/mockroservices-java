package co.vaughnvernon.mockroservices;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import co.vaughnvernon.mockroservices.model.DomainEvent;
import co.vaughnvernon.mockroservices.model.SourcedEntity;

public class Person {
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

  public Person(final String name, final Date birthDate) {
    this.name = name;
    this.birthDate = birthDate;
  }

  public static class ClientPerson {
    public String name;
    public Date birthDate;
  }

  public static class ClientPersonWithPrivateSetter {
    public String name;
    public Date birthDate;

    public ClientPersonWithPrivateSetter() { }

    public static ClientPersonWithPrivateSetter instance(String name, Date birthDate) {
      return new ClientPersonWithPrivateSetter(name, birthDate);
    }
    private ClientPersonWithPrivateSetter(String name, Date birthDate) {
      this.name = name;
      this.birthDate = birthDate;
    }
  }

  public static class PersonES extends SourcedEntity<DomainEvent> {
    // [JsonConstructor]
    public PersonES(String name, int age, Date birthDate) {
      apply(new PersonNamed(UUID.randomUUID().toString(), name, age, birthDate));
    }

    public PersonES(List<DomainEvent> stream, int streamVersion) {
      super(stream, streamVersion);
    }

    public String id;
    public String name;
    public int age;
    public Date birthDate;

    public void when(PersonNamed personNamed)
    {
      id = personNamed.id;
      name = personNamed.name;
      age = personNamed.age;
    }

    @Override
    public boolean equals(final Object other) {
      if (other == null || other.getClass() != PersonES.class) {
        return false;
      }

      final PersonES otherPerson = (PersonES) other;

      return this.id.equals(otherPerson.id) && this.name.equals(otherPerson.name) && this.age == otherPerson.age;
    }
    // public override int GetHashCode() => 31 * Id.GetHashCode() * Name.GetHashCode() * Age.GetHashCode();
    // public override string ToString() => $"PersonES [Id={Id} Name={Name} Age={Age}]";
  }
  public static class PersonNamed extends DomainEvent {
    public String id;
    public String name;
    public int age;
    public Date birthDate;

    public PersonNamed(String id, String name, int age, Date birthDate)
    {
      this.id = id;
      this.name = name;
      this.age = age;
      this.birthDate = birthDate;
    }
  }
}
