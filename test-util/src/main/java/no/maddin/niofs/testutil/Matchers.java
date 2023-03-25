package no.maddin.niofs.testutil;

import org.hamcrest.Condition;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class Matchers {

    private Matchers() {
    }

    public static <T> TypeSafeDiagnosingMatcher<T> hasStringValue(Matcher<String> stringMatcher) {
        return new org.hamcrest.TypeSafeDiagnosingMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("has string value that ").appendDescriptionOf(stringMatcher);
            }

            @Override
            protected boolean matchesSafely(T item, Description description) {
                return isNonNull(item, description)
                    .matching(stringMatcher);
            }

            private Condition<String> isNonNull(T item, Description description) {
                return item == null ? Condition.notMatched() : Condition.matched(item.toString(), description);
            }
        };
    }

}
