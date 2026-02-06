package ru.javarush.berezhnoy.domain.exception;

/**
 * Thrown when the encryption key is invalid.
 */
public class InvalidKeyException extends CaesarCipherException {

    private final int invalidKey;

    public InvalidKeyException(int invalidKey) {
        super("Invalid encryption key: " + invalidKey);
        this.invalidKey = invalidKey;
    }

    public InvalidKeyException(int invalidKey, String message) {
        super(message);
        this.invalidKey = invalidKey;
    }

    public int getInvalidKey() {
        return invalidKey;
    }
}
