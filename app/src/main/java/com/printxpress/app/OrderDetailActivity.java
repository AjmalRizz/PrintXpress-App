package com.printxpress.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.printxpress.app.databinding.ActivityOrderDetailBinding;
import com.printxpress.app.models.Order;
import com.printxpress.app.utils.DateTimeHelper;
import com.printxpress.app.utils.FirebaseHelper;
import com.printxpress.app.utils.PriceHelper;

public class OrderDetailActivity extends AppCompatActivity {

    private ActivityOrderDetailBinding binding;
    private String orderId;
    private Order currentOrder;
    private ListenerRegistration orderListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderId = getIntent().getStringExtra("order_id");
        if (orderId == null) {
            Toast.makeText(this, "Order not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCancelOrder.setOnClickListener(v -> showCancelConfirmDialog());
        binding.btnSimulateTracking.setOnClickListener(v -> simulateProgress());

        loadOrderDetails();
    }

    private void loadOrderDetails() {
        showLoading(true);
        FirebaseFirestore db = FirebaseHelper.getInstance().getFirestore();

        orderListener = db.collection("orders")
                .document(orderId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        showLoading(false);
                        Toast.makeText(this, "Failed to load order: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        currentOrder = documentSnapshot.toObject(Order.class);
                        if (currentOrder != null) {
                            renderOrderDetails();
                        }
                    }
                    showLoading(false);
                });
    }

    private void renderOrderDetails() {
        binding.txtOrderIdLabel.setText("Order #" + orderId.substring(0, Math.min(8, orderId.length())));
        binding.txtOrderDate.setText(DateTimeHelper.formatDate(currentOrder.getCreatedAt()));
        binding.txtProductName.setText(currentOrder.getProductName());
        binding.txtVariantInfo.setText(
                getString(R.string.label_quantity) + ": " + currentOrder.getQuantity()
                        + " | " + getString(R.string.delivery_option_title) + ": " + currentOrder.getDeliveryType()
        );
        binding.txtTotalPrice.setText(PriceHelper.formatLKR(currentOrder.getTotalPrice()));

        // Status badge background
        String status = currentOrder.getStatus();
        binding.txtStatusBadge.setText(status);
        
        updateTrackingProgress(status);

        int bgResId;
        switch (status) {
            case "Processing": bgResId = R.drawable.bg_status_processing; break;
            case "Printing":   bgResId = R.drawable.bg_status_printing;   break;
            case "Ready":      bgResId = R.drawable.bg_status_ready;      break;
            case "Completed":  bgResId = R.drawable.bg_status_completed;  break;
            case "Cancelled":  bgResId = R.drawable.bg_status_cancelled;  break;
            default:           bgResId = R.drawable.bg_status_pending;    break;
        }
        binding.txtStatusBadge.setBackground(ContextCompat.getDrawable(this, bgResId));

        // Design preview
        if (currentOrder.getDesignFileUrl() != null && !currentOrder.getDesignFileUrl().isEmpty()) {
            binding.imgDesignPreview.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(currentOrder.getDesignFileUrl())
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(binding.imgDesignPreview);
            binding.txtCustomText.setVisibility(View.GONE);
        } else if (currentOrder.getCustomText() != null && !currentOrder.getCustomText().isEmpty()) {
            binding.imgDesignPreview.setVisibility(View.GONE);
            binding.txtCustomText.setVisibility(View.VISIBLE);
            binding.txtCustomText.setText(currentOrder.getCustomText());
        } else {
            binding.txtCustomText.setText("No design or instructions provided.");
            binding.txtCustomText.setVisibility(View.VISIBLE);
        }

        // Show cancel button only for Pending orders
        binding.layoutActions.setVisibility("Pending".equals(status) ? View.VISIBLE : View.GONE);
    }

    private void updateTrackingProgress(String status) {
        int progress = 0;
        int activeColor = ContextCompat.getColor(this, R.color.accent);
        int inactiveColor = ContextCompat.getColor(this, R.color.text_secondary);

        // Reset text styles
        binding.stepPending.setTextColor(inactiveColor);
        binding.stepProcessing.setTextColor(inactiveColor);
        binding.stepPrinting.setTextColor(inactiveColor);
        binding.stepReady.setTextColor(inactiveColor);
        binding.stepCompleted.setTextColor(inactiveColor);

        switch (status) {
            case "Pending":
                progress = 10;
                binding.stepPending.setTextColor(activeColor);
                break;
            case "Processing":
                progress = 30;
                binding.stepProcessing.setTextColor(activeColor);
                break;
            case "Printing":
                progress = 55;
                binding.stepPrinting.setTextColor(activeColor);
                break;
            case "Ready":
                progress = 80;
                binding.stepReady.setTextColor(activeColor);
                break;
            case "Completed":
                progress = 100;
                binding.stepCompleted.setTextColor(activeColor);
                break;
            case "Cancelled":
                binding.cardTracking.setVisibility(View.GONE);
                return;
        }
        
        binding.cardTracking.setVisibility(View.VISIBLE);
        binding.trackingProgressBar.setProgress(progress);
    }

    private void simulateProgress() {
        if (currentOrder == null || "Cancelled".equals(currentOrder.getStatus())) {
            Toast.makeText(this, "Cannot simulate for cancelled orders", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSimulateTracking.setEnabled(false);
        binding.btnSimulateTracking.setText("Simulating...");

        android.os.Handler handler = new android.os.Handler();
        String[] statuses = {"Pending", "Processing", "Printing", "Ready", "Completed"};
        
        for (int i = 0; i < statuses.length; i++) {
            final String nextStatus = statuses[i];
            handler.postDelayed(() -> {
                if (!isFinishing()) {
                    updateStatusOnServer(nextStatus);
                    if ("Completed".equals(nextStatus)) {
                        binding.btnSimulateTracking.setText("Simulation Done");
                        binding.btnSimulateTracking.setEnabled(true);
                    }
                }
            }, i * 3000); // 3 seconds per step
        }
    }

    private void updateStatusOnServer(String newStatus) {
        FirebaseFirestore db = FirebaseHelper.getInstance().getFirestore();
        db.collection("orders").document(orderId)
                .update("status", newStatus, "updatedAt", Timestamp.now());
    }

    private void showCancelConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Order")
                .setMessage("Are you sure you want to cancel this order? This action cannot be undone.")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> cancelOrder())
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void cancelOrder() {
        showLoading(true);
        binding.layoutActions.setVisibility(View.GONE);

        FirebaseFirestore db = FirebaseHelper.getInstance().getFirestore();
        db.collection("orders")
                .document(orderId)
                .update("status", "Cancelled", "updatedAt", Timestamp.now())
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(this, "Order cancelled successfully.", Toast.LENGTH_SHORT).show();

                    // Add notification for cancellation
                    sendCancelNotification();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    binding.layoutActions.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Failed to cancel order: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void sendCancelNotification() {
        String uid = FirebaseHelper.getInstance().getCurrentUserId();
        if (uid == null) return;

        FirebaseFirestore db = FirebaseHelper.getInstance().getFirestore();
        com.google.firebase.firestore.DocumentReference notifRef = db.collection("notifications").document();
        com.printxpress.app.models.Notification notification = new com.printxpress.app.models.Notification(
                notifRef.getId(), uid, orderId,
                "Order Cancelled",
                "Your order has been cancelled.",
                false, Timestamp.now()
        );
        notifRef.set(notification);
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderListener != null) orderListener.remove();
    }
}
