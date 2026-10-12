package com.printxpress.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.Timestamp;
import com.printxpress.app.databinding.ActivityLoginBinding;
import com.printxpress.app.models.User;
import com.printxpress.app.utils.FirebaseHelper;
import com.printxpress.app.utils.ValidationHelper;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private static final String ADMIN_EMAIL = BuildConfig.ADMIN_EMAIL;
    private static final String ADMIN_PASSWORD = BuildConfig.ADMIN_PASSWORD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRoleSpinner();
        binding.btnLogin.setOnClickListener(v -> handleLogin());
        
        binding.txtForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        binding.txtRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void setupRoleSpinner() {
        String[] roles = {"Customer", "Admin"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRole.setAdapter(adapter);
    }

    private void handleLogin() {
        String email = binding.edtEmail.getText().toString().trim();
        String password = binding.edtPassword.getText().toString().trim();
        String role = binding.spinnerRole.getSelectedItem().toString();

        if (role.equals("Admin")) {
            if (email.equals(ADMIN_EMAIL) && password.equals(ADMIN_PASSWORD)) {
                showLoading(true);
                FirebaseHelper.getInstance().getAuth()
                        .signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            showLoading(false);
                            Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            // If sign-in fails for any reason, try creating the account
                            // This covers cases where the user doesn't exist yet
                            createAdminAccount(email, password);
                        });
            } else {
                Toast.makeText(this, "Invalid Admin credentials.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (!ValidationHelper.isNotEmpty(email)) {
            Toast.makeText(this, R.string.err_email_invalid, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ValidationHelper.isValidEmail(email)) {
            Toast.makeText(this, R.string.err_email_invalid, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ValidationHelper.isNotEmpty(password)) {
            Toast.makeText(this, R.string.err_password_short, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ValidationHelper.isPasswordStrong(password)) {
            Toast.makeText(this, R.string.err_password_short, Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        FirebaseHelper.getInstance().getAuth()
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    showLoading(false);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, getString(R.string.err_auth_failed) + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void createAdminAccount(String email, String password) {
        FirebaseHelper.getInstance().getAuth()
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    User adminUser = new User(uid, "System Admin", email, "0000000000", Timestamp.now());
                    
                    FirebaseHelper.getInstance().getFirestore()
                            .collection("users")
                            .document(uid)
                            .set(adminUser)
                            .addOnSuccessListener(aVoid -> {
                                showLoading(false);
                                Toast.makeText(this, "Admin account initialized.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                showLoading(false);
                                Toast.makeText(this, "Failed to save admin info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    if (e instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                        // User already exists, but sign-in failed (likely wrong password in code vs firebase)
                        Toast.makeText(this, "Admin authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Admin initialization failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
        binding.edtEmail.setEnabled(!isLoading);
        binding.edtPassword.setEnabled(!isLoading);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
