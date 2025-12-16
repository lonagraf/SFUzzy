package com.example.sfuzzy;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

public class ProfileFragment extends Fragment {

    private TextView tvUserName;
    private TextView tvLessons;
    private Button btnLogout;

    private FirebaseAuth mAuth;
    private UserRepository userRepository;
    private ListenerRegistration progressListener;

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();

        TextView tvEmail = view.findViewById(R.id.tvEmail);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvLessons = view.findViewById(R.id.tvStat1);
        btnLogout = view.findViewById(R.id.btnLogout);

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            tvEmail.setText(user.getEmail());
            loadUserData(user);
        } else {
            Toast.makeText(getContext(), "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
        }

        setupNameClick();
        setupLogout();

        Button btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        return view;
    }

    private void loadUserData(FirebaseUser user) {
        userRepository.loadOrCreateUser(
                user.getUid(),
                user.getEmail(),
                new UserRepository.UserCallback() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onLoaded(com.google.firebase.firestore.DocumentSnapshot doc) {
                        String name = doc.getString("name");
                        if (name != null && !name.isEmpty()) {
                            tvUserName.setText(name);
                        }

                        // подписка на прогресс
                        progressListener = userRepository.subscribeToProgress(
                                user.getUid(),
                                lessons -> tvLessons.setText("Lessons completed: " + lessons)
                        );
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getContext(),
                                "Ошибка загрузки данных",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupNameClick() {
        tvUserName.setOnClickListener(v -> {
            EditText editText = new EditText(getContext());
            editText.setText(tvUserName.getText().toString());

            new AlertDialog.Builder(requireContext())
                    .setTitle("Изменить имя")
                    .setView(editText)
                    .setPositiveButton("Сохранить", (dialog, which) ->
                            saveUserName(editText.getText().toString()))
                    .setNegativeButton("Отмена", null)
                    .show();
        });
    }

    private void saveUserName(String name) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        userRepository.updateUserName(
                user.getUid(),
                name,
                success -> {
                    if (success) {
                        tvUserName.setText(name);
                    } else {
                        Toast.makeText(getContext(),
                                "Ошибка при сохранении имени",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupLogout() {
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            if (progressListener != null) progressListener.remove();

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_view_tag, new LoginFragment())
                    .commit();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressListener != null) {
            progressListener.remove();
        }
    }
}
