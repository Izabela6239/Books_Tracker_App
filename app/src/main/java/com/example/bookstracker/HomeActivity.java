package com.example.bookstracker;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    private Uri imageUri;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private TextView tvDailyQuote, tvQuoteAuthor;

    private final ActivityResultLauncher<Intent> scanLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String isbn = result.getData().getStringExtra("ISBN_CODE");

                    fetchTitleAndShowSheet(isbn);
                }
            }
    );

    private void fetchTitleAndShowSheet(String isbn) {
        String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn;

        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                com.android.volley.Request.Method.GET, url, null,
                response -> {
                    try {
                        String title = isbn;
                        if (response.getInt("totalItems") > 0) {
                            title = response.getJSONArray("items")
                                    .getJSONObject(0)
                                    .getJSONObject("volumeInfo")
                                    .getString("title");
                        }
                        showPriceComparisonSheet(isbn, title);
                    } catch (Exception e) {
                        showPriceComparisonSheet(isbn, isbn);
                    }
                },
                error -> showPriceComparisonSheet(isbn, isbn)
        );
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_home);
        tvDailyQuote = findViewById(R.id.tvDailyQuote);
        tvQuoteAuthor = findViewById(R.id.tvQuoteAuthor);
        loadBookQuote();
        drawerLayout = findViewById(R.id.drawer_layout);
        com.google.android.material.navigation.NavigationView navigationView = findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);
        TextView tvNavHeaderTitle = headerView.findViewById(R.id.nav_header_title);
        TextView tvNavHeaderSubtitle = headerView.findViewById(R.id.nav_header_subtitle);

        String uid = currentUser.getUid();
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        tvNavHeaderTitle.setText(username);
                        TextView tvSalut = findViewById(R.id.tvSalutare);
                        tvSalut.setText("Salutare, " + username + "!");
                    }
                });

        setupCardListeners();
        setupNavigationMenu(navigationView);
        RecyclerView rvFeed = findViewById(R.id.rvFeed);
        rvFeed.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<FeedItem> hardcodedFeed = new ArrayList<>();
        hardcodedFeed.add(new FeedItem("Andrei", "Crimă și pedeapsă", "a terminat", 5.0f));
        hardcodedFeed.add(new FeedItem("Elena", "Hobbitul", "a adăugat în listă", 0));
        hardcodedFeed.add(new FeedItem("Marius", "Atomic Habits", "a dat rating", 4.5f));
        hardcodedFeed.add(new FeedItem("Simona", "Duna", "este la pagina 200 din", 0));

        FeedAdapter feedAdapter = new FeedAdapter(hardcodedFeed);
        rvFeed.setAdapter(feedAdapter);
    }

    private void setupNavigationMenu(com.google.android.material.navigation.NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_add_friend) {
                showAddFriendDialog();
            } else if (id == R.id.nav_view_friends) {
                startActivity(new Intent(HomeActivity.this, FriendsListActivity.class));
            } else if (id == R.id.nav_friend_requests) {
                startActivity(new Intent(HomeActivity.this, FriendRequestActivity.class));
            } else if (id == R.id.nav_logout) {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(HomeActivity.this, RegisterActivity.class));
                finish();
            }

            drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START);
            return true;
        });
    }

    private void setupCardListeners() {
        findViewById(R.id.cardMyLibrary).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.cardRecommendations).setOnClickListener(v -> startActivity(new Intent(this, RecommendationsActivity.class)));
        findViewById(R.id.cardPriceFinder).setOnClickListener(v -> showSearchDialog());
    }

    private void showSearchDialog() {
        android.widget.EditText inputTitle = new android.widget.EditText(this);
        inputTitle.setHint("Introdu titlul cărții...");
        inputTitle.setPadding(60, 40, 60, 40);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Caută cel mai bun preț")
                .setMessage("Introdu titlul sau autorul pentru a compara prețurile pe Bookzone, Elefant, Libris și Cărturești.")
                .setView(inputTitle)
                .setPositiveButton("Caută", (dialog, which) -> {
                    String title = inputTitle.getText().toString().trim();
                    if (!title.isEmpty()) {
                        showPriceComparisonSheet(null, title);
                    } else {
                        Toast.makeText(this, "Te rugăm să introduci un titlu", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Anulează", null)
                .show();
    }

    private void scheduleDailyReminder() {
        PeriodicWorkRequest reminderRequest =
                new PeriodicWorkRequest.Builder(ReadingReminderWorker.class, 24, TimeUnit.HOURS)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "daily_reading_reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                reminderRequest
        );
    }

    private String cleanTitleForSearch(String title) {
        if (title == null) return "";

        String normalized = java.text.Normalizer.normalize(title, java.text.Normalizer.Form.NFD);
        String result = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        result = result.replaceAll("[^a-zA-Z0-9 ]", " ");

        return result.trim().replaceAll("\\s+", " ");
    }

    private void showPriceComparisonSheet(String isbn, String bookTitle) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_price_comparison, null);
        bottomSheetDialog.setContentView(sheetView);

        LinearLayout layoutLoading = sheetView.findViewById(R.id.layoutLoading);
        LinearLayout layoutResults = sheetView.findViewById(R.id.layoutResults);
        TextView tvBookTitle = sheetView.findViewById(R.id.tvScannedIsbn);

        MaterialButton btnBookzone = sheetView.findViewById(R.id.btnBookzone);
        MaterialButton btnElefant = sheetView.findViewById(R.id.btnElefant);
        MaterialButton btnLibris = sheetView.findViewById(R.id.btnLibris);
        MaterialButton btnCarturesti = sheetView.findViewById(R.id.btnCarturesti);

        tvBookTitle.setText("Rezultate pentru: " + bookTitle);

        String query = Uri.encode(bookTitle);

        new android.os.Handler().postDelayed(() -> {
            layoutLoading.setVisibility(View.GONE);
            layoutResults.setVisibility(View.VISIBLE);
        }, 1200);

        btnBookzone.setOnClickListener(v -> {
            String url = "https://bookzone.ro/cauta?q=" + Uri.encode(bookTitle);
            openUrl(url);
            bottomSheetDialog.dismiss();
        });

        btnBookzone.setOnClickListener(v -> {
            String url = "https://bookzone.ro/cauta?q=" + Uri.encode(bookTitle);
            openUrl(url);
            bottomSheetDialog.dismiss();
        });

        btnLibris.setOnClickListener(v -> {
            String url = "https://www.libris.ro/cauta?q=" + Uri.encode(bookTitle);
            openUrl(url);
            bottomSheetDialog.dismiss();
        });

        btnElefant.setOnClickListener(v -> {
            String url = "https://www.elefant.ro/list?SearchTerm=" + Uri.encode(bookTitle);
            openUrl(url);
            bottomSheetDialog.dismiss();
        });

        btnCarturesti.setOnClickListener(v -> {
            String url = "https://carturesti.ro/product/search/" + Uri.encode(bookTitle);
            openUrl(url);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }
    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void showAddFriendDialog() {
        android.widget.EditText etUsername = new android.widget.EditText(this);
        etUsername.setHint("Nume utilizator prieten...");
        etUsername.setPadding(60, 40, 60, 40);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Adaugă un prieten")
                .setMessage("Introdu username-ul exact al prietenului pentru a-l adăuga.")
                .setView(etUsername)
                .setPositiveButton("Adaugă", (dialog, which) -> {
                    String friendUsername = etUsername.getText().toString().trim();
                    if (!friendUsername.isEmpty()) {
                        addFriendLogic(friendUsername);
                    }
                })
                .setNegativeButton("Anulează", null)
                .show();
    }

    private void addFriendLogic(String targetUsername) {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .whereEqualTo("username", targetUsername)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        com.google.firebase.firestore.DocumentSnapshot friendDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String friendId = friendDoc.getString("uid");

                        if (friendId.equals(currentUserId)) {
                            Toast.makeText(this, "Nu te poți adăuga pe tine însuți!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        db.collection("users").document(currentUserId)
                                .update("friends", com.google.firebase.firestore.FieldValue.arrayUnion(friendId))
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Prieten adăugat cu succes!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Eroare la adăugare", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Utilizatorul '" + targetUsername + "' nu există!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // 1. Launcher pentru a deschide galeria
   /* private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    uploadImageToFirebase();
                }
            }
    );*/

    // 2. Metoda care pornește selecția
   /* private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }*/

    // 3. Upload în Storage și salvare URL în Firestore
   /* private void uploadImageToFirebase() {
        if (imageUri == null) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference fileRef = storageRef.child("profile_images/" + userId + ".jpg");

        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();

                // Salvăm URL-ul în Firestore
                FirebaseFirestore.getInstance().collection("users").document(userId)
                        .update("profileImageUrl", downloadUrl)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Poză actualizată!", Toast.LENGTH_SHORT).show();
                            // Actualizează UI-ul cu Glide
                            loadProfileImage(downloadUrl);
                        });
            });
        }).addOnFailureListener(e -> Toast.makeText(this, "Eroare upload", Toast.LENGTH_SHORT).show());
    }*/

    // 4. Afișarea pozei cu Glide
//    private void loadProfileImage(String url) {
//        ImageView ivProfile = findViewById(R.id.ivProfilePicture); // ID-ul tău din XML
//        Glide.with(this)
//                .load(url)
//                .circleCrop() // Face poza rotundă automat!
//                .placeholder(R.drawable.default_avatar) // O imagine de rezervă
//                .into(ivProfile);
//    }

   /* private void listenForFeedUpdates() {
        cloudDb.collection("activities")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10) // Afișăm ultimele 10 noutăți
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    List<FeedItem> realFeed = new ArrayList<>();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        // Convertim documentul în obiectul nostru FeedItem
                        FeedItem item = doc.toObject(FeedItem.class);
                        realFeed.add(item);
                    }

                    // Actualizăm adaptorul
                    feedAdapter.setItems(realFeed);
                    feedAdapter.notifyDataSetChanged();
                });
    }*/

    private void loadBookQuote() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.quotable.io/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        QuoteApiService service = retrofit.create(QuoteApiService.class);

        service.getRandomQuote().enqueue(new Callback<List<LiteraryQuote>>() {
            @Override
            public void onResponse(Call<List<LiteraryQuote>> call, Response<List<LiteraryQuote>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    LiteraryQuote q = response.body().get(0);

                    tvDailyQuote.setText("„" + q.getQuote() + "”");
                    tvQuoteAuthor.setText("- " + q.getAuthor());
                } else {
                    tvDailyQuote.setText("Cărțile sunt oglinzi ale sufletului.");
                }
            }

            @Override
            public void onFailure(Call<List<LiteraryQuote>> call, Throwable t) {
                android.util.Log.e("API_ERROR", t.getMessage());
                tvDailyQuote.setText("Lectura este o formă de fericire.");
                tvQuoteAuthor.setText("- Jorge Luis Borges");
            }
        });
    }

}