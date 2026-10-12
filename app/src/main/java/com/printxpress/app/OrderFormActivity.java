package com.printxpress.app;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.printxpress.app.databinding.ActivityOrderFormBinding;
import com.printxpress.app.models.Address;
import com.printxpress.app.models.Order;
import com.printxpress.app.models.Notification;
import com.printxpress.app.utils.FirebaseHelper;
import com.printxpress.app.utils.PriceHelper;
import com.printxpress.app.utils.ValidationHelper;

import java.util.ArrayList;
import java.util.List;

public class OrderFormActivity extends AppCompatActivity {

    private static final String TAG = "OrderFormActivity";
    private ActivityOrderFormBinding binding;

    private String productId;
    private String productName;
    private String productImageUrl;
    private String variantId;
    private String selectedSize;
    private String selectedMaterial;
    private int quantity;
    private double originalTotalPrice;
    private double totalPrice;
    private double discountAmount = 0.0;
    private boolean isPromoApplied = false;

    private Uri selectedFileUri;
    private String selectedFileName;
    private long selectedFileSize = 0;

    private List<Address> addressList;
    private ArrayAdapter<Address> addressAdapter;
    private Address selectedAddress;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        handleFileSelection(uri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Retrieve product specs extras
        productId = getIntent().getStringExtra("product_id");
        productName = getIntent().getStringExtra("product_name");
        productImageUrl = getIntent().getStringExtra("product_image_url");
        variantId = getIntent().getStringExtra("variant_id");
        selectedSize = getIntent().getStringExtra("selected_size");
        selectedMaterial = getIntent().getStringExtra("selected_material");
        quantity = getIntent().getIntExtra("quantity", 1);
        totalPrice = getIntent().getDoubleExtra("total_price", 0.0);
        originalTotalPrice = totalPrice;

        if (productId == null || variantId == null) {
            Toast.makeText(this, "Invalid order information.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.btnBack.setOnClickListener(v -> finish());
        binding.layoutUploadBox.setOnClickListener(v -> openFilePicker());
        binding.btnRemoveFile.setOnClickListener(v -> removeSelectedFile());
        binding.btnApplyPromo.setOnClickListener(v -> applyPromoCode());

        binding.groupDelivery.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_delivery) {
                binding.layoutAddressSelect.setVisibility(View.VISIBLE);
            } else {
                binding.layoutAddressSelect.setVisibility(View.GONE);
            }
        });

        binding.btnAddAddress.setOnClickListener(v -> {
            startActivity(new Intent(OrderFormActivity.this, AddAddressActivity.class));
        });

        binding.btnSubmitOrder.setOnClickListener(v -> validateAndPlaceOrder());

        addressList = new ArrayList<>();
        addressAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, addressList);
        addressAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerAddress.setAdapter(addressAdapter);

        binding.spinnerAddress.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedAddress = addressList.get(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedAddress = null;
            }
        });

        renderOrderSummary();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserAddresses();
    }

    private void renderOrderSummary() {
        binding.txtSummaryName.setText(productName);
        binding.txtSummarySpecs.setText(getString(R.string.label_size) + ": " + selectedSize + " | " + getString(R.string.label_material) + ": " + selectedMaterial);
        binding.txtSummaryQty.setText(getString(R.string.label_quantity) + ": " + quantity);
        updateTotalPriceUI();

        if (productImageUrl != null && !productImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(productImageUrl)
                    .placeholder(R.drawable.placeholder_product)
                    .into(binding.imgProductSummary);
        }
    }

    private void updateTotalPriceUI() {
        String priceText = getString(R.string.label_total_price) + " " + PriceHelper.formatLKR(totalPrice);
        if (isPromoApplied) {
            priceText += " (Promo Applied)";
        }
        binding.txtSummaryPrice.setText(priceText);
    }

    private void applyPromoCode() {
        if (isPromoApplied) {
            Toast.makeText(this, "Promo already applied!", Toast.LENGTH_SHORT).show();
            return;
        }

        String code = binding.edtPromoCode.getText().toString().trim().toUpperCase();
        double discountPercent = 0.0;

        switch (code) {
            case "XMAS25":
                discountPercent = 0.25;
                break;
            case "RAMZAN15":
                discountPercent = 0.15;
                break;
            case "VESAK20":
                discountPercent = 0.20;
                break;
            default:
                Toast.makeText(this, "Invalid promo code!", Toast.LENGTH_SHORT).show();
                return;
        }

        discountAmount = originalTotalPrice * discountPercent;
        totalPrice = originalTotalPrice - discountAmount;
        isPromoApplied = true;

        binding.edtPromoCode.setEnabled(false);
        binding.btnApplyPromo.setEnabled(false);
        updateTotalPriceUI();
        Toast.makeText(this, "Promo code applied successfully!", Toast.LENGTH_SHORT).show();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"image/jpeg", "image/png", "application/pdf"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        filePickerLauncher.launch(Intent.createChooser(intent, "Select Design File"));
    }

    private void handleFileSelection(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            if (cursor.moveToFirst()) {
                selectedFileName = cursor.getString(nameIndex);
                selectedFileSize = cursor.getLong(sizeIndex);
            }
            cursor.close();
        }

        // Validate File Type
        if (!ValidationHelper.isValidFileType(selectedFileName)) {
            Toast.makeText(this, R.string.error_invalid_file_type, Toast.LENGTH_LONG).show();
            removeSelectedFile();
            return;
        }

        // Validate File Size (Max 20MB)
        if (selectedFileSize > 20 * 1024 * 1024) {
            Toast.makeText(this, R.string.error_file_too_large, Toast.LENGTH_LONG).show();
            removeSelectedFile();
            return;
        }

        selectedFileUri = uri;
        binding.txtFileName.setText(selectedFileName);
        binding.layoutFilePreview.setVisibility(View.VISIBLE);

        if (selectedFileName.toLowerCase().endsWith(".pdf")) {
            binding.imgFileThumbnail.setImageResource(R.drawable.placeholder_product); // Displays placeholder for PDF files
        } else {
            Glide.with(this).load(uri).into(binding.imgFileThumbnail);
        }
    }

    private void removeSelectedFile() {
        selectedFileUri = null;
        selectedFileName = null;
        selectedFileSize = 0;
        binding.layoutFilePreview.setVisibility(View.GONE);
    }

    private void loadUserAddresses() {
        String uid = FirebaseHelper.getInstance().getCurrentUserId();
        if (uid == null) return;

        FirebaseFirestore db = FirebaseHelper.getInstance().getFirestore();
        db.collection("addresses")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    addressList.clear();
                    int defaultIndex = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Address address = doc.toObject(Address.class);
                        addressList.add(address);
                        if (address.getIsDefault()) {
                            defaultIndex = addressList.size() - 1;
                        }
                    }
                    addressAdapter.notifyDataSetChanged();
                    
                    if (!addressList.isEmpty()) {
                        binding.spinnerAddress.setSelection(defaultIndex);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load addresses", e);
                });
    }

    private void validateAndPlaceOrder() {
        String customText = binding.edtInstructions.getText().toString().trim();
        boolean isDelivery = binding.radioDelivery.isChecked();

        // 1. Either a file OR custom text must be provided
        if (selectedFileUri == null && !ValidationHelper.isNotEmpty(customText)) {
            Toast.makeText(this, R.string.error_no_design_or_text, Toast.LENGTH_LONG).show();
            return;
        }

        // 2. If Delivery, address must be selected
        if (isDelivery && selectedAddress == null) {
            Toast.makeText(this, R.string.error_no_address, Toast.LENGTH_LONG).show();
            return;
        }

        showLoading(true, "Uploading design...");

        if (selectedFileUri != null) {
            uploadFileAndPlaceOrder(customText, isDelivery);
        } else {
            submitOrderToFirestore("", customText, isDelivery);
        }
    }

    private void uploadFileAndPlaceOrder(String customText, boolean isDelivery) {
        // MOCK UPLOAD: Simulating upload because Storage requires Blaze plan
        showLoading(true, "Uploading design: 0%");
        
        android.os.Handler handler = new android.os.Handler();
        final int[] progress = {0};
        
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (progress[0] < 100) {
                    progress[0] += 10;
                    showLoading(true, "Uploading design: " + progress[0] + "%");
                    handler.postDelayed(this, 300); // 300ms intervals
                } else {
                    // Simulation complete - use a high-quality placeholder image
                    String mockUrl = "https://images.unsplash.com/photo-1572044162444-ad60f128bde7?q=80&w=500&auto=format&fit=crop";
                    submitOrderToFirestore(mockUrl, customText, isDelivery);
                }
            }
        };
        handler.postDelayed(runnable, 300);
    }

    private void submitOrderToFirestore(String fileUrl, String customText, boolean isDelivery) {
        showLoading(true, "Placing your order...");
        
        FirebaseFirestore db = FirebaseHelper.getInstance().getFirestore();
        com.google.firebase.firestore.WriteBatch batch = db.batch();
        
        DocumentReference orderRef = db.collection("orders").document();
        String orderId = orderRef.getId();
        String uid = FirebaseHelper.getInstance().getCurrentUserId();
        String addressId = isDelivery ? selectedAddress.getAddressId() : "";
        String deliveryType = isDelivery ? "Delivery" : "Pickup";

        Order order = new Order(
                orderId,
                uid,
                productId,
                productName,
                variantId,
                quantity,
                customText,
                fileUrl,
                deliveryType,
                addressId,
                "Pending",
                totalPrice,
                Timestamp.now(),
                Timestamp.now()
        );

        batch.set(orderRef, order);

        // Add Notification to the same batch
        DocumentReference notifRef = db.collection("notifications").document();
        Notification notification = new Notification(
                notifRef.getId(),
                uid,
                orderId,
                "Order Placed",
                "Your order has been placed successfully!",
                false,
                Timestamp.now()
        );
        batch.set(notifRef, notification);

        // Commit both in one network request
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    showLoading(false, "");
                    Intent intent = new Intent(OrderFormActivity.this, OrderSuccessActivity.class);
                    intent.putExtra("order_id", orderId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false, "");
                    Toast.makeText(OrderFormActivity.this, getString(R.string.err_order_failed) + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Removed sendOrderNotification as it's now part of the batch

    private void showLoading(boolean isLoading, String statusText) {
        binding.layoutLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.txtLoadingLabel.setText(statusText);
        binding.btnSubmitOrder.setEnabled(!isLoading);
        binding.edtInstructions.setEnabled(!isLoading);
        binding.radioPickup.setEnabled(!isLoading);
        binding.radioDelivery.setEnabled(!isLoading);
        binding.spinnerAddress.setEnabled(!isLoading);
        binding.btnRemoveFile.setEnabled(!isLoading);
        binding.layoutUploadBox.setEnabled(!isLoading);
    }

    @Override
    public void onBackPressed() {
        if (binding.layoutLoading.getVisibility() != View.VISIBLE) {
            super.onBackPressed();
        }
    }
}
