package com.example.sfuzzy;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainFragment extends Fragment {

    ShapeableImageView profileImage;
    TextView userName, mail;

    private Map<String, String> wordMap = new LinkedHashMap<>();
    private List<String> englishWords = new ArrayList<>();
    private int currentIndex = 0;

    private TextView wordLabel, feedbackLabel;
    private EditText inputField;
    private Button submitButton, exitButton;

    FirebaseDatabase db = FirebaseDatabase.getInstance("https://sfuzzy-93892-default-rtdb.asia-southeast1.firebasedatabase.app/");
    DatabaseReference dbRef;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        exitButton = view.findViewById(R.id.exitButton);
        profileImage = view.findViewById(R.id.profileImage);
        userName = view.findViewById(R.id.userName);
        mail = view.findViewById(R.id.mail);
        wordLabel = view.findViewById(R.id.wordLabel);
        inputField = view.findViewById(R.id.inputField);
        submitButton = view.findViewById(R.id.submitButton);
        feedbackLabel = view.findViewById(R.id.feedbackLabel);

        // Инициализируем Firebase
        db = FirebaseDatabase.getInstance("https://sfuzzy-93892-default-rtdb.asia-southeast1.firebasedatabase.app/");

        // Сначала проверим всю структуру базы данных
        checkDatabaseStructure();

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();

                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container_view_tag, new LoginFragment())
                            .commit();
                    getActivity().getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkTranslation();
            }
        });

        if (getArguments() != null){
            String name = getArguments().getString("userName");
            String email = getArguments().getString("userEmail");
            String photoUrl = getArguments().getString("photoUrl");

            if (name != null){
                userName.setText(name);
            }
            if (email != null){
                mail.setText(email);
            }
            if (photoUrl != null && !photoUrl.isEmpty()){
                Glide.with(this)
                        .load(photoUrl)
                        .into(profileImage);
            }
        }
        return view;
    }

    private void checkDatabaseStructure() {
        System.out.println("=== CHECKING DATABASE STRUCTURE ===");
        DatabaseReference rootRef = db.getReference();

        feedbackLabel.setText("Проверка структуры базы данных...");

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("=== FULL DATABASE STRUCTURE ===");
                System.out.println("Root exists: " + dataSnapshot.exists());
                System.out.println("Root children count: " + dataSnapshot.getChildrenCount());

                // Выводим всю структуру базы данных
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String key = child.getKey();
                    Object value = child.getValue();
                    System.out.println("Key: " + key + ", Value type: " + (value != null ? value.getClass().getSimpleName() : "null"));
                    System.out.println("Full value: " + value);

                    // Если это объект, покажем его внутреннюю структуру
                    if (value instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) value;
                        System.out.println("Map contents: " + map);
                    }
                }
                System.out.println("=== END DATABASE STRUCTURE ===");

                // Теперь попробуем найти слова разными путями
                findWordsInDatabase(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Error reading database structure: " + databaseError.getMessage());
                feedbackLabel.setText("Ошибка чтения структуры базы: " + databaseError.getMessage());
            }
        });
    }

    private void findWordsInDatabase(DataSnapshot rootSnapshot) {
        System.out.println("=== SEARCHING FOR WORDS ===");

        // Проверяем путь "words"
        DataSnapshot wordsSnapshot = rootSnapshot.child("words");
        if (wordsSnapshot.exists()) {
            System.out.println("Found existing words at path: words");
            loadWordsFromPath(wordsSnapshot, "words");
            return;
        }

    }

    private boolean isLikelyWordMap(Map<?, ?> map) {
        // Проверяем, похоже ли это на словарь слов
        if (map.isEmpty()) return false;

        for (Object key : map.keySet()) {
            if (key instanceof String) {
                String strKey = (String) key;
                // Если ключ - английское слово (только буквы), вероятно это наш словарь
                if (strKey.matches("[a-zA-Z]+")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void loadWordsFromPath(DataSnapshot wordsSnapshot, String path) {
        System.out.println("Loading words from path: " + path);
        dbRef = db.getReference(path);

        wordMap.clear();
        englishWords.clear();

        for (DataSnapshot wordSnapshot : wordsSnapshot.getChildren()) {
            String englishWord = wordSnapshot.getKey();
            System.out.println("Processing word: " + englishWord);

            try {
                Object value = wordSnapshot.getValue();
                System.out.println("Value type: " + (value != null ? value.getClass().getSimpleName() : "null"));

                if (value instanceof List) {
                    // Обработка массива переводов
                    List<?> rawList = (List<?>) value;
                    List<String> translations = new ArrayList<>();
                    for (Object item : rawList) {
                        if (item instanceof String) {
                            translations.add((String) item);
                        }
                    }
                    if (!translations.isEmpty()) {
                        String joinedTranslations = String.join(", ", translations);
                        wordMap.put(englishWord, joinedTranslations);
                        System.out.println("Added: " + englishWord + " -> " + joinedTranslations);
                    }
                } else if (value instanceof String) {
                    // Обработка строки
                    wordMap.put(englishWord, (String) value);
                    System.out.println("Added: " + englishWord + " -> " + value);
                }
            } catch (Exception e) {
                System.out.println("Error processing word " + englishWord + ": " + e.getMessage());
            }
        }

        if (wordMap.isEmpty()) {
            System.out.println("No words processed from path: " + path);
            feedbackLabel.setText("Не удалось загрузить слова из пути: " + path);
            return;
        }

        englishWords.addAll(wordMap.keySet());
        Collections.shuffle(englishWords);
        currentIndex = 0;

        System.out.println("Successfully loaded " + englishWords.size() + " words from path: " + path);

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                loadNextWord();
                feedbackLabel.setText("Загружено слов: " + englishWords.size() + " из " + path);
            });
        }
    }


    private void loadWordsFromFirebase() {
        System.out.println("Loading words from Firebase path: words");
        dbRef = db.getReference("words");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                loadWordsFromPath(dataSnapshot, "words");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Error loading from words path: " + databaseError.getMessage());
                feedbackLabel.setText("Ошибка загрузки: " + databaseError.getMessage());
            }
        });
    }

    private void loadNextWord() {
        System.out.println("Loading next word. Current index: " + currentIndex + ", Total: " + englishWords.size());

        if (englishWords.isEmpty()) {
            wordLabel.setText("Слова не загружены");
            feedbackLabel.setText("Попробуйте снова");
            return;
        }

        if (currentIndex < englishWords.size()) {
            String word = englishWords.get(currentIndex);
            wordLabel.setText("Переведите: " + word);
            inputField.setText("");
            feedbackLabel.setText("Слово " + (currentIndex + 1) + " из " + englishWords.size());
        } else {
            wordLabel.setText("Поздравляем!");
            inputField.setEnabled(false);
            submitButton.setEnabled(false);
            feedbackLabel.setText("Вы завершили все слова! (" + englishWords.size() + " слов)");
        }
    }

    private void checkTranslation() {
        String userInput = inputField.getText().toString().trim().toLowerCase(Locale.ROOT);
        if (currentIndex >= englishWords.size()) return;

        String word = englishWords.get(currentIndex);
        String correctTranslations = wordMap.get(word);

        if (correctTranslations == null) return;

        String[] possibleAnswers = correctTranslations.toLowerCase(Locale.ROOT).split(",\\s*");

        boolean isCorrect = false;
        for (String answer : possibleAnswers) {
            if (userInput.equals(answer.trim())) {
                isCorrect = true;
                break;
            }
        }

        if (isCorrect) {
            feedbackLabel.setText("Верно!");
            currentIndex++;
            loadNextWord();
        } else {
            feedbackLabel.setText("Неправильно. Возможные ответы: " + correctTranslations);
        }
    }
}