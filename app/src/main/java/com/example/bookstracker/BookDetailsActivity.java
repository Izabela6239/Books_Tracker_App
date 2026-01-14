package com.example.bookstracker;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookDetailsActivity extends AppCompatActivity {

    private ImageView imgBook;
    private TextView tvTitle, tvAuthor, tvStatus;
    private EditText editQuoteText;
    private ImageButton btnMic;
    private Button btnSaveQuote, btnUpdateProgress;

    private RecyclerView rvQuotes;
    private QuoteAdapter quoteAdapter;

    private BookDatabase localDatabase;
    private FirebaseFirestore cloudDb;

    private String currentBookId;
    private String currentUserId;
    private Book currentBook;

    private ActivityResultLauncher<Intent> speechRecognizerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        localDatabase = BookDatabase.getInstance(this);
        cloudDb = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        tvTitle = findViewById(R.id.textTitle);
        tvAuthor = findViewById(R.id.textAuthor);
        tvStatus = findViewById(R.id.textStatus);
        editQuoteText = findViewById(R.id.editQuoteText);
        btnMic = findViewById(R.id.btnMic);
        btnSaveQuote = findViewById(R.id.btnSaveQuote);
        btnUpdateProgress = findViewById(R.id.btnUpdateProgress);

        rvQuotes = findViewById(R.id.rvQuotes);
        rvQuotes.setLayoutManager(new LinearLayoutManager(this));
        quoteAdapter = new QuoteAdapter();
        rvQuotes.setAdapter(quoteAdapter);

        setupSwipeToDeleteQuote();

        currentBookId = getIntent().getStringExtra("BOOK_ID");

        if (currentBookId != null) {
            loadBookFromFirebase();
            loadQuotesFromRoom();
        }

        setupSpeechRecognizer();

        btnMic.setOnClickListener(v -> startVoiceRecognition());
        btnSaveQuote.setOnClickListener(v -> saveQuoteToRoom(editQuoteText.getText().toString()));
        btnUpdateProgress.setOnClickListener(v -> {
            if (currentBook != null) showStatusDialog(currentBook);
        });
    }

    private void loadBookFromFirebase() {
        cloudDb.collection("users").document(currentUserId)
                .collection("library").document(currentBookId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    currentBook = documentSnapshot.toObject(Book.class);
                    if (currentBook != null) {
                        tvTitle.setText(currentBook.getTitle());
                        tvAuthor.setText(currentBook.getAuthor());
                        tvStatus.setText(currentBook.getStatus());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Eroare la încărcare cloud", Toast.LENGTH_SHORT).show());
    }

    private void showStatusDialog(Book book) {
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Pagina curentă (Total: " + book.getPageCount() + ")");

        new AlertDialog.Builder(this)
                .setTitle("Actualizează progresul")
                .setView(input)
                .setPositiveButton("Salvează", (dialog, which) -> {
                    String val = input.getText().toString();
                    if (!val.isEmpty()) {
                        int newPage = Integer.parseInt(val);
                        if (newPage <= book.getPageCount()) {
                            book.setPagesRead(newPage);

                            if (newPage == book.getPageCount()) {
                                showReviewDialog();
                            } else {
                                book.setStatus("În curs");
                                updateBookInFirebase(book);
                            }
                        } else {
                            Toast.makeText(this, "Pagina depășește totalul!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Anulează", null)
                .show();
    }

    private void showReviewDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_review, null);
        RatingBar rb = dialogView.findViewById(R.id.dialogRatingBar);
        TextInputEditText et = dialogView.findViewById(R.id.dialogEditReview);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Felicitări! Ai terminat cartea!")
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton("Salvează", (dialog, which) -> {
                    float rating = rb.getRating();
                    String reviewText = et.getText().toString().trim();
                    saveFinalStatusWithReview(rating, reviewText);
                })
                .setNegativeButton("Mai târziu", (dialog, which) -> {
                    updateBookStatusOnly("Finalizată");
                })
                .show();
    }

    private void saveFinalStatusWithReview(float rating, String review) {
        if (currentBookId == null) return;

        cloudDb.collection("users").document(currentUserId)
                .collection("library").document(currentBookId)
                .update(
                        "status", "Finalizată",
                        "pagesRead", currentBook.getPageCount(), // Ne asigurăm că paginile sunt la max
                        "rating", rating,
                        "review", review
                )
                .addOnSuccessListener(aVoid -> {
                    tvStatus.setText("Finalizată");
                    Toast.makeText(this, "Review salvat cu succes!", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateBookStatusOnly(String status) {
        cloudDb.collection("users").document(currentUserId)
                .collection("library").document(currentBookId)
                .update("status", status, "pagesRead", currentBook.getPageCount())
                .addOnSuccessListener(aVoid -> {
                    tvStatus.setText(status);
                    Toast.makeText(this, "Status actualizat!", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateBookInFirebase(Book book) {
        cloudDb.collection("users").document(currentUserId)
                .collection("library").document(currentBookId)
                .set(book)
                .addOnSuccessListener(aVoid -> {
                    tvStatus.setText(book.getStatus());
                    Toast.makeText(this, "Progres salvat!", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadQuotesFromRoom() {
        new Thread(() -> {
            List<Quote> quotes = localDatabase.quoteDao().getQuotesForBook(currentBookId);
            runOnUiThread(() -> quoteAdapter.setQuotes(quotes));
        }).start();
    }

    private void saveQuoteToRoom(String text) {
        if (text.isEmpty()) return;
        new Thread(() -> {
            Quote newQuote = new Quote(currentBookId, text, System.currentTimeMillis());
            localDatabase.quoteDao().insert(newQuote);
            runOnUiThread(() -> {
                Toast.makeText(this, "Notă salvată!", Toast.LENGTH_SHORT).show();
                editQuoteText.setText("");
                loadQuotesFromRoom();
            });
        }).start();
    }

    private void setupSwipeToDeleteQuote() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder t) { return false; }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Quote quoteToDelete = quoteAdapter.getQuoteAt(position);
                new Thread(() -> {
                    localDatabase.quoteDao().delete(quoteToDelete);
                    runOnUiThread(() -> loadQuotesFromRoom());
                }).start();
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(rvQuotes);
    }

    private void setupSpeechRecognizer() {
        speechRecognizerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (matches != null && !matches.isEmpty()) {
                            editQuoteText.setText(matches.get(0));
                        }
                    }
                }
        );
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ro-RO");
        try { speechRecognizerLauncher.launch(intent); }
        catch (Exception e) { Toast.makeText(this, "Eroare microfon", Toast.LENGTH_SHORT).show(); }
    }

    private void postToFeed(String userName, String bookTitle, String action, float rating) {
        Map<String, Object> activity = new HashMap<>();
        activity.put("userName", userName);
        activity.put("bookTitle", bookTitle);
        activity.put("actionText", action);
        activity.put("rating", rating);
        activity.put("timestamp", FieldValue.serverTimestamp());
        activity.put("userId", currentUserId); // Ca să știm al cui e postarea

        cloudDb.collection("activities")
                .add(activity)
                .addOnSuccessListener(documentReference -> Log.d("FEED", "Activitate postată!"));
    }
}