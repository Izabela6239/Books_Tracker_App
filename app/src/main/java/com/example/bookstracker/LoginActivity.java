package com.example.bookstracker;


import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmailLogin);
        etPassword = findViewById(R.id.etPasswordLogin);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        tvForgotPassword.setOnClickListener(v -> {
            EditText resetMail = new EditText(v.getContext());
            resetMail.setHint("Introdu adresa de email");

            AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
            passwordResetDialog.setTitle("Resetare Parolă?");
            passwordResetDialog.setMessage("Introdu email-ul pentru a primi link-ul de resetare.");
            passwordResetDialog.setView(resetMail);

            passwordResetDialog.setPositiveButton("Trimite", (dialog, which) -> {
                String mail = resetMail.getText().toString().trim();
                if (mail.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Te rugăm să introduci email-ul!", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.sendPasswordResetEmail(mail)
                        .addOnSuccessListener(aVoid -> Toast.makeText(LoginActivity.this, "Link-ul de resetare a fost trimis pe email!", Toast.LENGTH_LONG).show())
                        .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Eroare: " + e.getMessage(), Toast.LENGTH_LONG).show());
            });

            passwordResetDialog.setNegativeButton("Anulează", (dialog, which) -> {
            });

            passwordResetDialog.create().show();
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completează toate câmpurile!", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        Toast.makeText(LoginActivity.this, "Conectare reușită!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(LoginActivity.this, "Eroare: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }
}
