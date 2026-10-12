package com.printxpress.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.printxpress.app.databinding.ActivitySplashBinding;
import com.printxpress.app.utils.FirebaseHelper;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Wait 2 seconds and check login status
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing()) {
                if (FirebaseHelper.getInstance().isLoggedIn()) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                finish();
            }
        }, 2000);
    }

    @Override
    public void onBackPressed() {
        // Do nothing to prevent users from backing out during the splash transition
    }
}
