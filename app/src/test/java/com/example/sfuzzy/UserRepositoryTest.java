package com.example.sfuzzy;

import static org.mockito.Mockito.*;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class UserRepositoryTest {

    @Test
    public void loadOrCreateUser_callsOnLoaded() {
        // Мокаем Firestore и его цепочку вызовов
        FirebaseFirestore mockDb = mock(FirebaseFirestore.class);
        CollectionReference mockUsers = mock(CollectionReference.class);
        DocumentReference mockDocRef = mock(DocumentReference.class);
        DocumentSnapshot mockSnapshot = mock(DocumentSnapshot.class);
        Task<DocumentSnapshot> mockTask = mock(Task.class);

        // Настраиваем поведение
        when(mockDb.collection("users")).thenReturn(mockUsers);
        when(mockUsers.document("123")).thenReturn(mockDocRef);
        when(mockDocRef.get()).thenReturn(mockTask);

        // Мокаем добавление слушателей
        doAnswer(invocation -> {
            OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
            listener.onSuccess(mockSnapshot); // вызываем сразу onSuccess
            return mockTask;
        }).when(mockTask).addOnSuccessListener(any());

        doAnswer(invocation -> {
            OnFailureListener listener = invocation.getArgument(0);
            // ничего не делаем, не вызываем onFailure
            return mockTask;
        }).when(mockTask).addOnFailureListener(any());

        // Мокаем snapshot.exists()
        when(mockSnapshot.exists()).thenReturn(true);

        // Создаем репозиторий с мокнутой БД
        UserRepository repo = new UserRepository(mockDb);

        // Мокаем callback
        UserRepository.UserCallback callback = mock(UserRepository.UserCallback.class);

        // Вызов метода
        repo.loadOrCreateUser("123", "test@example.com", callback);

        // Проверяем, что callback.onLoaded вызвался
        verify(callback).onLoaded(mockSnapshot);
        verify(callback, never()).onError(any());
    }

    @Test
    public void loadOrCreateUser_failure_callsOnError() {
        // Мокаем Firestore и цепочку коллекций/документов
        FirebaseFirestore mockDb = mock(FirebaseFirestore.class);
        CollectionReference mockCollection = mock(CollectionReference.class);
        DocumentReference mockDocument = mock(DocumentReference.class);
        Task<DocumentSnapshot> mockTask = mock(Task.class);

        // Настраиваем поведение моков
        when(mockDb.collection("users")).thenReturn(mockCollection);
        when(mockCollection.document("uid123")).thenReturn(mockDocument);
        when(mockDocument.get()).thenReturn(mockTask);

        // Настраиваем цепочку addOnSuccessListener / addOnFailureListener
        when(mockTask.addOnSuccessListener(any())).thenReturn(mockTask);
        when(mockTask.addOnFailureListener(any())).thenReturn(mockTask);

        // Эмулируем ошибку: вызываем onFailure при добавлении слушателя
        doAnswer(invocation -> {
            OnFailureListener listener = invocation.getArgument(0);
            listener.onFailure(new Exception("Ошибка загрузки"));
            return null;
        }).when(mockTask).addOnFailureListener(any());

        // Мокаем колбэк репозитория
        UserRepository.UserCallback callback = mock(UserRepository.UserCallback.class);

        // Создаем репозиторий с мокнутым Firestore
        UserRepository repo = new UserRepository(mockDb);

        // Вызываем метод, который тестируем
        repo.loadOrCreateUser("uid123", "test@mail.com", callback);

        // Проверяем, что сработал onError
        verify(callback).onError(any(Exception.class));
    }

    @Test
    public void loadOrCreateUser_newUser_callsSetAndOnLoaded(){
        FirebaseFirestore mockDb = mock(FirebaseFirestore.class);
        CollectionReference mockCollection = mock(CollectionReference.class);
        DocumentReference mockDocument = mock(DocumentReference.class);
        Task<DocumentSnapshot> mockTask = mock(Task.class);
        DocumentSnapshot mockSnapshot = mock(DocumentSnapshot.class);

        when(mockDb.collection("users")).thenReturn(mockCollection);
        when(mockCollection.document("uid123")).thenReturn(mockDocument);
        when(mockDocument.get()).thenReturn(mockTask);

        doAnswer(invocation -> {
            OnSuccessListener<DocumentSnapshot> listener = invocation.getArgument(0);
            when(mockSnapshot.exists()).thenReturn(false);
            listener.onSuccess(mockSnapshot);
            return mockTask;
        }).when(mockTask).addOnSuccessListener(any());

        Task<Void> mockSetTask = mock(Task.class);
        when(mockDocument.set(anyMap(), any())).thenReturn(mockSetTask);

        UserRepository.UserCallback callback = mock(UserRepository.UserCallback.class);
        UserRepository repo = new UserRepository(mockDb);

        repo.loadOrCreateUser("uid123", "test@mail.com", callback);

        verify(mockDocument).set(argThat(argument -> {
            Map<String, Object> map = (Map<String, Object>) argument;
            return map.get("email").equals("test@mail.com") &&
                    map.containsKey("name") &&
                    map.containsKey("progress");
        }), eq(SetOptions.merge()));

        verify(callback).onLoaded(mockSnapshot);
    }

    @Test
    public void subscribeToProgress_documentExists_callsOnProgressWithValue() {
        // Мокаем Firestore и необходимые объекты
        FirebaseFirestore mockDb = mock(FirebaseFirestore.class);
        DocumentReference mockDocument = mock(DocumentReference.class);

        when(mockDb.collection("users")).thenReturn(mock(CollectionReference.class));
        when(mockDb.collection("users").document("uid123")).thenReturn(mockDocument);

        // Колбэк прогресса
        UserRepository.ProgressCallback callback = mock(UserRepository.ProgressCallback.class);

        // Эмулируем snapshotListener
        doAnswer(invocation -> {
            EventListener<DocumentSnapshot> listener = invocation.getArgument(0);

            // Мокаем snapshot
            DocumentSnapshot mockSnapshot = mock(DocumentSnapshot.class);
            when(mockSnapshot.exists()).thenReturn(true);

            Map<String, Object> progressMap = new HashMap<>();
            progressMap.put("lessonsCompleted", 5);

            when(mockSnapshot.get("progress")).thenReturn(progressMap);

            // Вызываем listener
            listener.onEvent(mockSnapshot, null);
            return mock(ListenerRegistration.class);
        }).when(mockDocument).addSnapshotListener(any());

        UserRepository repo = new UserRepository(mockDb);

        repo.subscribeToProgress("uid123", callback);

        // Проверяем, что колбэк получил правильное число
        verify(callback).onProgress(5);
    }

    @Test
    public void subscribeToProgress_emptyProgress_callsOnProgressWithZero() {
        FirebaseFirestore mockDb = mock(FirebaseFirestore.class);
        DocumentReference mockDocument = mock(DocumentReference.class);

        when(mockDb.collection("users")).thenReturn(mock(CollectionReference.class));
        when(mockDb.collection("users").document("uid123")).thenReturn(mockDocument);

        UserRepository.ProgressCallback callback = mock(UserRepository.ProgressCallback.class);

        doAnswer(invocation -> {
            EventListener<DocumentSnapshot> listener = invocation.getArgument(0);

            DocumentSnapshot mockSnapshot = mock(DocumentSnapshot.class);
            when(mockSnapshot.exists()).thenReturn(true);
            when(mockSnapshot.get("progress")).thenReturn(null);

            listener.onEvent(mockSnapshot, null);
            return mock(ListenerRegistration.class);
        }).when(mockDocument).addSnapshotListener(any());

        UserRepository repo = new UserRepository(mockDb);

        repo.subscribeToProgress("uid123", callback);

        verify(callback).onProgress(0);
    }

    @Test
    public void subscribeToProgress_documentDoesNotExist_doesNotCallOnProgress() {
        FirebaseFirestore mockDb = mock(FirebaseFirestore.class);
        DocumentReference mockDocument = mock(DocumentReference.class);
        ListenerRegistration mockRegistration = mock(ListenerRegistration.class);

        when(mockDb.collection("users")).thenReturn(mock(CollectionReference.class));
        when(mockDb.collection("users").document("uid123")).thenReturn(mockDocument);

        UserRepository.ProgressCallback callback = mock(UserRepository.ProgressCallback.class);

        doAnswer(invocation -> {
            EventListener<DocumentSnapshot> listener = invocation.getArgument(0);

            // Создаем snapshot, который не существует
            DocumentSnapshot mockSnapshot = mock(DocumentSnapshot.class);
            when(mockSnapshot.exists()).thenReturn(false);

            listener.onEvent(mockSnapshot, null);

            return mockRegistration;
        }).when(mockDocument).addSnapshotListener(any());


        UserRepository repo = new UserRepository(mockDb);

        repo.subscribeToProgress("uid123", callback);

        verify(callback, never()).onProgress(anyLong());
    }

    @Test
    public void updateUserName_success_callsCallbackTrue() {
        // Мокаем Firestore и цепочку вызовов
        FirebaseFirestore mockDb = mock(FirebaseFirestore.class);
        DocumentReference mockDocRef = mock(DocumentReference.class);
        Task<Void> mockTask = mock(Task.class);

        // Настраиваем поведение моков
        when(mockDb.collection("users")).thenReturn(mock(CollectionReference.class));
        when(mockDb.collection("users").document("uid123")).thenReturn(mockDocRef);
        when(mockDocRef.update(anyMap())).thenReturn(mockTask);

        // Эмулируем успешное выполнение update
        doAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return mockTask;
        }).when(mockTask).addOnSuccessListener(any());

        // Создаем колбэк
        UserRepository.SimpleCallback callback = mock(UserRepository.SimpleCallback.class);

        // Создаем репозиторий и вызываем метод
        UserRepository repo = new UserRepository(mockDb);
        repo.updateUserName("uid123", "NewName", callback);

        // Проверяем, что колбэк вызвался с true
        verify(callback).onResult(true);
    }

    @Test
    public void updateUserName_emptyName_callsCallbackFalse() {
        FirebaseFirestore mockDb = mock(FirebaseFirestore.class);
        UserRepository.SimpleCallback callback = mock(UserRepository.SimpleCallback.class);

        UserRepository repo = new UserRepository(mockDb);

        // Пустое имя
        repo.updateUserName("uid123", "   ", callback);

        // Проверяем, что callback с false вызвался
        verify(callback).onResult(false);

        // Firestore update не должен вызываться
        verifyNoInteractions(mockDb);
    }
    @Test
    public void updateUserName_firestoreFails_callsCallbackFalse() {
        FirebaseFirestore mockDb = mock(FirebaseFirestore.class);
        CollectionReference mockCollection = mock(CollectionReference.class);
        DocumentReference mockDoc = mock(DocumentReference.class);
        Task<Void> mockTask = mock(Task.class);

        when(mockDb.collection("users")).thenReturn(mockCollection);
        when(mockCollection.document("uid123")).thenReturn(mockDoc);
        when(mockDoc.update(anyMap())).thenReturn(mockTask);

        // Мокаем addOnSuccessListener чтобы возвращал Task (this)
        when(mockTask.addOnSuccessListener(any())).thenReturn(mockTask);
        // Мокаем addOnFailureListener, чтобы вызывать listener
        doAnswer(invocation -> {
            OnFailureListener listener = invocation.getArgument(0);
            listener.onFailure(new Exception("Firestore error"));
            return mockTask;
        }).when(mockTask).addOnFailureListener(any());

        UserRepository.SimpleCallback callback = mock(UserRepository.SimpleCallback.class);
        UserRepository repo = new UserRepository(mockDb);

        repo.updateUserName("uid123", "NewName", callback);

        verify(callback).onResult(false);
    }



}
