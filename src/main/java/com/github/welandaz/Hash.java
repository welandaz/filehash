package com.github.welandaz;

import javax.annotation.Nonnull;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.lang.String.format;

public enum Hash {
    MD5(getDigest("MD5")),
    SHA256(getDigest("SHA-256")),
    SHA512(getDigest("SHA-512"));

    private MessageDigest messageDigest;

    Hash(final MessageDigest messageDigest) {
        this.messageDigest = messageDigest;
    }

    @Nonnull
    public MessageDigest messageDigest() {
        return messageDigest;
    }

    private static MessageDigest getDigest(final String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(format("Environment doesn't support [%s] encoding", algorithm), e);
        }
    }
}
