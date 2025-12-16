package com.example.sfuzzy;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProgressRepository {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public ProgressRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public ProgressRepository(FirebaseFirestore db, FirebaseAuth auth) {
        this.db = db;
        this.auth = auth;
    }

    public void incrementLessonProgress() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        if (uid.isEmpty()) return;  // безопасная проверка

        Map<String, Object> update = new HashMap<>();
        update.put("progress.lessonsCompleted", FieldValue.increment(1));

        DocumentReference docRef = db.collection("users").document(uid);
        docRef.update(update);
    }
}
