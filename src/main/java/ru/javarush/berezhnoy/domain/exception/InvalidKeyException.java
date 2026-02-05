package ru.javarush.berezhnoy.domain.exception;

/**
 * Исключение для невалидного ключа шифрования.
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
