package com.printxpress.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.printxpress.app.adapters.AddressAdapter;
import com.printxpress.app.databinding.ActivitySavedAddressesBinding;
import com.printxpress.app.models.Address;
import com.printxpress.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class SavedAddressesActivity extends AppCompatActivity {

    private static final String TAG = "SavedAddressesActivity";
    private ActivitySavedAddressesBinding binding;
    private List<Address> addressList;
    private AddressAdapter adapter;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySavedAddressesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        uid = FirebaseHelper.getInstance().getCurrentUserId();
        if (uid == null) { finish(); return; }

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddAddressActivity.class)));

        binding.btnAddFirst.setOnClickListener(v ->
                startActivity(new Intent(this, AddAddressActivity.class)));

        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAddresses();
    }

    private void setupRecyclerView() {
        binding.rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        addressList = new ArrayList<>();
        adapter = new AddressAdapter(this, addressList, new AddressAdapter.OnAddressActionListener() {
            @Override
            public void onSetDefault(Address address) {
                setAddressAsDefault(address);
            }

            @Override
            public void onDelete(Address address) {
                confirmAndDeleteAddress(address);
            }
        });
        binding.rvAddresses.setAdapter(adapter);
    }

    private void loadAddresses() {
        showLoading(true);
        FirebaseFirestore db = FirebaseHelper.getInstance().getFirestore();
        db.collection("addresses")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    addressList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Address address = doc.toObject(Address.class);
                        addressList.add(address);
                    }
                    adapter.notifyDataSetChanged();
                    showLoading(false);

                    boolean isEmpty = addressList.isEmpty();
                    binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    binding.rvAddresses.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Failed to load addresses", e);
                    Toast.makeText(this, "Failed to load addresses.", Toast.LENGTH_SHORT).show();
                });
    }

    private void setAddressAsDefault(Address targetAddress) {
        FirebaseFirestore db = FirebaseHelper.getInstance().getFirestore();
        WriteBatch batch = db.batch();

        // First, unset all existing defaults for this user
        db.collection("addresses")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.update(doc.getReference(), "isDefault", false);
                    }
                    // Now set the target as default
                    db.collection("addresses")
                            .document(targetAddress.getAddressId())
                            .update("isDefault", true)
                            .addOnSuccessListener(aVoid -> {
                                batch.commit();
                                Toast.makeText(this, "Default address updated.", Toast.LENGTH_SHORT).show();
                                loadAddresses();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to update default: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
    }

    private void confirmAndDeleteAddress(Address address) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Address")
                .setMessage("Are you sure you want to delete this address?")
                .setPositiveButton("Delete", (dialog, which) -> deleteAddress(address))
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void deleteAddress(Address address) {
        FirebaseHelper.getInstance().getFirestore()
                .collection("addresses")
                .document(address.getAddressId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Address deleted.", Toast.LENGTH_SHORT).show();
                    loadAddresses();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            binding.rvAddresses.setVisibility(View.GONE);
            binding.layoutEmpty.setVisibility(View.GONE);
        }
    }
}
