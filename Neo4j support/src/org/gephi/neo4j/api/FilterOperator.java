package org.gephi.neo4j.api;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Martin Škurla
 */
public enum FilterOperator {
    EQUALS("==")
    // <editor-fold defaultstate="collapsed" desc="implementation">
    {

        @Override
        public boolean executeOnWholeNumbers(Number number1, Number number2) {
            return number1.longValue() == number2.longValue();
        }

        @Override
        public boolean executeOnWholeNumberArrays(Object array1, Object array2) {
            if (Array.getLength(array1) != Array.getLength(array2)) {
                return false;


            }
            for (int index = 0; index < Array.getLength(array1); index++) {
                boolean comparisonResult =
                        EQUALS.executeOnWholeNumbers((Number) Array.get(array1, index),
                        (Number) Array.get(array2, index));

                if (comparisonResult == false) {
                    return false;

                }
            }
            return true;
        }

        @Override
        public boolean executeOnRealNumbers(Number number1, Number number2) {
            return number1.doubleValue() == number2.doubleValue();
        }

        @Override
        public boolean executeOnRealNumberArrays(Object array1, Object array2) {
            if (Array.getLength(array1) != Array.getLength(array2)) {
                return false;


            }
            for (int index = 0; index < Array.getLength(array1); index++) {
                boolean comparisonResult =
                        EQUALS.executeOnRealNumbers((Number) Array.get(array1, index),
                        (Number) Array.get(array2, index));

                if (comparisonResult == false) {
                    return false;

                }
            }
            return true;
        }

        @Override
        public boolean executeOnBooleans(Boolean boolean1, Boolean boolean2) {
            return boolean1.booleanValue() == boolean2.booleanValue();
        }

        @Override
        public boolean executeOnBooleanArrays(Object array1, Object array2) {
            if (Array.getLength(array1) != Array.getLength(array2)) {
                return false;


            }
            for (int index = 0; index < Array.getLength(array1); index++) {
                boolean comparisonResult =
                        EQUALS.executeOnBooleans((Boolean) Array.get(array1, index),
                        (Boolean) Array.get(array2, index));

                if (comparisonResult == false) {
                    return false;

                }
            }
            return true;
        }

        @Override
        public boolean executeOnCharacters(Character char1, Character char2, boolean matchCase) {
            return matchCase ? char1.charValue() == char2.charValue()
                    : Character.toLowerCase(char1) == Character.toLowerCase(char2);
        }

        @Override
        public boolean executeOnCharacterArrays(Object array1, Object array2, boolean matchCase) {
            if (Array.getLength(array1) != Array.getLength(array2)) {
                return false;


            }
            for (int index = 0; index < Array.getLength(array1); index++) {
                boolean comparisonResult =
                        EQUALS.executeOnCharacters((Character) Array.get(array1, index),
                        (Character) Array.get(array2, index),
                        matchCase);

                if (comparisonResult == false) {
                    return false;

                }
            }
            return true;
        }

        @Override
        public boolean executeOnStrings(String str1, String str2, boolean matchCase) {
            return matchCase ? str1.equals(str2)
                    : str1.equalsIgnoreCase(str2);
        }

        @Override
        public boolean executeOnStringArrays(Object array1, Object array2, boolean matchCase) {
            if (Array.getLength(array1) != Array.getLength(array2)) {
                return false;


            }
            for (int index = 0; index < Array.getLength(array1); index++) {
                boolean comparisonResult =
                        EQUALS.executeOnStrings((String) Array.get(array1, index),
                        (String) Array.get(array2, index),
                        matchCase);

                if (comparisonResult == false) {
                    return false;

                }
            }
            return true;
        }
    },// </editor-fold>

    NOT_EQUALS("!=")
    // <editor-fold defaultstate="collapsed" desc="implementation">
    {

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
    },// </editor-fold>
    
    LESS("<")
    // <editor-fold defaultstate="collapsed" desc="implementation">
    {

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
    },// </editor-fold>

    LESS_OR_EQUALS("<=")
    // <editor-fold defaultstate="collapsed" desc="implementation">
    {

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
    },// </editor-fold>

    GREATER(">")
    // <editor-fold defaultstate="collapsed" desc="implementation">
    {

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
    },// </editor-fold>

    GREATER_OR_EQUALS(">=")
    // <editor-fold defaultstate="collapsed" desc="implementation">
    {

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
    };// </editor-fold>


    private static final boolean RESTRICTIVE_MODE = false;

    private final String textRepresentation;


    private FilterOperator(String textRepresentation) {
        this.textRepresentation = textRepresentation;
    }


    public boolean executeOnWholeNumbers(Number number1, Number number2) {
        return !RESTRICTIVE_MODE;
    }
    public boolean executeOnWholeNumberArrays(Object array1, Object array2) {
        return !RESTRICTIVE_MODE;
    }

    public boolean executeOnRealNumbers(Number number1, Number number2) {
        return !RESTRICTIVE_MODE;
    }
    public boolean executeOnRealNumberArrays(Object array1, Object array2) {
        return !RESTRICTIVE_MODE;
    }

    public boolean executeOnBooleans(Boolean boolean1, Boolean boolean2) {
        return !RESTRICTIVE_MODE;
    }
    public boolean executeOnBooleanArrays(Object array1, Object array2) {
        return !RESTRICTIVE_MODE;
    }

    public boolean executeOnCharacters(Character char1, Character char2, boolean matchCase) {
        return !RESTRICTIVE_MODE;
    }
    public boolean executeOnCharacterArrays(Object array1, Object array2, boolean matchCase) {
        return !RESTRICTIVE_MODE;
    }

    public boolean executeOnStrings(String str1, String str2, boolean matchCase) {
        return !RESTRICTIVE_MODE;
    }
    public boolean executeOnStringArrays(Object array1, Object array2, boolean matchCase) {
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
