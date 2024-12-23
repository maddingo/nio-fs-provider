package no.maddin.niofs.testutil;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class MatchersTests {

    @Test
    void contextLoads() {
        DataClass actual = new DataClass("HALO");
        assertThat(actual, Matchers.hasStringValue(equalTo("DataClass{value='HALO'}")));
    }

    private static class DataClass {
        private final String value;

        public DataClass(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "DataClass{" +
                "value='" + value + '\'' +
                '}';
        }
    }
}
