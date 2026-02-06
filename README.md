# Caesar Cipher Application

Итоговый проект первого модуля курса JavaRush. Реализация шифра Цезаря с несколькими режимами работы.

[Техническое задание и рекомендации](Technical_specification.md) · [Архитектура и компоненты](ARCHITECTURE.md)

[![Java CI](https://github.com/Aberezhnoy1980/CryptAnalyzer/actions/workflows/build.yml/badge.svg)](https://github.com/Aberezhnoy1980/CryptAnalyzer/actions/workflows/build.yml)
[![Java Version](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/projects/jdk/21/)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-orange.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## Режимы работы

- **Шифрование** — файл + ключ (сдвиг) → зашифрованный файл  
- **Расшифровка по ключу** — зашифрованный файл + ключ → исходный текст  
- **Bruteforce** — перебор ключей с оценкой «похожести на язык»  
- **Статистический анализ** — подбор ключа по частоте символов (опционально с эталонным текстом)

Запуск: `java -jar target/cryptanalyzer-1.0-SNAPSHOT-jar-with-dependencies.jar <команда> ...`  
Справка: `caesar --help`, `caesar encrypt --help` и т.д.

## Архитектура (кратко)

Четыре слоя: **Presentation** (CLI) → **Application** (оркестрация) → **Domain** (логика шифра, brute force, статистика) ← **Infrastructure** (файлы, конфиг).

Domain не зависит от Infrastructure: в domain объявлены порты **FileProcessor** и **AlphabetProvider**, их реализации (EfficientFileProcessor, AlphabetConfigProvider) живут в infrastructure. Сборка зависимостей выполняется в **CaesarCliApp** (composition root): создаются провайдер алфавита и файловый процессор, затем ими инициализируется CipherService и передаётся в CLI.

Подробнее: схема слоёв, диаграмма компонентов и связи между ними — в [ARCHITECTURE.md](ARCHITECTURE.md).

## Технологии и конфигурация

- **Java 21**, Maven  
- **Конфигурация**: `application.properties` + системные свойства; алфавит, буфер, кодировка, защищённые пути — см. `ApplicationConfig`, `AlphabetConfig`, `SecurityConfig`.  
- **Логирование**: Log4j2; в консоль идёт только INFO и выше, в файл `logs/caesar-cipher.log` — в т.ч. DEBUG.  
- **Производительность**: NIO, чанковая обработка файлов, алфавит с O(1) поиском (EnhancedAlphabet).

## Сборка и тесты

```bash
mvn clean package
mvn test
```

## CI/CD

GitHub Actions: сборка и тесты на push/PR в `dev` и `main`.

---

Исполнитель: Бережной А.
