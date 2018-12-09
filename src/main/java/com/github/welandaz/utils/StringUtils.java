package com.github.welandaz.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *  Utility class for working with strings
 */
public final class StringUtils {

    private static char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private StringUtils() {
    }

    /**
     * This function transform a byte array into its hexadecimal string representation.
     *
     * @param bytes - an array of bytes to transform
     * @return - Hexadecimal representation of byte array
     */
    @Nonnull
    public static String toHexString(@Nullable final byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }

        final int length = bytes.length;
        final char[] buffer = new char[length * 2];

        for (int i = 0; i < length; i++) {
            final int masked = bytes[i] & 0xFF;
            buffer[i * 2] = HEX[masked >> 4];
            buffer[i * 2 + 1] = HEX[masked & 0x0F];
        }

        return new String(buffer);
    }

}
