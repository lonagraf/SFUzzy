package com.example.sfuzzy;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DatabaseRepository {

    private final FirebaseDatabase db;

    // Обычный конструктор для приложения
    public DatabaseRepository() {
        this.db = FirebaseDatabase.getInstance(
                "https://sfuzzy-93892-default-rtdb.asia-southeast1.firebasedatabase.app/"
        );
    }

    // Конструктор для тестов, чтобы подставлять мок
    public DatabaseRepository(FirebaseDatabase db) {
        this.db = db;
    }

    public interface TheoryCallback {
        void onSuccess(String theoryHtml);
        void onError(String error);
    }

    public interface WordsCallback {
        void onSuccess(Map<String, String> words);
        void onError(String error);
    }

    public interface GrammarCallback {
        void onSuccess(List<GrammarFragment.Question> questions);
        void onError(String error);
    }

    public void loadTheory(String topicName, TheoryCallback callback) {
        DatabaseReference ref = db.getReference("topics").child(topicName).child("theory");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theory = snapshot.getValue(String.class);
                if (theory != null) {
                    callback.onSuccess(theory);
                } else {
                    callback.onError("Теория отсутствует");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void loadWords(String topicName, WordsCallback callback) {
        DatabaseReference ref = db.getReference("topics").child(topicName).child("words");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, String> wordMap = new LinkedHashMap<>();
                for (DataSnapshot wordSnap : snapshot.getChildren()) {
                    String englishWord = wordSnap.getKey();
                    List<String> translations = new ArrayList<>();
                    for (DataSnapshot t : wordSnap.getChildren()) {
                        String tr = t.getValue(String.class);
                        if (tr != null) translations.add(tr);
                    }
                    if (englishWord != null && !translations.isEmpty()) {
                        wordMap.put(englishWord, String.join(", ", translations));
                    }
                }
                callback.onSuccess(wordMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void loadGrammar(String topicName, GrammarCallback callback) {
        DatabaseReference ref = db.getReference("topics").child(topicName).child("grammar");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<GrammarFragment.Question> questions = new ArrayList<>();
                for (DataSnapshot qSnap : snapshot.getChildren()) {
                    String questionText = qSnap.child("question").getValue(String.class);
                    List<String> options = new ArrayList<>();
                    for (DataSnapshot optSnap : qSnap.child("options").getChildren()) {
                        options.add(optSnap.getValue(String.class));
                    }
                    String correct = qSnap.child("answer").getValue(String.class);
                    questions.add(new GrammarFragment.Question(questionText, options, correct));
                }
                callback.onSuccess(questions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}
