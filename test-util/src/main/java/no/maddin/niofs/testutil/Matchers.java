package no.maddin.niofs.testutil;

import org.hamcrest.Condition;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static org.hamcrest.Condition.matched;
import static org.hamcrest.Condition.notMatched;

public class Matchers {

    private Matchers() {
    }

    public static <T> TypeSafeDiagnosingMatcher<T> hasStringValue(Matcher<String> stringMatcher) {
        return new org.hamcrest.TypeSafeDiagnosingMatcher<T>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("string value matching (").appendDescriptionOf(stringMatcher).appendText(")");
            }

            @Override
            protected boolean matchesSafely(T item, Description description) {
                return isNonNull(item, description)
                    .matching(stringMatcher);
            }

            private Condition<String> isNonNull(T item, Description description) {
                return item == null ? notMatched() : matched(item.toString(), description);
            }
        };
    }
}
