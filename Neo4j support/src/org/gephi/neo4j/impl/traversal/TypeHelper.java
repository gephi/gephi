package org.gephi.neo4j.impl.traversal;

/**
 *
 * @author Martin Škurla
 */
public class TypeHelper {
    private static final String TRUE_LITERAL_VALUE  = "true";
    private static final String FALSE_LITERAL_VALUE = "false";
    private static final String ARRAY_LITERAL_START_REGEX = "[";
    private static final String ARRAY_LITERAL_END_REGEX = "]";
    private static final String ARRAY_LITERAL_SEPARATOR_REGEX = ",";


    private TypeHelper() {}


    public static boolean isWholeNumber(Object object) {
        Class<?> clazz = object.getClass();

        return clazz == Byte.class ||
               clazz == Short.class ||
               clazz == Integer.class ||
               clazz == Long.class;
    }

    public static boolean isRealNumber(Object object) {
        Class<?> clazz = object.getClass();

        return clazz == Float.class ||
               clazz == Double.class;
    }

    public static boolean isCharacter(Object object) {
        return object.getClass() == Character.class;
    }

    public static boolean isBoolean(Object object) {
        return object.getClass() == Boolean.class;
    }

    public static Long parseWholeNumber(String input) throws NotParsableException {
        try {
            return Long.valueOf(input);
        }
        catch (NumberFormatException nfe) {
            throw new NotParsableException();
        }
    }

    public static Double parseRealNumber(String input) throws NotParsableException {
        try {
            return Double.valueOf(input);
        }
        catch (NumberFormatException nfe) {
            throw new NotParsableException();
        }
    }

    public static Boolean parseBoolean(String input) throws NotParsableException {
       if (input.equals(TRUE_LITERAL_VALUE))
           return Boolean.TRUE;
       else if (input.equals(FALSE_LITERAL_VALUE))
           return Boolean.FALSE;
       else
           throw new NotParsableException();
    }
    
    public static Character parseCharacter(String input) throws NotParsableException {
        if (input.length() == 1)
            return Character.valueOf(input.charAt(0));
        else
            throw new NotParsableException();
    }
}
