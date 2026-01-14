package com.example.bookstracker;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FriendsListActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String currentUserId;

    private ArrayList<String> friendsNames = new ArrayList<>();
    private ArrayList<User> friendsObjects = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        db = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Utilizator neautentificat!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ListView listView = findViewById(R.id.lvFriends);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, friendsNames) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);

                text.setTextColor(Color.parseColor("#967BB6"));
                text.setTextSize(18f);
                text.setTypeface(null, Typeface.BOLD);
                text.setPadding(40, 45, 40, 45);

                view.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

                return view;
            }
        };
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < friendsObjects.size()) {
                User selectedFriend = friendsObjects.get(position);
                String friendId = selectedFriend.getUid();
                String friendName = selectedFriend.getUsername();

                if (friendId != null && !friendId.isEmpty()) {
                    Intent intent = new Intent(FriendsListActivity.this, FriendLibraryActivity.class);
                    intent.putExtra("FRIEND_ID", friendId);
                    intent.putExtra("FRIEND_NAME", friendName);
                    startActivity(intent);
                }
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            User friendToDelete = friendsObjects.get(position);

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Șterge prieten")
                    .setMessage("Sigur vrei să îl elimini pe " + friendToDelete.getUsername() + " din lista ta?")
                    .setPositiveButton("Șterge", (d, w) -> removeFriend(friendToDelete.getUid()))
                    .setNegativeButton("Anulează", null)
                    .show();

            return true;
        });

        loadFriends();
    }

    private void removeFriend(String friendUid) {
        db.collection("users").document(currentUserId).update("friends", FieldValue.arrayRemove(friendUid));
        db.collection("users").document(friendUid).update("friends", FieldValue.arrayRemove(currentUserId))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Prieten eliminat", Toast.LENGTH_SHORT).show();
                    loadFriends();
                });
    }

    private void loadFriends() {
        friendsNames.clear();
        friendsObjects.clear();

        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> friendsIds = (List<String>) documentSnapshot.get("friends");

                        if (friendsIds != null && !friendsIds.isEmpty()) {
                            for (String fId : friendsIds) {
                                db.collection("users").document(fId).get()
                                        .addOnSuccessListener(friendDoc -> {
                                            if (friendDoc.exists()) {
                                                User friend = friendDoc.toObject(User.class);
                                                if (friend != null) {
                                                    friend.setUid(friendDoc.getId());
                                                    friendsObjects.add(friend);
                                                    friendsNames.add(friend.getUsername() != null ? friend.getUsername() : "Utilizator");
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("FIRESTORE_ERROR", e.getMessage()));
    }
}