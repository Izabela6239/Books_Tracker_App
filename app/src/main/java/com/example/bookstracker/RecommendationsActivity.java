package com.example.bookstracker;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.view.View;
import android.widget.AdapterView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RecommendationsActivity extends AppCompatActivity {

    private BookAdapter adapter;
    private Spinner spinnerGenre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

        spinnerGenre = findViewById(R.id.spinnerGenre);
        RecyclerView recyclerView = findViewById(R.id.recyclerRecommendations);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookAdapter();
        adapter.setRecommendationMode(true);
        recyclerView.setAdapter(adapter);

        String[] genres = {
                "Ficțiune", "Dezvoltare Personală", "Istorie", "SF & Fantasy",
                "Mistere & Thriller", "Biografii", "Business & Economie",
                "Sănătate & Fitness", "Gătit", "Artă", "Psihologie",
                "Religie", "Știință", "Calculatoare & IT", "Copii", "Poezie"
        };
        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genres);
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenre.setAdapter(genreAdapter);

        spinnerGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String searchGenre;
                switch (genres[position]) {
                    case "Ficțiune": searchGenre = "fiction"; break;
                    case "Dezvoltare Personală": searchGenre = "self-help"; break;
                    case "Istorie": searchGenre = "history"; break;
                    case "SF & Fantasy": searchGenre = "science_fiction"; break;
                    case "Mistere & Thriller": searchGenre = "mystery_detective"; break;
                    case "Biografii": searchGenre = "biography"; break;
                    case "Business & Economie": searchGenre = "business"; break;
                    case "Sănătate & Fitness": searchGenre = "health"; break;
                    case "Gătit": searchGenre = "cooking"; break;
                    case "Artă": searchGenre = "art"; break;
                    case "Psihologie": searchGenre = "psychology"; break;
                    case "Religie": searchGenre = "religion"; break;
                    case "Știință": searchGenre = "science"; break;
                    case "Calculatoare & IT": searchGenre = "computers"; break;
                    case "Copii": searchGenre = "juvenile_fiction"; break;
                    case "Poezie": searchGenre = "poetry"; break;
                    default: searchGenre = "books";
                }

                updateRecommendations(searchGenre);

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateRecommendations(String genre) {
        adapter.setBooks(new ArrayList<>());

        String url = "https://www.googleapis.com/books/v1/volumes?q=subject:" + genre +
                "&orderBy=relevance&maxResults=5&langRestrict=ro&printType=books";

        com.android.volley.toolbox.JsonObjectRequest request = new com.android.volley.toolbox.JsonObjectRequest(
                com.android.volley.Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getInt("totalItems") > 0) {
                            org.json.JSONArray items = response.getJSONArray("items");
                            List<Book> dynamicRecs = new ArrayList<>();

                            for (int i = 0; i < items.length(); i++) {
                                org.json.JSONObject volumeInfo = items.getJSONObject(i).getJSONObject("volumeInfo");

                                String title = volumeInfo.optString("title", "Titlu necunoscut");

                                String author = "Autor necunoscut";
                                if (volumeInfo.has("authors")) {
                                    author = volumeInfo.getJSONArray("authors").getString(0);
                                }

                                String thumbnail = "";
                                if (volumeInfo.has("imageLinks")) {
                                    thumbnail = volumeInfo.getJSONObject("imageLinks")
                                            .optString("thumbnail", "")
                                            .replace("http://", "https://");
                                }

                                String publisher = volumeInfo.optString("publisher", "Editură necunoscută");
                                int pageCount = volumeInfo.optInt("pageCount", 0);
                                float avgRating = (float) volumeInfo.optDouble("averageRating", 0.0);
                                int ratingsCount = volumeInfo.optInt("ratingsCount", 0);

                                String status = "Recomandată";

                                dynamicRecs.add(new Book(
                                        title,
                                        author,
                                        thumbnail,
                                        status,
                                        0,
                                        // streak
                                        pageCount,
                                        publisher,
                                        avgRating,
                                        ratingsCount
                                ));
                            }
                            adapter.setBooks(dynamicRecs);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> android.widget.Toast.makeText(this, "Nu am putut încărca recomandările", android.widget.Toast.LENGTH_SHORT).show()
        );

        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }
}