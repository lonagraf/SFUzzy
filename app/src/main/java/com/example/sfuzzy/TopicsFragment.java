package com.example.sfuzzy;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class TopicsFragment extends Fragment {

    public TopicsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topics, container, false);

        Button wordsButton = view.findViewById(R.id.wordsButton);
        Button grammarButton = view.findViewById(R.id.grammarButton);
        Button theoryButton = view.findViewById(R.id.theoryButton);

        wordsButton.setOnClickListener(v -> openFragment(new MainFragment())); // слова
        // grammarButton.setOnClickListener(v -> openFragment(new GrammarFragment())); // грамматика
        theoryButton.setOnClickListener(v -> openFragment(new TheoryFragment())); // теория

        return view;
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_view_tag, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
