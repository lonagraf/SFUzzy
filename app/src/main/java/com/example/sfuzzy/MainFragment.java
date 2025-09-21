package com.example.sfuzzy;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainFragment extends Fragment {

    ShapeableImageView profileImage;
    TextView userName, mail;

    private Map<String, String> wordMap = new LinkedHashMap<>();
    private List<String> englishWords = new ArrayList<>();
    private int currentIndex = 0;

    private TextView wordLabel, feedbackLabel;
    private EditText inputField;
    private Button submitButton;


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
        profileImage = view.findViewById(R.id.profileImage);
        userName = view.findViewById(R.id.userName);
        mail = view.findViewById(R.id.mail);
        wordLabel = view.findViewById(R.id.wordLabel);
        inputField = view.findViewById(R.id.inputField);
        submitButton = view.findViewById(R.id.submitButton);
        feedbackLabel = view.findViewById(R.id.feedbackLabel);

        wordMap.put("cat", "кот");
        wordMap.put("dog", "собака");
        wordMap.put("sun", "солнце");

        englishWords.addAll(wordMap.keySet());
        Collections.shuffle(englishWords);

        loadNextWord();

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


    private void loadNextWord() {
        if (currentIndex < englishWords.size()) {
            String word = englishWords.get(currentIndex);
            wordLabel.setText("Переведите: " + word);
            inputField.setText("");
            feedbackLabel.setText("");
        } else {
            wordLabel.setText("Поздравляем!");
            inputField.setEnabled(false);
            submitButton.setEnabled(false);
            feedbackLabel.setText("Вы перевели все слова!");
        }
    }

    private void checkTranslation() {
        //получаем строку из EditText
        String userInput = inputField.getText().toString().trim().toLowerCase(Locale.ROOT);


        if (currentIndex >= englishWords.size()) return;

        String correctTranslation = wordMap.get(englishWords.get(currentIndex));

        if (correctTranslation != null && userInput.equals(correctTranslation)) {
            feedbackLabel.setText("Верно!");
            currentIndex++;
            loadNextWord();
        } else {
            feedbackLabel.setText("Неправильно. Попробуйте ещё.");
        }
    }

}