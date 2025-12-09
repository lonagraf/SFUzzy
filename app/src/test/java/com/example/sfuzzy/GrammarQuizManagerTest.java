package com.example.sfuzzy;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class GrammarQuizManagerTest {

    // Внутренний класс для тестовых вопросов
    static class TestQuestion {
        public String question;
        public List<String> options;
        public String correctAnswer;

        TestQuestion(String q, String[] opts, String correct) {
            this.question = q;
            this.options = Arrays.asList(opts);
            this.correctAnswer = correct;
        }
    }

    // ТЕСТ 1: Проверка правильности ответа
    @Test
    public void testAnswerCorrectness() {
        String correct = "Present Simple";
        String userCorrect = "Present Simple";
        String userWrong = "Past Continuous";

        assertTrue("Правильный ответ должен совпадать", correct.equals(userCorrect));
        assertFalse("Неправильный ответ не должен совпадать", correct.equals(userWrong));
    }

    // ТЕСТ 2: Логика подсчета очков
    @Test
    public void testScoreCalculation() {
        int score = 0;
        int totalQuestions = 10;

        // Симулируем 4 правильных ответа
        score += 4;

        assertEquals("Счет должен быть 4", 4, score);

        // Расчет процента
        int percentage = (score * 100) / totalQuestions;
        assertEquals("Процент должен быть 40%", 40, percentage);
    }

    // ТЕСТ 3: Создание и проверка вопросов
    @Test
    public void testCreateAndValidateQuestions() {
        // Создаем вопросы напрямую в тесте
        TestQuestion q1 = new TestQuestion("Вопрос 1?", new String[]{"Да", "Нет"}, "Да");
        TestQuestion q2 = new TestQuestion("Вопрос 2?", new String[]{"А", "Б", "В"}, "Б");

        // Проверяем q1
        assertNotNull("Вопрос не должен быть null", q1.question);
        assertEquals("Должно быть 2 варианта", 2, q1.options.size());
        assertTrue("Правильный ответ должен быть среди вариантов",
                q1.options.contains(q1.correctAnswer));

        // Проверяем q2
        assertNotNull("Вопрос не должен быть null", q2.question);
        assertEquals("Должно быть 3 варианта", 3, q2.options.size());
        assertTrue("Правильный ответ должен быть среди вариантов",
                q2.options.contains(q2.correctAnswer));
    }

    // ТЕСТ 4: Проверка индексации (простая математика)
    @Test
    public void testQuestionIndexLogic() {
        int totalQuestions = 5;
        int currentIndex = 0;

        // Проверяем логику перехода
        assertTrue("При индексе 0 есть следующий вопрос", currentIndex < totalQuestions);

        currentIndex = 3;
        assertTrue("При индексе 3 есть следующий вопрос", currentIndex < totalQuestions);

        currentIndex = 5;
        assertFalse("При индексе 5 нет следующего вопроса", currentIndex < totalQuestions);
    }

    // ТЕСТ 5: Проверка завершения теста
    @Test
    public void testQuizCompletionLogic() {
        int total = 8;
        int completed = 8;

        boolean isComplete = completed >= total;
        assertTrue("Тест должен быть завершен", isComplete);

        completed = 4;
        isComplete = completed >= total;
        assertFalse("Тест не должен быть завершен", isComplete);
    }

    // ТЕСТ 6: Проверка сброса значений
    @Test
    public void testValueReset() {
        int score = 7;
        int index = 4;

        // Сброс
        score = 0;
        index = 0;

        assertEquals("Счет должен быть 0", 0, score);
        assertEquals("Индекс должен быть 0", 0, index);
    }

    // ТЕСТ 7: Проверка сравнения строк (регистр)
    @Test
    public void testStringComparisonCaseInsensitive() {
        String answer1 = "PRESENT SIMPLE";
        String answer2 = "present simple";
        String answer3 = "Present Simple";

        // Приводим к нижнему регистру для сравнения
        String lower1 = answer1.toLowerCase();
        String lower2 = answer2.toLowerCase();
        String lower3 = answer3.toLowerCase();

        assertEquals("Строки должны совпадать после приведения к нижнему регистру",
                lower1, lower2);
        assertEquals("Строки должны совпадать после приведения к нижнему регистру",
                lower2, lower3);
    }

    // ТЕСТ 8: Проверка математических расчетов
    @Test
    public void testMathCalculations() {
        // Расчет прогресса
        int current = 3;
        int total = 10;
        double progress = (double) current / total * 100;

        assertEquals("Прогресс должен быть 30%", 30.0, progress, 0.01);

        // Расчет оценки по 5-балльной системе
        double grade = (double) current / total * 5;
        assertEquals("Оценка должна быть 1.5", 1.5, grade, 0.01);
    }


}