package com.example.sfuzzy;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

public class LevelsFragment extends Fragment {

    public LevelsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_levels, container, false);
        LinearLayout levelsContainer = view.findViewById(R.id.levelsContainer);

        // Генерируем кнопки уровней 1–5
        for (int i = 1; i <= 5; i++) {
            Button levelButton = new Button(requireContext());
            levelButton.setText("Уровень " + i);
            levelButton.setAllCaps(false);
            levelButton.setTextColor(Color.WHITE);
            levelButton.setBackgroundColor(Color.parseColor("#FFA500")); // оранжевый
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 16, 0, 16); // отступы между кнопками
            levelButton.setLayoutParams(params);

            int finalI = i;
            levelButton.setOnClickListener(v -> openTopicsFragment(finalI));

            levelsContainer.addView(levelButton);
            Log.d("LevelsFragment", "Добавлена кнопка уровня " + i);
        }

        return view;
    }

    private void openTopicsFragment(int levelNumber) {
        TopicsFragment topicsFragment = new TopicsFragment();

        // Передаём выбранный уровень через Bundle
        Bundle args = new Bundle();
        args.putInt("selectedLevel", levelNumber);
        topicsFragment.setArguments(args);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_view_tag, topicsFragment);
        transaction.addToBackStack(null);
        transaction.commit();

        Log.d("LevelsFragment", "Переход в TopicsFragment с уровнем " + levelNumber);
    }
}
