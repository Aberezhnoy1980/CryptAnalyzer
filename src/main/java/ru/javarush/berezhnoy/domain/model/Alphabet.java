package ru.javarush.berezhnoy.domain.model;

/**
 * Алфавит для шифра Цезаря.
 * Представляет собой набор символов, используемых при шифровании.
 * <p>
 * Иммутабельный класс для обеспечения безопасности многопоточности.
 */
public class Alphabet {
    private final char[] symbols;
    private final int size;

    /**
     * Создает алфавит из массива символов.
     *
     * @param symbols массив символов алфавита
     * @throws IllegalArgumentException если symbols пустой или null
     */
    public Alphabet(char[] symbols) {
        if (symbols == null || symbols.length == 0) {
            throw new IllegalArgumentException("Alphabet symbols cannot be null or empty");
        }
        this.symbols = symbols.clone(); // Защитная копия
        this.size = symbols.length;
    }

    public char[] getSymbols() {
        return symbols.clone(); // Возвращаем копию для иммутабельности
    }

    public int getSize() {
        return size;
    }

    /**
     * Возвращает индекс символа в алфавите.
     *
     * @param symbol символ для поиска
     * @return индекс символа или -1 если символ не найден
     */
    public int indexOf(char symbol) {
        for (int i = 0; i < size; i++) {
            if (symbols[i] == symbol) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Возвращает символ по индексу с учетом циклического сдвига.
     *
     * @param index индекс символа (может быть отрицательным или больше размера)
     * @return символ алфавита
     */
    public char getSymbol(int index) {
        // Нормализация индекса для циклического сдвига
        int normalizedIndex = ((index % size) + size) % size;
        return symbols[normalizedIndex];
    }

    /**
     * Проверяет, содержится ли символ в алфавите.
     */
    public boolean contains(char symbol) {
        return indexOf(symbol) != -1;
    }
}
