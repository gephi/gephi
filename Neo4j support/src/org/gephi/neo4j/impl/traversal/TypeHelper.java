package org.gephi.neo4j.impl.traversal;

/**
 *
 * @author Martin Škurla
 */
public class TypeHelper {
    private static final String TRUE_LITERAL_VALUE  = "true";
    private static final String FALSE_LITERAL_VALUE = "false";
    private static final String ARRAY_LITERAL_START_REGEX = "\\[";
    private static final String ARRAY_LITERAL_END_REGEX = "\\]";
    private static final String ARRAY_LITERAL_SEPARATOR_REGEX = ",";


    private TypeHelper() {}


    public static boolean isWholeNumber(Object object) {
        Class<?> clazz = object.getClass();

        return clazz == Byte.class ||
               clazz == Short.class ||
               clazz == Integer.class ||
               clazz == Long.class;
    }

    public static boolean isWholeNumberArray(Object object) {
        Class<?> componentType = object.getClass().getComponentType();

        return componentType == byte.class ||
               componentType == short.class ||
               componentType == int.class ||
               componentType == long.class;
    }

    public static boolean isRealNumber(Object object) {
        Class<?> clazz = object.getClass();

        return clazz == Float.class ||
               clazz == Double.class;
    }

    public static boolean isRealNumberArray(Object object) {
        Class<?> componentType = object.getClass().getComponentType();

        return componentType == float.class ||
               componentType == double.class;
    }

    public static boolean isCharacter(Object object) {
        return object.getClass() == Character.class;
    }

    public static boolean isCharacterArray(Object object) {
        return object.getClass().getComponentType() == char.class;
    }

    public static boolean isBoolean(Object object) {
        return object.getClass() == Boolean.class;
    }

    public static boolean isBooleanArray(Object object) {
        return object.getClass().getComponentType() == boolean.class;
    }

    public static boolean isArray(Object object) {
        return object.getClass().isArray();
    }

    private static String removeWhitespacesFromArrayLiteral(String input) {
        String result;

        result = input .replaceAll("\\s*" + ARRAY_LITERAL_START_REGEX     + "\\s*", ARRAY_LITERAL_START_REGEX);
        result = result.replaceAll("\\s*" + ARRAY_LITERAL_SEPARATOR_REGEX + "\\s*", ARRAY_LITERAL_SEPARATOR_REGEX);
        result = result.replaceAll("\\s*" + ARRAY_LITERAL_END_REGEX       + "\\s*", ARRAY_LITERAL_END_REGEX);

        return result;
    }

    public static Long parseWholeNumber(String input) throws NotParsableException {
        try {
            return Long.valueOf(input);
        }
        catch (NumberFormatException nfe) {
            throw new NotParsableException();
        }
    }

    public static Long[] parseWholeNumberArray(String input) throws NotParsableException {
        System.out.println("before removal: " + input);
        String parsedInput = removeWhitespacesFromArrayLiteral(input);
        System.out.println("after removal: " + input);

        String regex = String.format("%s\\d+(%s\\d+)*%s",
                ARRAY_LITERAL_START_REGEX, ARRAY_LITERAL_SEPARATOR_REGEX, ARRAY_LITERAL_END_REGEX);
        if (!parsedInput.matches(regex))
            throw new NotParsableException();

        parsedInput = parsedInput.replaceAll(ARRAY_LITERAL_START_REGEX, "");
        parsedInput = parsedInput.replaceAll(ARRAY_LITERAL_END_REGEX,   "");

        String[] tokenizedInput = parsedInput.split(regex);

        Long[] array = new Long[tokenizedInput.length];

        for (int index = 0; index < parsedInput.length(); index++)
            array[index] = parseWholeNumber(tokenizedInput[index]);

        return array;
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
