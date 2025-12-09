package com.example.sfuzzy;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import com.google.firebase.database.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseRepositoryTest {

    @Mock FirebaseDatabase mockDb;
    @Mock DatabaseReference mockRef;
    @Mock DataSnapshot mockSnapshot;
    @Mock DatabaseError mockError;

    private DatabaseRepository repository;

    @Before
    public void setup() {
        // Передаём мок FirebaseDatabase в конструктор
        repository = new DatabaseRepository(mockDb);

        // Настройка: любой вызов getReference(...).child(...) возвращает mockRef
        when(mockDb.getReference(anyString())).thenReturn(mockRef);
        when(mockRef.child(anyString())).thenReturn(mockRef);
    }

    //================ loadTheory =================
    @Test
    public void loadTheory_success_callsOnSuccess() {
        when(mockSnapshot.getValue(String.class)).thenReturn("Теория по теме");

        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mockSnapshot);
            return null;
        }).when(mockRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        repository.loadTheory("topic1", new DatabaseRepository.TheoryCallback() {
            @Override
            public void onSuccess(String theoryHtml) {
                assertEquals("Теория по теме", theoryHtml);
            }

            @Override
            public void onError(String error) {
                fail("Не должно вызываться onError");
            }
        });
    }

    @Test
    public void loadTheory_nullValue_callsOnError() {
        when(mockSnapshot.getValue(String.class)).thenReturn(null);

        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mockSnapshot);
            return null;
        }).when(mockRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        repository.loadTheory("topic1", new DatabaseRepository.TheoryCallback() {
            @Override
            public void onSuccess(String theoryHtml) {
                fail("Не должно вызываться onSuccess");
            }

            @Override
            public void onError(String error) {
                assertEquals("Теория отсутствует", error);
            }
        });
    }

    @Test
    public void loadTheory_cancelled_callsOnError() {
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onCancelled(mockError);
            return null;
        }).when(mockRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        when(mockError.getMessage()).thenReturn("Ошибка сети");

        repository.loadTheory("topic1", new DatabaseRepository.TheoryCallback() {
            @Override
            public void onSuccess(String theoryHtml) {
                fail("Не должно вызываться onSuccess");
            }

            @Override
            public void onError(String error) {
                assertEquals("Ошибка сети", error);
            }
        });
    }

    // Новый тест: пустой snapshot
    @Test
    public void loadTheory_emptySnapshot_callsOnError() {
        when(mockSnapshot.getValue(String.class)).thenReturn(null);

        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mockSnapshot);
            return null;
        }).when(mockRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        repository.loadTheory("topic1", new DatabaseRepository.TheoryCallback() {
            @Override
            public void onSuccess(String theoryHtml) {
                fail("Не должно вызываться onSuccess");
            }

            @Override
            public void onError(String error) {
                assertEquals("Теория отсутствует", error);
            }
        });
    }

    //================ loadWords =================
    @Test
    public void loadWords_success_callsOnSuccess() {
        DataSnapshot wordSnap = mock(DataSnapshot.class);
        DataSnapshot trSnap = mock(DataSnapshot.class);

        when(wordSnap.getKey()).thenReturn("apple");
        when(wordSnap.getChildren()).thenReturn(Collections.singletonList(trSnap));
        when(trSnap.getValue(String.class)).thenReturn("яблоко");
        when(mockSnapshot.getChildren()).thenReturn(Collections.singletonList(wordSnap));

        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mockSnapshot);
            return null;
        }).when(mockRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        repository.loadWords("topic1", new DatabaseRepository.WordsCallback() {
            @Override
            public void onSuccess(Map<String, String> words) {
                assertEquals(1, words.size());
                assertEquals("яблоко", words.get("apple"));
            }

            @Override
            public void onError(String error) {
                fail("Не должно вызываться onError");
            }
        });
    }

    // Новый тест: пустой snapshot
    @Test
    public void loadWords_emptySnapshot_callsOnSuccessWithEmptyMap() {
        when(mockSnapshot.getChildren()).thenReturn(Collections.emptyList());

        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mockSnapshot);
            return null;
        }).when(mockRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        repository.loadWords("topic1", new DatabaseRepository.WordsCallback() {
            @Override
            public void onSuccess(Map<String, String> words) {
                assertTrue(words.isEmpty());
            }

            @Override
            public void onError(String error) {
                fail("Не должно вызываться onError");
            }
        });
    }

    // Новый тест: null перевод
    @Test
    public void loadWords_partialNullTranslation_skipsNulls() {
        DataSnapshot wordSnap = mock(DataSnapshot.class);
        DataSnapshot tr1 = mock(DataSnapshot.class);
        DataSnapshot tr2 = mock(DataSnapshot.class);

        when(wordSnap.getKey()).thenReturn("apple");
        when(wordSnap.getChildren()).thenReturn(Arrays.asList(tr1, tr2));
        when(tr1.getValue(String.class)).thenReturn("яблоко");
        when(tr2.getValue(String.class)).thenReturn(null); // null перевод
        when(mockSnapshot.getChildren()).thenReturn(Collections.singletonList(wordSnap));

        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mockSnapshot);
            return null;
        }).when(mockRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        repository.loadWords("topic1", new DatabaseRepository.WordsCallback() {
            @Override
            public void onSuccess(Map<String, String> words) {
                assertEquals(1, words.size());
                assertEquals("яблоко", words.get("apple"));
            }

            @Override
            public void onError(String error) {
                fail("Не должно вызываться onError");
            }
        });
    }

    //================ loadGrammar =================
    @Test
    public void loadGrammar_success_callsOnSuccess() {
        DataSnapshot qSnap = mock(DataSnapshot.class);
        DataSnapshot optSnap = mock(DataSnapshot.class);

        // Моки для child("question")
        DataSnapshot questionSnap = mock(DataSnapshot.class);
        when(qSnap.child("question")).thenReturn(questionSnap);
        when(questionSnap.getValue(String.class)).thenReturn("Вопрос?");

        // Моки для child("options")
        DataSnapshot optionsSnap = mock(DataSnapshot.class);
        when(qSnap.child("options")).thenReturn(optionsSnap);
        when(optionsSnap.getChildren()).thenReturn(Collections.singletonList(optSnap));
        when(optSnap.getValue(String.class)).thenReturn("Опция1");

        // Моки для child("answer")
        DataSnapshot answerSnap = mock(DataSnapshot.class);
        when(qSnap.child("answer")).thenReturn(answerSnap);
        when(answerSnap.getValue(String.class)).thenReturn("Опция1");

        // Главный snapshot возвращает список вопросов
        when(mockSnapshot.getChildren()).thenReturn(Collections.singletonList(qSnap));

        // Настройка addListenerForSingleValueEvent
        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mockSnapshot);
            return null;
        }).when(mockRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        repository.loadGrammar("topic1", new DatabaseRepository.GrammarCallback() {
            @Override
            public void onSuccess(List<GrammarFragment.Question> questions) {
                assertEquals(1, questions.size());
                assertEquals("Вопрос?", questions.get(0).question);
                assertEquals("Опция1", questions.get(0).correctAnswer);
                assertEquals(1, questions.get(0).options.size());
                assertEquals("Опция1", questions.get(0).options.get(0));
            }

            @Override
            public void onError(String error) {
                fail("Не должно вызываться onError");
            }
        });
    }

    // Новый тест: пустой snapshot
    @Test
    public void loadGrammar_emptySnapshot_callsOnSuccessWithEmptyList() {
        when(mockSnapshot.getChildren()).thenReturn(Collections.emptyList());

        doAnswer(invocation -> {
            ValueEventListener listener = invocation.getArgument(0);
            listener.onDataChange(mockSnapshot);
            return null;
        }).when(mockRef).addListenerForSingleValueEvent(any(ValueEventListener.class));

        repository.loadGrammar("topic1", new DatabaseRepository.GrammarCallback() {
            @Override
            public void onSuccess(List<GrammarFragment.Question> questions) {
                assertTrue(questions.isEmpty());
            }

            @Override
            public void onError(String error) {
                fail("Не должно вызываться onError");
            }
        });
    }
}
