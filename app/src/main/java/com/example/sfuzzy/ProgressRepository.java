package com.example.sfuzzy;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProgressRepository {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void incrementLessonProgress() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        firestore.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    Map<String, Object> progress = (Map<String, Object>) doc.get("progress");
                    if (progress == null) progress = new HashMap<>();

                    long lessonsCompleted = ((Number) progress.getOrDefault("lessonsCompleted", 0)).longValue();
                    lessonsCompleted++;

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("progress.lessonsCompleted", lessonsCompleted);

                    firestore.collection("users").document(uid).update(updates);
                });
    }
}
