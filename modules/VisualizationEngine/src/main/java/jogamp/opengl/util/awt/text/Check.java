package jogamp.opengl.util.awt.text;


/**
 * Utility for checking arguments and preconditions.
 */
/*@ThreadSafe*/
public final class Check {

    /**
     * Prevents instantiation.
     */
    private Check() {
        // empty
    }

    /**
     * Ensures an argument is valid.
     *
     * @param condition Condition involving argument that should be true
     * @param message Message of exception thrown if condition is false
     * @throws IllegalArgumentException if condition is false
     */
    public static void argument(final boolean condition, /*@CheckForNull*/ final String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Ensures an object is not null.
     *
     * @param obj Object to check
     * @param message Message of exception thrown if object is null
     * @return Reference to the given object, not null
     * @throws NullPointerException if object is null
     */
    /*@Nonnull*/
    public static <T> T notNull(/*@Nullable*/ final T obj,
                                /*@CheckForNull*/ final String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
        return obj;
    }

    /**
     * Ensures the state of an object is valid when a method's called.
     *
     * @param condition Condition involving state that should be true
     * @param message Message of exception thrown if condition is false
     * @throws IllegalStateException if condition is false
     */
    public static void state(final boolean condition, /*@CheckForNull*/ final String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
