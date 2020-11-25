package co.vaughnvernon.mockroservices.journal;

import co.vaughnvernon.mockroservices.Person;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StreamNameBuilderTest {

    @Test
    public void testThatStreamNameBuilderBuildsCorrectStream() {
      assertEquals("person_1234", StreamNameBuilder.buildStreamNameFor(Person.class, "1234"));
    }

    @Test
    public void TestThatStreamNameBuilderBuildsCorrectCategoryStream() {
      assertEquals("cat-person", StreamNameBuilder.buildStreamNameFor(Person.class));
    }
}
