package fr.zirnitra.safecall;

import java.util.Optional;
import java.util.function.Function;

/**
 * Allows to initiate a SafeCall on an object and the methods to call on it and on the objects returned by those calls.
 * <br>
 * There are two options :
 * <ul>
 *     <li>{@link SafeCall#of(Object)} to create a chain of calls on a single object</li>
 *     <li>{@link SafeCall#prepare(Class)} to create a chain of calls that can be applied to multiple objects</li>
 * </ul>
 *
 * The final value of the chain can be retrieved with :
 * <ul>
 *     <li>{@link SafeCallChain#get()}</li>
 *     <li>{@link SafeCallChain#getOrDefault(Object)}</li>
 *     <li>{@link SafeCallChain#getOptional()}</li>
 *     <li>{@link Result#get()}</li>
 *     <li>{@link Result#getOrDefault(Object)}</li>
 *     <li>{@link Result#getOptional()}</li>
 * </ul>
 *
 * Each {@link SafeCallChain#call(Function)}, {@link PreparedSafeCallChain#call(Function)}
 * and {@link PreparedSafeCallChain#on(Object)} create a new instances of those classes.<br>
 * {@link PreparedSafeCallChain} should be used on lists of objects.
 *
 * @author Victor Duda (Zirnitra)
 */
public class SafeCall {

    /**
     * Creates a SafeCallChain on a given object to check for null values on consecutive method calls.<br>
     * After calling this method, you can, optionally, add method calls with {@link SafeCallChain#call(Function)}.<br>
     * <br>
     * Once your chain is ready, you can get the final value with :
     * <ul>
     *     <li>{@link SafeCallChain#get()} to get the last object of the chain or null if one of the objects in the
     *     chain is null</li>
     *     <li>{@link SafeCallChain#getOrDefault(Object)} to get the last objet of the chain or a default value if
     *     one of the objects in the chain is null</li>
     *     <li>{@link SafeCallChain#getOptional()} to get an Optional containing the last objet of the chain or if
     *     one of the objects in the chain is null</li>
     * </ul>
     *
     * @param <T> Type of the root object
     * @param object The object to work with
     * @return An object chain that can be further processed
     */
    public static <T> SafeCallChain<T> of(T object) {
        return new SafeCallChain<>(object);
    }

    /**
     * Initiates a PreparedSafeCallChain for a given class which can be used multiple times on
     * instances of that class.<br>
     * After calling this method, you should call {@link PreparedSafeCallChain#call(Function)}
     * to generate the chain of calls.<br>
     * Once you're done, you can use {@link PreparedSafeCallChain#on(Object)} to apply the chain
     * to an object or {@link PreparedSafeCallChain#asFunction()} to get a reusable function.<br>
     * <br>
     * After calling {@link PreparedSafeCallChain#on(Object)} you can get the final value with :
     * <ul>
     *     <li>{@link SafeCallChain#get()} to get the last object of the chain or null if one of the objects in the
     *     chain is null</li>
     *     <li>{@link SafeCallChain#getOrDefault(Object)} to get the last objet of the chain or a default value if
     *     one of the objects in the chain is null</li>
     *     <li>{@link SafeCallChain#getOptional()} to get an Optional containing the last objet of the chain or if
     *     one of the objects in the chain is null</li>
     * </ul>
     *
     * @param <T>         Type of the input object
     * @param clazz       Class of the input object
     * @return A new empty function chain
     */
    @SuppressWarnings("unused")
    public static <T> PreparedSafeCallChain<T, T> prepare(Class<T> clazz) {
        return new PreparedSafeCallChain<>();
    }

    /**
     * Class representing a chain of safe calls on an object.
     *
     * @param <T> Type of the base object
     */
    public static class SafeCallChain<T> {
        private final T value;

        private SafeCallChain(T value) {
            this.value = value;
        }

        /**
         * The method to call on the object. After calling this method, it's possible to chain
         * another call to the returned object and so on.
         *
         * @param <R> Type of the result after applying the function
         * @param function The function to apply
         * @return A new object chain with the transformed value
         */
        public <R> SafeCallChain<R> call(Function<? super T, ? extends R> function) {
            if (value == null) {
                return new SafeCallChain<>(null);
            }
            return new SafeCallChain<>(function.apply(value));
        }

        /**
         * @return the last object of the chain or null if one of the objects in the chain is null
         */
        public T get() {
            return value;
        }

        /**
         * @return an Optional containing the last objet of the chain or if one of the objects in the chain is null
         */
        public Optional<T> getOptional() {
            return Optional.ofNullable(value);
        }

        /**
         * Gets the last object of the chain with a default fallback.
         *
         * @param defaultValue the default value if one of the objects in the chain is null
         * @return the last objet of the chain or a default value if one of the objects in the chain is null
         */
        public T getOrDefault(T defaultValue) {
            return value != null ? value : defaultValue;
        }
    }

    /**
     * A prepared safe call chain that can be applied to multiple objects.
     *
     * @param <T> Type of the input object
     * @param <R> Type of the result after applying all functions
     */
    public static class PreparedSafeCallChain<T, R> {
        private final Function<T, R> composedFunction;

        @SuppressWarnings("unchecked")
        private PreparedSafeCallChain() {
            // Identity function as starting point
            this.composedFunction = obj -> (R) obj;
        }

        private PreparedSafeCallChain(Function<T, R> composedFunction) {
            this.composedFunction = composedFunction;
        }

        /**
         * Adds a function call to the chain.
         *
         * @param <V>      Type of the result after applying the new function
         * @param function The function to add to the chain
         * @return A new chain with the added function
         */
        public <V> PreparedSafeCallChain<T, V> call(Function<? super R, ? extends V> function) {
            return new PreparedSafeCallChain<>(input -> {
                if (input == null) return null;
                R intermediate = composedFunction.apply(input);
                if (intermediate == null) return null;
                return function.apply(intermediate);
            });
        }

        /**
         * Applies the chain on an object.
         *
         * @param object The object to apply the functions to
         * @return A {@link Result} giving options to get the result of the chain calls
         */
        public Result<R> on(T object) {
            if (object == null) {
                return new Result<>(null);
            }
            return new Result<>(composedFunction.apply(object));
        }

        /**
         * Converts this chain to a reusable function.<br>
         * This is useful when you want to use the chain in a stream for example.
         *
         * @return A function that applies this chain to any input, returning the result or null.
         */
        public Function<T, R> asFunction() {
            return composedFunction;
        }

        /**
         * Converts this chain to a reusable function.<br>
         * This is useful when you want to use the chain in a stream for example.
         *
         * @return A function that applies this chain to any input and returns an Optional result
         */
        public Function<T, Optional<R>> asFunctionOptional() {
            return input -> Optional.ofNullable(composedFunction.apply(input));
        }

        /**
         * Converts this chain to a reusable function.<br>
         * This is useful when you want to use the chain in a stream for example.
         *
         * @param defaultValue The default value to return if the result is null
         * @return A function that applies this chain to any input and returns the result or default value
         */
        public Function<T, R> asFunction(R defaultValue) {
            return input -> {
                R result = composedFunction.apply(input);
                return result != null ? result : defaultValue;
            };
        }
    }

    /**
     * Represents the result of chain calls on an object.
     *
     * @param <T> Type of the last object of the chain.
     */
    public static class Result<T> {
        private final T value;

        private Result(T value) {
            this.value = value;
        }

        /**
         * @return the last object of the chain or null if one of the objects in the chain is null
         */
        public T get() {
            return value;
        }

        /**
         * @return an Optional containing the last objet of the chain or if one of the objects in the chain is null
         */
        public Optional<T> getOptional() {
            return Optional.ofNullable(value);
        }

        /**
         * Gets the last object of the chain with a default fallback.
         *
         * @param defaultValue the default value if one of the objects in the chain is null
         * @return the last objet of the chain or a default value if one of the objects in the chain is null
         */
        public T getOrDefault(T defaultValue) {
            return value != null ? value : defaultValue;
        }
    }
}