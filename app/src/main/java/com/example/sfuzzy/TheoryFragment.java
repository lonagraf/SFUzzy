package com.example.sfuzzy;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TheoryFragment extends Fragment {

    private static final String ARG_TOPIC_NAME = "topic_name";
    private String topicName;
    private TextView tvTheoryContent;

    public static TheoryFragment newInstance(String topicName) {
        TheoryFragment fragment = new TheoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TOPIC_NAME, topicName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            topicName = getArguments().getString(ARG_TOPIC_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_theory, container, false);
        tvTheoryContent = view.findViewById(R.id.tvTheoryContent);

        // Загружаем теорию из Realtime Database
        loadTheoryFromRealtimeDatabase();

        Button btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return view;
    }

    private void loadTheoryFromRealtimeDatabase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("topics").child(topicName);

        databaseRef.child("theory").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theoryText = snapshot.getValue(String.class);
                if (theoryText != null) {
                    tvTheoryContent.setText(Html.fromHtml(theoryText, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    tvTheoryContent.setText("Теория пока не добавлена");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvTheoryContent.setText("Ошибка загрузки теории: " + error.getMessage());
            }
        });
    }
}
