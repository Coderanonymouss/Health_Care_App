package com.ensias.healthcareapp.fireStoreApi;

import com.ensias.healthcareapp.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserHelper {
    static FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static CollectionReference UsersRef = db.collection("User");

    // Добавление пользователя
    public static void addUser(String uid, String name, String type){
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        User user = new User(uid, name, email, type);
        UsersRef.document(uid).set(user);
    }


}
