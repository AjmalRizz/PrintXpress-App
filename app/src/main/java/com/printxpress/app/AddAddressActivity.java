package com.printxpress.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.printxpress.app.databinding.ActivityAddAddressBinding;
import com.printxpress.app.models.Address;
import com.printxpress.app.utils.FirebaseHelper;
import com.printxpress.app.utils.ValidationHelper;

public class AddAddressActivity extends AppCompatActivity {

    private ActivityAddAddressBinding binding;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        uid = FirebaseHelper.getInstance().getCurrentUserId();
        if (uid == null) { finish(); return; }

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSaveAddress.setOnClickListener(v -> validateAndSave());
    }

    private void validateAndSave() {
        String label = binding.edtLabel.getText().toString().trim();
        String fullAddress = binding.edtFullAddress.getText().toString().trim();
        String city = binding.edtCity.getText().toString().trim();
        boolean isDefault = binding.chkDefault.isChecked();

        if (!ValidationHelper.isNotEmpty(label) || label.length() < 2) {
            Toast.makeText(this, R.string.err_address_label_short, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ValidationHelper.isNotEmpty(fullAddress)) {
            Toast.makeText(this, R.string.err_full_address_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ValidationHelper.isNotEmpty(city)) {
            Toast.makeText(this, R.string.err_city_required, Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        if (isDefault) {
            // Unset all existing defaults first, then save
            unsetAllDefaultsThenSave(label, fullAddress, city);
        } else {
            saveAddress(label, fullAddress, city, false);
        }
    }

    private void unsetAllDefaultsThenSave(String label, String fullAddress, String city) {
        FirebaseFirestore db = FirebaseHelper.getInstance().getFirestore();
        db.collection("addresses")
                .whereEqualTo("uid", uid)
                .whereEqualTo("isDefault", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.update(doc.getReference(), "isDefault", false);
                    }
                    batch.commit().addOnSuccessListener(aVoid ->
                            saveAddress(label, fullAddress, city, true)
                    ).addOnFailureListener(e -> {
                        showLoading(false);
                        Toast.makeText(this, "Error updating defaults: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveAddress(String label, String fullAddress, String city, boolean isDefault) {
        FirebaseFirestore db = FirebaseHelper.getInstance().getFirestore();
        com.google.firebase.firestore.DocumentReference ref = db.collection("addresses").document();
        String addressId = ref.getId();

        Address address = new Address(addressId, uid, label, fullAddress, city, isDefault);

        ref.set(address)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(this, "Address saved successfully.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Failed to save address: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSaveAddress.setEnabled(!isLoading);
        binding.edtLabel.setEnabled(!isLoading);
        binding.edtFullAddress.setEnabled(!isLoading);
        binding.edtCity.setEnabled(!isLoading);
        binding.chkDefault.setEnabled(!isLoading);
    }
}
