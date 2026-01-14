package com.example.bookstracker;

import android.os.Bundle;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FriendRequestActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private ArrayList<FriendRequest> requestList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ArrayList<String> displayList = new ArrayList<>();
    private TextView tvNoRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        tvNoRequests = findViewById(R.id.tvNoRequests);
        ListView listView = findViewById(R.id.lvRequests);

        // Buton înapoi
        findViewById(R.id.btnBackRequests).setOnClickListener(v -> finish());

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, displayList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextColor(android.graphics.Color.WHITE);
                text.setPadding(32, 32, 32, 32); // Adăugăm spațiu pentru un aspect aerisit
                text.setTextSize(16);

                return view;
            }
        };
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            showActionDialog(requestList.get(position));
        });

        loadRequests();
    }

    private void loadRequests() {
        db.collection("friend_requests")
                .whereEqualTo("to", myUid)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    requestList.clear();
                    displayList.clear();

                    if (value != null && !value.isEmpty()) {
                        tvNoRequests.setVisibility(View.GONE);
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            FriendRequest req = new FriendRequest(
                                    doc.getId(),
                                    doc.getString("from"),
                                    doc.getString("fromUsername")
                            );
                            requestList.add(req);
                            displayList.add("Cerere de prietenie: " + req.getFromUsername());
                        }
                    } else {
                        tvNoRequests.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void showActionDialog(FriendRequest req) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmare Prietenie")
                .setMessage("Dorești să accepți cererea trimisă de " + req.getFromUsername() + "?")
                .setCancelable(true)
                .setPositiveButton("Acceptă", (d, w) -> acceptRequest(req))
                .setNegativeButton("Refuză", (d, w) -> deleteRequest(req.getRequestId()))
                .setNeutralButton("Mai târziu", null)
                .show();
    }

    private void acceptRequest(FriendRequest req) {

        db.collection("users").document(myUid).update("friends", FieldValue.arrayUnion(req.getFromUid()));
        db.collection("users").document(req.getFromUid()).update("friends", FieldValue.arrayUnion(myUid))
                .addOnSuccessListener(aVoid -> {
                    deleteRequest(req.getRequestId());
                    Toast.makeText(this, "Acum sunteți prieteni!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Eroare: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteRequest(String id) {
        db.collection("friend_requests").document(id).delete();
    }
}