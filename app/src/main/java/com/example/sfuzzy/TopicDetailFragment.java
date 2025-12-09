package com.example.sfuzzy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TopicDetailFragment extends Fragment {

    private static final String ARG_TITLE = "topic_name";
    private static final String ARG_IMAGE = "topic_image";

    private String topicName;
    private int topicImageRes;

    public static TopicDetailFragment newInstance(String title, int imageRes) {
        TopicDetailFragment fragment = new TopicDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putInt(ARG_IMAGE, imageRes);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            topicName = getArguments().getString(ARG_TITLE);
            topicImageRes = getArguments().getInt(ARG_IMAGE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_topic_detail, container, false);


        TextView tvTitle = view.findViewById(R.id.tvTopicTitle);
        ImageView ivTopic = view.findViewById(R.id.ivTopicImage);

        Button btnWords = view.findViewById(R.id.btnWords);
        Button btnTheory = view.findViewById(R.id.btnTheory);
        Button btnGrammar = view.findViewById(R.id.btnGrammar);
        Button btnBack = view.findViewById(R.id.btnBack);

        tvTitle.setText(topicName);
        ivTopic.setImageResource(topicImageRes);

        btnWords.setOnClickListener(v -> openFragment(WordsFragment.newInstance(topicName)));
        btnTheory.setOnClickListener(v -> openFragment(TheoryFragment.newInstance(topicName)));
        btnGrammar.setOnClickListener(v -> openFragment(GrammarFragment.newInstance(topicName)));

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return view;
    }

    private void openFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_view_tag, fragment)
                .addToBackStack(null)
                .commit();
    }
}
