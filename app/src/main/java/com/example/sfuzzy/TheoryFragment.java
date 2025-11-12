package com.example.sfuzzy;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TheoryFragment extends Fragment {

    public TheoryFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Используем XML-разметку для этого фрагмента
        return inflater.inflate(R.layout.fragment_theory, container, false);
    }
}
