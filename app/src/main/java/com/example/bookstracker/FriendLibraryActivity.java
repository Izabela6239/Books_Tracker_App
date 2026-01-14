package com.example.bookstracker;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FriendLibraryActivity extends AppCompatActivity {

    private String friendId;
    private FriendBookAdapter adapterFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_library);

        friendId = getIntent().getStringExtra("FRIEND_ID");
        String friendName = getIntent().getStringExtra("FRIEND_NAME");

        if (friendId == null || friendId.isEmpty()) {
            Toast.makeText(this, "Eroare: ID prieten invalid!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvHeader = findViewById(R.id.tvFriendLibraryTitle);
        if (tvHeader != null) {
            tvHeader.setText("Biblioteca lui " + (friendName != null ? friendName : "prieten"));
        }

        RecyclerView recyclerView = findViewById(R.id.rvFriendBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapterFriend = new FriendBookAdapter();
        recyclerView.setAdapter(adapterFriend);

        loadFriendBooks();
    }

    private void loadFriendBooks() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(friendId).collection("library")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Book> books = queryDocumentSnapshots.toObjects(Book.class);

                    if (books.isEmpty()) {
                        Toast.makeText(this, "Acest utilizator nu are nicio carte.", Toast.LENGTH_LONG).show();
                    } else {
                        if (adapterFriend != null) {
                            adapterFriend.setBooks(books);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE_ERROR", "Eroare: " + e.getMessage());
                    Toast.makeText(this, "Eroare la conectare: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}