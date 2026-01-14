package com.example.bookstracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.app.Activity;

import org.json.JSONArray;
import org.json.JSONObject;

public class AddBookActivity extends AppCompatActivity {

    private EditText editTitle, editAuthor, editImageUrl, editPages, editPublisher, editRating, editRatingsCount;
    private float currentRating = 0;
    private int currentRatingsCount = 0;

    private final ActivityResultLauncher<Intent> scanLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            String isbn = result.getData().getStringExtra("ISBN_CODE");
                            String mode = result.getData().getStringExtra("SCAN_MODE");

                            if (isbn != null && "ADD_BOOK".equals(mode)) {
                                String cleanIsbn = isbn.replaceAll("[^0-9]", "");
                                fetchBookDetails(cleanIsbn);
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        editTitle = findViewById(R.id.editTextTitle);
        editAuthor = findViewById(R.id.editTextAuthor);
        editPages = findViewById(R.id.editTextPages);
        editPublisher = findViewById(R.id.editTextPublisher);
        editRating = findViewById(R.id.editTextRating);
        editRatingsCount = findViewById(R.id.editTextRatingsCount);

        Button btnSave = findViewById(R.id.buttonAddBook);
        Button btnScan = findViewById(R.id.buttonScanISBN);
        Spinner spinnerStatus = findViewById(R.id.spinnerStatus);

        String[] statusuri = {"Vreau să citesc", "În curs", "Finalizată"};
        android.widget.ArrayAdapter<String> adapterStatus = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, statusuri);
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapterStatus);

        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String author = editAuthor.getText().toString().trim();
            String publisher = editPublisher.getText().toString().trim();
            String selectedStatus = spinnerStatus.getSelectedItem().toString();

            if (title.isEmpty()) {
                Toast.makeText(this, "Titlul este obligatoriu", Toast.LENGTH_SHORT).show();
                return;
            }

            int pages = getIntFromEditText(editPages);
            float rating = getFloatFromEditText(editRating);
            int ratingsCount = getIntFromEditText(editRatingsCount);

            Book newBook = new Book(title, author, selectedStatus, 0, pages, publisher, rating, ratingsCount);

            String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

            db.collection("users")
                    .document(userId)
                    .collection("library")
                    .add(newBook)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Carte salvată în Cloud!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Eroare la salvarea în Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        btnScan.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScanActivity.class);
            intent.putExtra("SCAN_MODE", "ADD_BOOK");
            scanLauncher.launch(intent);
        });
    }

    private int getIntFromEditText(EditText et) {
        try {
            String s = et.getText().toString();
            return s.isEmpty() ? 0 : Integer.parseInt(s);
        } catch (Exception e) { return 0; }
    }

    private float getFloatFromEditText(EditText et) {
        try {
            String s = et.getText().toString();
            return s.isEmpty() ? 0.0f : Float.parseFloat(s);
        } catch (Exception e) { return 0.0f; }
    }

    private void parseGoogleData(JSONObject response) throws Exception {
        JSONArray items = response.getJSONArray("items");
        JSONObject volumeInfo = items.getJSONObject(0).getJSONObject("volumeInfo");

        editTitle.setText(volumeInfo.optString("title", "Titlu necunoscut"));
        editPublisher.setText(volumeInfo.optString("publisher", "Editură necunoscută"));
        editPages.setText(String.valueOf(volumeInfo.optInt("pageCount", 0)));
        editRating.setText(String.valueOf(volumeInfo.optDouble("averageRating", 0.0)));
        editRatingsCount.setText(String.valueOf(volumeInfo.optInt("ratingsCount", 0)));

        if (volumeInfo.has("authors")) {
            editAuthor.setText(volumeInfo.getJSONArray("authors").getString(0));
        } else {
            editAuthor.setText("Autor necunoscut");
        }
    }

    private void fetchFromOpenLibrary(String isbn) {
        String url = "https://openlibrary.org/api/books?bibkeys=ISBN:" + isbn + "&format=json&jscmd=data";

        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                com.android.volley.Request.Method.GET, url, null,
                response -> {
                    try {
                        String key = "ISBN:" + isbn;
                        if (response.has(key)) {
                            JSONObject bookData = response.getJSONObject(key);
                            editTitle.setText(bookData.optString("title", ""));
                            if (bookData.has("authors")) {
                                editAuthor.setText(bookData.getJSONArray("authors").getJSONObject(0).optString("name"));
                            }
                            editPublisher.setText(bookData.optString("publisher", "Necunoscut"));
                            editPages.setText(String.valueOf(bookData.optInt("number_of_pages", 0)));
                            Toast.makeText(this, "Date găsite pe Open Library!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                },
                error -> Toast.makeText(this, "Eroare rețea!", Toast.LENGTH_SHORT).show()
        );
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }

    private void fetchBookDetails(String isbn) {
        String googleUrl = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn;
        com.android.volley.toolbox.JsonObjectRequest googleRequest = new com.android.volley.toolbox.JsonObjectRequest(
                com.android.volley.Request.Method.GET, googleUrl, null,
                response -> {
                    try {
                        if (response.getInt("totalItems") > 0) {
                            parseGoogleData(response);
                            Toast.makeText(this, "Date găsite pe Google Books", Toast.LENGTH_SHORT).show();
                        } else {
                            fetchFromOpenLibrary(isbn);
                        }
                    } catch (Exception e) {
                        fetchFromOpenLibrary(isbn);
                    }
                },
                error -> fetchFromOpenLibrary(isbn)
        );
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(googleRequest);
    }
}