package com.printxpress.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.printxpress.app.databinding.ActivityGuidelinesBinding;

public class GuidelinesActivity extends AppCompatActivity {

    private ActivityGuidelinesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGuidelinesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
    }
}
