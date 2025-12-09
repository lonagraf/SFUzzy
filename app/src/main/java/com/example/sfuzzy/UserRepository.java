package com.example.sfuzzy;

import androidx.annotation.Nullable;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

public class UserRepository {

    private final FirebaseFirestore db;

    // Конструктор по умолчанию для продакшена
    public UserRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // Конструктор для тестов
    public UserRepository(FirebaseFirestore db) {
        this.db = db;
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    // Загружает документ пользователя или создаёт, если его нет
    public void loadOrCreateUser(String uid, String email, UserCallback callback) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Map<String, Object> initData = new HashMap<>();
                        initData.put("email", email);
                        initData.put("name", "");
                        Map<String, Object> progress = new HashMap<>();
                        progress.put("lessonsCompleted", 0);
                        initData.put("progress", progress);

                        db.collection("users")
                                .document(uid)
                                .set(initData, SetOptions.merge());
                    }
                    callback.onLoaded(doc);
                })
                .addOnFailureListener(callback::onError);
    }

    public ListenerRegistration subscribeToProgress(String uid, ProgressCallback callback) {
        return db.collection("users").document(uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (snapshot != null && snapshot.exists()) {
                        Object obj = snapshot.get("progress");
                        Map<String, Object> progress;
                        if (obj instanceof Map) {
                            progress = (Map<String, Object>) obj;
                        } else {
                            progress = new HashMap<>();
                        }
                        long lessons = ((Number) progress.getOrDefault("lessonsCompleted", 0)).longValue();
                        callback.onProgress(lessons);
                    }
                });
    }

    public void updateUserName(String uid, String newName, SimpleCallback callback) {
        if (newName == null || newName.trim().isEmpty()) {
            callback.onResult(false);
            return;
        }

        Map<String, Object> update = new HashMap<>();
        update.put("name", newName);

        db.collection("users").document(uid)
                .update(update)
                .addOnSuccessListener(aVoid -> callback.onResult(true))
                .addOnFailureListener(e -> callback.onResult(false));
    }

    public interface UserCallback {
        void onLoaded(DocumentSnapshot doc);
        void onError(Exception e);
    }

    public interface ProgressCallback {
        void onProgress(long lessonsCompleted);
    }

    public interface SimpleCallback {
        void onResult(boolean success);
    }


}
