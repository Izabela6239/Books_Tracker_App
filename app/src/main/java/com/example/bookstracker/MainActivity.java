package com.example.bookstracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BookAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new BookAdapter();
        recyclerView.setAdapter(adapter);

        adapter = new BookAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(book -> {
            Intent intent = new Intent(MainActivity.this, BookDetailsActivity.class);

            intent.putExtra("BOOK_ID", book.getId());
            intent.putExtra("BOOK_TITLE", book.getTitle());
            intent.putExtra("BOOK_AUTHOR", book.getAuthor());
            intent.putExtra("BOOK_STATUS", book.getStatus());
            intent.putExtra("BOOK_IMAGE", book.getImageUrl());
            intent.putExtra("BOOK_PAGES_TOTAL", book.getPageCount());
            intent.putExtra("BOOK_PAGES_READ", book.getPagesRead());

            startActivity(intent);
        });



        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                Book bookToDelete = adapter.getBookAt(position);
                String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users").document(userId)
                        .collection("library").document(bookToDelete.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(MainActivity.this, "Carte ștearsă din Cloud", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(MainActivity.this, "Eroare la ștergere: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            adapter.notifyItemChanged(position);
                        });
            }
        };

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        FloatingActionButton buttonAdd = findViewById(R.id.buttonAddBook);
        buttonAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddBookActivity.class);
            startActivity(intent);
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

        db.collection("users").document(userId).collection("library")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Eroare la sincronizare: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        List<com.example.bookstracker.Book> booksFromFirebase = value.toObjects(com.example.bookstracker.Book.class);

                        for (int i = 0; i < value.getDocuments().size(); i++) {
                            String documentId = value.getDocuments().get(i).getId();
                            booksFromFirebase.get(i).setId(documentId);
                        }
                        if (adapter != null) {
                            adapter.setBooks(booksFromFirebase);
                        }
                    }
                });
    }



}