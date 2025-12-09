package com.example.sfuzzy;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class ProgressTest {

    @Mock FirebaseFirestore firestore;
    @Mock FirebaseAuth auth;
    @Mock FirebaseUser user;
    @Mock CollectionReference usersCollection;
    @Mock DocumentReference userDocument;

    private ProgressRepository repository;

    @Before
    public void setup() {
        // Настраиваем моки для обычного пользователя
        when(auth.getCurrentUser()).thenReturn(user);
        when(user.getUid()).thenReturn("test_user_123");
        when(firestore.collection("users")).thenReturn(usersCollection);
        when(usersCollection.document("test_user_123")).thenReturn(userDocument);

        repository = new ProgressRepository(firestore, auth);
    }

    @Test
    public void incrementLessonProgress_callsUpdate() {
        repository.incrementLessonProgress();
        verify(userDocument, times(1)).update(any(Map.class));
    }

    @Test
    public void incrementLessonProgress_userNull_doesNotCallUpdate() {
        when(auth.getCurrentUser()).thenReturn(null);
        repository.incrementLessonProgress();
        verify(userDocument, never()).update(any());
    }

    @Test
    public void incrementLessonProgress_userIdEmpty_doesNotCallUpdate() {
        when(user.getUid()).thenReturn(""); // пустой UID

        repository.incrementLessonProgress();

        // update на userDocument не должен вызываться
        verify(userDocument, never()).update(any());
    }

    @Test
    public void incrementLessonProgress_incrementsCorrectly() {
        repository.incrementLessonProgress();

        verify(userDocument).update(argThat(map ->
                map.containsKey("progress.lessonsCompleted") &&
                        map.get("progress.lessonsCompleted") instanceof FieldValue
        ));
    }

    @Test
    public void incrementLessonProgress_multipleCalls_incrementsEachTime() {
        repository.incrementLessonProgress();
        repository.incrementLessonProgress();
        repository.incrementLessonProgress();

        verify(userDocument, times(3)).update(any(Map.class));
    }

    @Test
    public void incrementLessonProgress_firestoreError_doesNotCrash() {
        Task<Void> failedTask = Tasks.forException(new RuntimeException("Firestore error"));
        when(userDocument.update(any(Map.class))).thenReturn(failedTask);

        try {
            repository.incrementLessonProgress();
        } catch (Exception e) {
            fail("incrementLessonProgress should not throw: " + e.getMessage());
        }

        verify(userDocument).update(any(Map.class));
    }
}
