package com.example.bookstracker;
// Asigură-te că pachetul coincide cu proiectul tău

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etEmail, etPassword;
    private MaterialButton btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateForm(username, email, password)) {
                checkUsernameAndRegister(username, email, password);
            }
        });
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);
        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private boolean validateForm(String username, String email, String password) {
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Introdu un nume de utilizator");
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Introdu email-ul");
            return false;
        }
        if (password.length() < 6) {
            etPassword.setError("Parola trebuie să aibă minim 6 caractere");
            return false;
        }
        return true;
    }

    private void checkUsernameAndRegister(String username, String email, String password) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().isEmpty()) {
                        performFirebaseRegistration(username, email, password);
                    } else {
                        Toast.makeText(this, "Acest username este deja utilizat!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void performFirebaseRegistration(String username, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        User newUser = new User(uid, username, email);

                        db.collection("users").document(uid).set(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(RegisterActivity.this, "Cont creat!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                    finish();
                                });
                    } else {
                        Toast.makeText(this, "Eroare la înregistrare: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
