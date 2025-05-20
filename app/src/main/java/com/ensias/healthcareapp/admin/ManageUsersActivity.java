package com.ensias.healthcareapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ensias.healthcareapp.R;
import com.ensias.healthcareapp.admin.activity.EditUserActivity;
import com.ensias.healthcareapp.admin.adapter.UserAdapter;
import com.ensias.healthcareapp.model.User;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity implements UserAdapter.OnUserClickListener {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);
        setTitle("Пайдаланушыларды басқару");

        recyclerView = findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(userList, this);
        recyclerView.setAdapter(adapter);

        loadUsers();
    }

    private void loadUsers() {
        db.collection("User")
                .whereNotEqualTo("type", "doctor")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        if (user != null) userList.add(user);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Қате жүктеу: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onUserDelete(User user) {
        db.collection("User").document(user.getEmail())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Пайдаланушы удален", Toast.LENGTH_SHORT).show();
                    userList.remove(user);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Қате: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onUserEdit(User user) {
        Intent intent = new Intent(this, EditUserActivity.class);
        intent.putExtra("userId", user.getEmail());
        startActivity(intent);
    }
}
