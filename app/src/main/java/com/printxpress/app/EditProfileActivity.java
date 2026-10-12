package com.printxpress.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.printxpress.app.databinding.ActivityEditProfileBinding;
import com.printxpress.app.models.User;
import com.printxpress.app.utils.FirebaseHelper;
import com.printxpress.app.utils.ValidationHelper;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private String uid;
    private Uri selectedImageUri;
    private String currentImageUrl = "";

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        Glide.with(this).load(selectedImageUri).into(binding.imgProfile);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        uid = FirebaseHelper.getInstance().getCurrentUserId();
        if (uid == null) { finish(); return; }

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> saveChanges());
        binding.fabEditImage.setOnClickListener(v -> openImagePicker());

        loadCurrentData();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Profile Picture"));
    }

    private void loadCurrentData() {
        FirebaseFirestore db = FirebaseHelper.getInstance().getFirestore();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            binding.edtFullname.setText(user.getName());
                            binding.edtPhone.setText(user.getPhone());
                            binding.edtEmail.setText(user.getEmail());
                            currentImageUrl = user.getProfileImageUrl();
                            
                            if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(currentImageUrl)
                                        .placeholder(R.drawable.ic_person)
                                        .into(binding.imgProfile);
                            }
                        }
                    }
                });
    }

    private void saveChanges() {
        String name = binding.edtFullname.getText().toString().trim();
        String phone = binding.edtPhone.getText().toString().trim();

        if (!ValidationHelper.isNotEmpty(name) || name.length() < 2) {
            Toast.makeText(this, R.string.err_name_short, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ValidationHelper.isValidSriLankanPhone(phone)) {
            Toast.makeText(this, R.string.err_phone_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        if (selectedImageUri != null) {
            // Take persistable URI permission to ensure it can be loaded later across app restarts
            try {
                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                getContentResolver().takePersistableUriPermission(selectedImageUri, takeFlags);
            } catch (SecurityException e) {
                // Ignore if it fails (not all URIs support this)
            }

            // MOCK UPLOAD Simulation
            android.os.Handler handler = new android.os.Handler();
            handler.postDelayed(() -> {
                updateFirestore(name, phone, selectedImageUri.toString());
            }, 1000);
        } else {
            updateFirestore(name, phone, currentImageUrl);
        }
    }

    private void updateFirestore(String name, String phone, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("profileImageUrl", imageUrl);

        FirebaseHelper.getInstance().getFirestore()
                .collection("users").document(uid)
                .update(updates)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSave.setEnabled(!isLoading);
        binding.edtFullname.setEnabled(!isLoading);
        binding.edtPhone.setEnabled(!isLoading);
        binding.fabEditImage.setEnabled(!isLoading);
    }
}
