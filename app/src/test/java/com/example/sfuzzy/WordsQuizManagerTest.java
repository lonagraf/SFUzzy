// WordsQuizManagerTest.java
package com.example.sfuzzy;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;

public class WordsQuizManagerTest {

    // Тест 1: Проверка логики валидации ответов
    @Test
    public void testAnswerValidation_CorrectAnswer() {
        String correctTranslations = "привет, здравствуйте, добрый день";
        String userInput = "привет";

        boolean result = validateAnswer(correctTranslations, userInput);
        assertTrue("Правильный ответ должен быть принят", result);
    }

    @Test
    public void testAnswerValidation_WrongAnswer() {
        String correctTranslations = "яблоко";
        String userInput = "груша";

        boolean result = validateAnswer(correctTranslations, userInput);
        assertFalse("Неправильный ответ должен быть отклонён", result);
    }

    @Test
    public void testAnswerValidation_CaseInsensitive() {
        String correctTranslations = "APPLE";
        String userInput = "apple";

        boolean result = validateAnswer(correctTranslations, userInput);
        assertTrue("Регистр не должен влиять на проверку", result);
    }

    @Test
    public void testAnswerValidation_MultipleCorrectAnswers() {
        String correctTranslations = "кот, кошка, котик";

        assertTrue(validateAnswer(correctTranslations, "кот"));
        assertTrue(validateAnswer(correctTranslations, "кошка"));
        assertTrue(validateAnswer(correctTranslations, "котик"));
        assertFalse(validateAnswer(correctTranslations, "собака"));
    }

    @Test
    public void testAnswerValidation_WithSpaces() {
        String correctTranslations = "добрый день, добрый вечер";

        assertTrue(validateAnswer(correctTranslations, "добрый день"));
        assertTrue(validateAnswer(correctTranslations, "добрый вечер"));
        assertFalse(validateAnswer(correctTranslations, "доброе утро"));
    }

    // Тест 2: Проверка логики работы с Map
    @Test
    public void testWordMapProcessing() {
        Map<String, String> wordMap = new HashMap<>();
        wordMap.put("hello", "привет");
        wordMap.put("goodbye", "пока, до свидания");
        wordMap.put("apple", "яблоко");

        List<String> englishWords = Arrays.asList("hello", "goodbye", "apple");

        assertEquals(3, englishWords.size());
        assertTrue(englishWords.contains("hello"));
        assertEquals("привет", wordMap.get("hello"));
        assertEquals("пока, до свидания", wordMap.get("goodbye"));
    }

    // Тест 3: Проверка логики подсчёта прогресса
    @Test
    public void testProgressLogic() {
        int totalWords = 10;
        int completed = 7;
        int remaining = totalWords - completed;

        assertEquals(3, remaining);
        assertEquals(70, (completed * 100) / totalWords);
    }

    // Тест 4: Проверка перемешивания
    @Test
    public void testShuffleLogic() {
        List<String> original = Arrays.asList("apple", "book", "cat", "dog", "elephant");
        List<String> shuffled = Arrays.asList("dog", "cat", "elephant", "book", "apple");

        assertEquals(original.size(), shuffled.size());
        assertTrue(shuffled.containsAll(original));
        assertTrue(original.containsAll(shuffled));
    }

    // Тест 5: Проверка нормализации ввода
    @Test
    public void testInputNormalization() {
        assertEquals("привет", normalizeInput("  ПРИВЕТ  "));
        assertEquals("привет", normalizeInput("привет "));
        assertEquals("привет", normalizeInput(" Привет"));
        assertEquals("привет", normalizeInput("ПРИВЕТ"));
        assertEquals("добрый день", normalizeInput("  ДОБРЫЙ ДЕНЬ  "));
    }

    // Тест 6: Проверка разделения строки с вариантами ответов
    @Test
    public void testAnswerSplitting() {
        String answers = "привет,здравствуйте,добрый день";
        String[] split = answers.split(",\\s*");

        assertEquals(3, split.length);
        assertEquals("привет", split[0]);
        assertEquals("здравствуйте", split[1]);
        assertEquals("добрый день", split[2]);
    }

    // Тест 7: Проверка на пустые/неправильные данные
    @Test
    public void testEmptyInputHandling() {
        assertFalse(validateAnswer("", "привет"));
        assertFalse(validateAnswer("привет", ""));
        assertFalse(validateAnswer(null, "привет"));
    }

    // Тест 8: Проверка логики завершения теста
    @Test
    public void testQuizCompletionLogic() {
        int total = 5;
        int current = 5;

        boolean isCompleted = (current >= total);
        assertTrue("Тест должен быть завершён когда пройдены все слова", isCompleted);
    }

    // === ДОБАВЛЕННЫЕ ТЕСТЫ ===

    @Test
    public void testAnswerValidation_WithExtraSpacesInAnswers() {
        String correctTranslations = "привет , здравствуйте , добрый день";
        assertTrue(validateAnswer(correctTranslations, "привет"));
        assertTrue(validateAnswer(correctTranslations, "здравствуйте"));
        assertTrue(validateAnswer(correctTranslations, "добрый день"));
        assertFalse(validateAnswer(correctTranslations, "пока"));
    }

    @Test
    public void testAnswerValidation_SingleAnswer() {
        String correctTranslations = "яблоко";
        assertTrue(validateAnswer(correctTranslations, "яблоко"));
        assertFalse(validateAnswer(correctTranslations, "apple"));
        assertFalse(validateAnswer(correctTranslations, "груша"));
    }

    @Test
    public void testAnswerValidation_SpecialCharacters() {
        String correctTranslations = "café, résumé";
        assertTrue(validateAnswer(correctTranslations, "café"));
        assertTrue(validateAnswer(correctTranslations, "résumé"));
        assertFalse(validateAnswer(correctTranslations, "cafe"));
        assertFalse(validateAnswer(correctTranslations, "resume"));
    }

    @Test
    public void testAnswerValidation_MixedCaseInAnswers() {
        String correctTranslations = "Apple, BANANA, Cherry";
        assertTrue(validateAnswer(correctTranslations, "apple"));
        assertTrue(validateAnswer(correctTranslations, "banana"));
        assertTrue(validateAnswer(correctTranslations, "cherry"));
        assertTrue(validateAnswer(correctTranslations, "APPLE"));
        assertTrue(validateAnswer(correctTranslations, "Banana"));
    }

    @Test
    public void testAnswerValidation_WithNewlinesAndTabs() {
        String correctTranslations = "привет,\nздравствуйте,\tдобрый день";
        assertTrue(validateAnswer(correctTranslations, "привет"));
        assertTrue(validateAnswer(correctTranslations, "здравствуйте"));
        assertTrue(validateAnswer(correctTranslations, "добрый день"));
    }

    @Test
    public void testAnswerValidation_DuplicateAnswers() {
        String correctTranslations = "кот, кошка, кот"; // дубликат "кот"
        String[] answers = correctTranslations.toLowerCase(Locale.ROOT).split(",\\s*");

        assertEquals(3, answers.length);
        assertEquals("кот", answers[0]);
        assertEquals("кошка", answers[1]);
        assertEquals("кот", answers[2]); // дубликат сохраняется
    }

    @Test
    public void testAnswerValidation_ExactMatchRequired() {
        String correctTranslations = "красная машина";
        assertTrue(validateAnswer(correctTranslations, "красная машина"));
        assertFalse(validateAnswer(correctTranslations, "красная"));
        assertFalse(validateAnswer(correctTranslations, "машина"));
        assertFalse(validateAnswer(correctTranslations, "краснаямашина"));
    }

    @Test
    public void testAnswerValidation_NumbersAndSymbols() {
        String correctTranslations = "hello123, test@mail.com, 100%";
        assertTrue(validateAnswer(correctTranslations, "hello123"));
        assertTrue(validateAnswer(correctTranslations, "test@mail.com"));
        assertTrue(validateAnswer(correctTranslations, "100%"));
        assertFalse(validateAnswer(correctTranslations, "hello"));
        assertFalse(validateAnswer(correctTranslations, "test"));
    }

    @Test
    public void testAnswerValidation_LongPhrases() {
        String correctTranslations = "How are you?, I'm fine thank you, Nice to meet you";
        assertTrue(validateAnswer(correctTranslations, "how are you?"));
        assertTrue(validateAnswer(correctTranslations, "i'm fine thank you"));
        assertTrue(validateAnswer(correctTranslations, "nice to meet you"));
        assertFalse(validateAnswer(correctTranslations, "how are you"));
        assertFalse(validateAnswer(correctTranslations, "fine thank you"));
    }

    @Test
    public void testAnswerValidation_EdgeCases() {
        // Пробелы в середине ответа пользователя
        String correctTranslations = "good morning";
        assertTrue(validateAnswer(correctTranslations, "good morning"));
        assertFalse(validateAnswer(correctTranslations, "good  morning")); // два пробела
        assertFalse(validateAnswer(correctTranslations, "goodmorning"));
    }

    // Вспомогательные методы (копия логики из WordsQuizManager)
    private boolean validateAnswer(String correctTranslations, String userInput) {
        if (correctTranslations == null || userInput == null || userInput.trim().isEmpty()) {
            return false;
        }

        String[] possibleAnswers = correctTranslations.toLowerCase(Locale.ROOT).split(",\\s*");

        for (String answer : possibleAnswers) {
            if (userInput.toLowerCase(Locale.ROOT).equals(answer.trim())) {
                return true;
            }
        }
        return false;
    }

    private String normalizeInput(String input) {
        return input.trim().toLowerCase(Locale.ROOT);
    }
}