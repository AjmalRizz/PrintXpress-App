package com.printxpress.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.printxpress.app.databinding.ActivityForgotPasswordBinding;
import com.printxpress.app.utils.FirebaseHelper;
import com.printxpress.app.utils.ValidationHelper;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSendReset.setOnClickListener(v -> handlePasswordReset());
    }

    private void handlePasswordReset() {
        String email = binding.edtEmail.getText().toString().trim();

        if (!ValidationHelper.isNotEmpty(email) || !ValidationHelper.isValidEmail(email)) {
            Toast.makeText(this, R.string.err_email_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        FirebaseHelper.getInstance().getAuth()
                .sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(ForgotPasswordActivity.this, R.string.reset_email_sent, Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(ForgotPasswordActivity.this, getString(R.string.err_reset_failed) + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSendReset.setEnabled(!isLoading);
        binding.edtEmail.setEnabled(!isLoading);
    }
}
