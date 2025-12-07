package com.example.sfuzzy;

import static org.hamcrest.CoreMatchers.any;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

public class ProgressTest {
    @Mock
    FirebaseFirestore firestore;
    @Mock
    FirebaseAuth auth;
    @Mock
    FirebaseUser user;
    @Mock
    CollectionReference userCollection;
    @Mock
    DocumentReference userDocument;
    ProgressRepository repository;
    @Before
    public void setup(){
        MockitoAnnotations.openMocks(this);
        when(auth.getCurrentUser()).thenReturn(user);
        when(user.getUid()).thenReturn("test_uid");

        when(firestore.collection("users")).thenReturn(userCollection);
        when(userCollection.document("test_uid")).thenReturn(userDocument);

        repository = new ProgressRepository(firestore, auth);
    }

    @Test
    public void incrementLessonProgress_callsUpdate() {

        repository.incrementLessonProgress();

        // Проверяем, что update вызвался один раз
        verify(userDocument, times(1)).update(ArgumentMatchers.any(Map.class));
    }

    @Test
    public void incrementLessonProgress_userNull_doesNotCallUpdate() {

        when(auth.getCurrentUser()).thenReturn(null);

        repository.incrementLessonProgress();

        // update не должен вызываться
        verify(userDocument, never()).update(ArgumentMatchers.any(Map.class));
    }
}
