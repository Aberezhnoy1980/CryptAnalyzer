package ru.javarush.berezhnoy.domain.exception;

public class CaesarCipherException extends RuntimeException {
    public CaesarCipherException(String message) {
        super(message);
    }

    public CaesarCipherException(String message, Throwable cause) {
        super(message, cause);
    }
}
