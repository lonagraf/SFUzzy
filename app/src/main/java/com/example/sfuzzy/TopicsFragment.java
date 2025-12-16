package com.example.sfuzzy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TopicsFragment extends Fragment {

    private TextView tvGreeting;


    private TextView tvTopic1, tvTopic2, tvTopic3, tvTopic4;
    private Button btnOpen1, btnOpen2, btnOpen3, btnOpen4;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topics, container, false);

        // Greeting
        tvGreeting = view.findViewById(R.id.tvGreeting);

        Button btnProfile = view.findViewById(R.id.btnProfile);

        btnProfile.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_view_tag, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });


        tvGreeting.setText("Hi!"); // Можно динамически менять имя

        // Topic 1
        tvTopic1 = view.findViewById(R.id.tvTopicName);
        btnOpen1 = view.findViewById(R.id.btnOpen);
        btnOpen1.setOnClickListener(v ->
                openTopic(tvTopic1.getText().toString(), R.drawable.basics)
        );

        // Topic 2
        tvTopic2 = view.findViewById(R.id.tvTopicName2);
        btnOpen2 = view.findViewById(R.id.btnOpen2);
        btnOpen2.setOnClickListener(v ->
                openTopic(tvTopic2.getText().toString(), R.drawable.family)
        );

        // Topic 3
        tvTopic3 = view.findViewById(R.id.tvTopicName3);
        btnOpen3 = view.findViewById(R.id.btnOpen3);
        btnOpen3.setOnClickListener(v ->
                openTopic(tvTopic3.getText().toString(), R.drawable.food)
        );

        // Topic 4
        tvTopic4 = view.findViewById(R.id.tvTopicName4);
        btnOpen4 = view.findViewById(R.id.btnOpen4);
        btnOpen4.setOnClickListener(v ->
                openTopic(tvTopic4.getText().toString(), R.drawable.travel)
        );

        return view;
    }

    private void openTopic(String topicName, int imageResId) {
        TopicDetailFragment detailFragment = TopicDetailFragment.newInstance(topicName, imageResId);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_view_tag, detailFragment)
                .addToBackStack(null)
                .commit();
    }
}
