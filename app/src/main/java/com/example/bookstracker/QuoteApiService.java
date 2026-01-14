package com.example.bookstracker;

import retrofit2.Call;
import retrofit2.http.GET;
import java.util.List;

public interface QuoteApiService {
    @GET("quotes/random?tags=famous-quotes")
    Call<List<LiteraryQuote>> getRandomQuote();
}
