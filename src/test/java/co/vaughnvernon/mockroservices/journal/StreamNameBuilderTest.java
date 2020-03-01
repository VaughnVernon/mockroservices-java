package co.vaughnvernon.mockroservices.journal;

import co.vaughnvernon.mockroservices.serialization.SerializationTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StreamNameBuilderTest {

    @Test
    public void testThatStreamNameBuilderBuildsCorrectStream() {
        assertEquals("Person_1234", StreamNameBuilder.buildStreamNameFor(SerializationTest.Person.class, "1234"));
    }
}
