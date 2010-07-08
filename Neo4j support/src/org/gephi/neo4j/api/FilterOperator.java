package org.gephi.neo4j.api;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Martin Škurla
 */
public enum FilterOperator {
    EQUALS("==") {
        @Override
        public boolean executeOnWholeNumbers(Number number1, Number number2) {
            return number1.longValue() == number2.longValue();
        }

        @Override
        public boolean executeOnRealNumbers(Number number1, Number number2) {
            return number1.doubleValue() == number2.doubleValue();
        }

        @Override
        public boolean executeOnBooleans(Boolean boolean1, Boolean boolean2) {
            return boolean1.booleanValue() == boolean2.booleanValue();
        }

        @Override
        public boolean executeOnCharacters(Character char1, Character char2, boolean matchCase) {
            return matchCase ? char1.charValue() == char2.charValue()
                             : Character.toLowerCase(char1) == Character.toLowerCase(char2);
        }

        @Override
        public boolean executeOnStrings(String str1, String str2, boolean matchCase) {
            return matchCase ? str1.equals(str2)
                             : str1.equalsIgnoreCase(str2);
        }
    },
    NOT_EQUALS("!=") {
        @Override
        public boolean executeOnWholeNumbers(Number number1, Number number2) {
            return number1.longValue() != number2.longValue();
        }

        @Override
        public boolean executeOnRealNumbers(Number number1, Number number2) {
            return number1.doubleValue() != number2.doubleValue();
        }

        @Override
        public boolean executeOnBooleans(Boolean boolean1, Boolean boolean2) {
            return boolean1.booleanValue() != boolean2.booleanValue();
        }

        @Override
        public boolean executeOnCharacters(Character char1, Character char2, boolean matchCase) {
            return matchCase ? char1.charValue() != char2.charValue()
                             : Character.toLowerCase(char1) != Character.toLowerCase(char2);
        }

        @Override
        public boolean executeOnStrings(String str1, String str2, boolean matchCase) {
            return matchCase ? !str1.equals(str2)
                             : !str1.equalsIgnoreCase(str2);
        }
    },
    LESS("<") {
        @Override
        public boolean executeOnWholeNumbers(Number number1, Number number2) {
            return number1.longValue() < number2.longValue();
        }

        @Override
        public boolean executeOnRealNumbers(Number number1, Number number2) {
            return number1.doubleValue() < number2.doubleValue();
        }

        @Override
        public boolean executeOnCharacters(Character char1, Character char2, boolean matchCase) {
            return matchCase ? char1.charValue() < char2.charValue()
                             : Character.toLowerCase(char1) < Character.toLowerCase(char2);
        }
    },
    LESS_OR_EQUALS("<=") {
        @Override
        public boolean executeOnWholeNumbers(Number number1, Number number2) {
            return number1.longValue() <= number2.longValue();
        }

        @Override
        public boolean executeOnRealNumbers(Number number1, Number number2) {
            return number1.doubleValue() <= number2.doubleValue();
        }

        @Override
        public boolean executeOnCharacters(Character char1, Character char2, boolean matchCase) {
            return matchCase ? char1.charValue() <= char2.charValue()
                             : Character.toLowerCase(char1) <= Character.toLowerCase(char2);
        }
    },
    GREATER(">") {
        @Override
        public boolean executeOnWholeNumbers(Number number1, Number number2) {
            return number1.longValue() > number2.longValue();
        }

        @Override
        public boolean executeOnRealNumbers(Number number1, Number number2) {
            return number1.doubleValue() > number2.doubleValue();
        }

        @Override
        public boolean executeOnCharacters(Character char1, Character char2, boolean matchCase) {
            return matchCase ? char1.charValue() > char2.charValue()
                             : Character.toLowerCase(char1) > Character.toLowerCase(char2);
        }
    },
    GREATER_OR_EQUALS(">=") {
        @Override
        public boolean executeOnWholeNumbers(Number number1, Number number2) {
            return number1.longValue() >= number2.longValue();
        }

        @Override
        public boolean executeOnRealNumbers(Number number1, Number number2) {
            return number1.doubleValue() >= number2.doubleValue();
        }

        @Override
        public boolean executeOnCharacters(Character char1, Character char2, boolean matchCase) {
            return matchCase ? char1.charValue() >= char2.charValue()
                             : Character.toLowerCase(char1) >= Character.toLowerCase(char2);
        }
    };

    private static final boolean RESTRICTIVE_MODE = false;

    private final String textRepresentation;


    private FilterOperator(String textRepresentation) {
        this.textRepresentation = textRepresentation;
    }


    public boolean executeOnWholeNumbers(Number number1, Number number2) {
        return !RESTRICTIVE_MODE;
    }

    public boolean executeOnRealNumbers(Number number1, Number number2) {
        return !RESTRICTIVE_MODE;
    }

    public boolean executeOnBooleans(Boolean boolean1, Boolean boolean2) {
        return !RESTRICTIVE_MODE;
    }

    public boolean executeOnCharacters(Character char1, Character char2, boolean matchCase) {
        return !RESTRICTIVE_MODE;
    }

    public boolean executeOnStrings(String str1, String str2, boolean matchCase) {
        return !RESTRICTIVE_MODE;
    }


    public static String[] getTextRepresentations() {
        List<String> textRepresentations = new LinkedList<String>();

        for (FilterOperator filterOperator : FilterOperator.values())
            textRepresentations.add(filterOperator.textRepresentation);

        return textRepresentations.toArray(new String[0]);
    }

    public static FilterOperator fromTextRepresentation(String textRepresentation) {
        for (FilterOperator filterOperator : values()) {
            if (filterOperator.textRepresentation.equals(textRepresentation))
                return filterOperator;
        }

        throw new AssertionError();
    }
}
