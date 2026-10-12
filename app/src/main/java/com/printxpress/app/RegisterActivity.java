package com.printxpress.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.printxpress.app.databinding.ActivityRegisterBinding;
import com.printxpress.app.models.User;
import com.printxpress.app.utils.FirebaseHelper;
import com.printxpress.app.utils.ValidationHelper;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupValidation();
        binding.btnRegister.setOnClickListener(v -> handleRegister());
        binding.txtLoginLink.setOnClickListener(v -> finish());
    }

    private void setupValidation() {
        binding.edtFullname.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateName(s.toString());
            }
        });

        binding.edtEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail(s.toString());
            }
        });

        binding.edtPhone.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePhone(s.toString());
            }
        });

        binding.edtPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
                validateConfirmPassword(binding.edtConfirmPassword.getText().toString());
            }
        });

        binding.edtConfirmPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateConfirmPassword(s.toString());
            }
        });
    }

    private boolean validateName(String name) {
        if (!ValidationHelper.isNotEmpty(name)) {
            binding.layoutFullname.setError(getString(R.string.err_name_short));
            return false;
        } else if (name.length() < 2) {
            binding.layoutFullname.setError(getString(R.string.err_name_short));
            return false;
        } else {
            binding.layoutFullname.setError(null);
            return true;
        }
    }

    private boolean validateEmail(String email) {
        if (!ValidationHelper.isValidEmail(email)) {
            binding.layoutEmail.setError(getString(R.string.err_email_invalid));
            return false;
        } else {
            binding.layoutEmail.setError(null);
            return true;
        }
    }

    private boolean validatePhone(String phone) {
        if (!ValidationHelper.isValidSriLankanPhone(phone)) {
            binding.layoutPhone.setError(getString(R.string.err_phone_invalid));
            return false;
        } else {
            binding.layoutPhone.setError(null);
            return true;
        }
    }

    private boolean validatePassword(String password) {
        if (!ValidationHelper.isPasswordStrong(password)) {
            binding.layoutPassword.setError(getString(R.string.err_password_short));
            return false;
        } else {
            binding.layoutPassword.setError(null);
            return true;
        }
    }

    private boolean validateConfirmPassword(String confirmPassword) {
        String password = binding.edtPassword.getText().toString();
        if (!password.equals(confirmPassword)) {
            binding.layoutConfirmPassword.setError(getString(R.string.err_passwords_mismatch));
            return false;
        } else {
            binding.layoutConfirmPassword.setError(null);
            return true;
        }
    }

    private void handleRegister() {
        String name = binding.edtFullname.getText().toString().trim();
        String email = binding.edtEmail.getText().toString().trim();
        String phone = binding.edtPhone.getText().toString().trim();
        String password = binding.edtPassword.getText().toString().trim();
        String confirmPassword = binding.edtConfirmPassword.getText().toString().trim();

        boolean isNameValid = validateName(name);
        boolean isEmailValid = validateEmail(email);
        boolean isPhoneValid = validatePhone(phone);
        boolean isPasswordValid = validatePassword(password);
        boolean isConfirmValid = validateConfirmPassword(confirmPassword);

        if (!(isNameValid && isEmailValid && isPhoneValid && isPasswordValid && isConfirmValid)) {
            return;
        }

        showLoading(true);
        FirebaseHelper.getInstance().getAuth()
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        saveUserToFirestore(firebaseUser.getUid(), name, email, phone);
                    } else {
                        showLoading(false);
                        Toast.makeText(RegisterActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(RegisterActivity.this, getString(R.string.err_auth_failed) + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveUserToFirestore(String uid, String name, String email, String phone) {
        User user = new User(uid, name, email, phone, Timestamp.now());
        
        FirebaseHelper.getInstance().getFirestore()
                .collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(RegisterActivity.this, "Firestore save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!isLoading);
        binding.edtFullname.setEnabled(!isLoading);
        binding.edtEmail.setEnabled(!isLoading);
        binding.edtPhone.setEnabled(!isLoading);
        binding.edtPassword.setEnabled(!isLoading);
        binding.edtConfirmPassword.setEnabled(!isLoading);
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) {}
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

