package com.github.welandaz.utils;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Utility class to wrap exceptions while working with functional interfaces
 */
public final class ThrowingUtils {

    private ThrowingUtils() {
    }

    /**
     * Accepts BiConsumer, which throws an unchecked exception
     *
     * @param throwing - consumer to execute
     * @param <T> - type of first parameter
     * @param <U> - type of second parameter
     * @return - instance of ThrowingBiConsumer, which doesn't require try{...} catch{...} block
     */
    @Nonnull
    public static <T, U> BiConsumer<T, U> consume(@Nonnull final ThrowingBiConsumer<T, U> throwing ) {
        Objects.requireNonNull(throwing, "bi-consumer must not be null");

        return throwing.asBiConsumer();
    }

    @FunctionalInterface
    public interface ThrowingBiConsumer<T, U> {
        void accept(T t,U u) throws Exception;

        default BiConsumer<T, U> asBiConsumer() {
            return ( t, u ) -> {
                try {
                    this.accept(t, u);
                } catch(final Exception e ) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

}
