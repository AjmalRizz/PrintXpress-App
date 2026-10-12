package com.printxpress.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.printxpress.app.adapters.AdminUnifiedAdapter;
import com.printxpress.app.databinding.ActivityAdminBinding;
import com.printxpress.app.models.Order;
import com.printxpress.app.models.SupportMessage;
import com.printxpress.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;
    private List<Object> unifiedItems;
    private AdminUnifiedAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLogout.setOnClickListener(v -> {
            FirebaseHelper.getInstance().getAuth().signOut();
            startActivity(new Intent(AdminActivity.this, LoginActivity.class));
            finish();
        });

        setupRecyclerView();
        loadAllData();
    }

    private void setupRecyclerView() {
        binding.rvAllOrders.setLayoutManager(new LinearLayoutManager(this));
        unifiedItems = new ArrayList<>();
        adapter = new AdminUnifiedAdapter(this, unifiedItems, order -> {
            Intent intent = new Intent(AdminActivity.this, OrderDetailActivity.class);
            intent.putExtra("order_id", order.getOrderId());
            startActivity(intent);
        });
        binding.rvAllOrders.setAdapter(adapter);
    }

    private void loadAllData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseHelper.getInstance().getFirestore();
        
        unifiedItems.clear();
        
        // Load Orders
        db.collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(orderSnapshots -> {
                    for (QueryDocumentSnapshot doc : orderSnapshots) {
                        unifiedItems.add(doc.toObject(Order.class));
                    }
                    
                    // Load Messages
                    db.collection("support_messages")
                            .orderBy("createdAt", Query.Direction.DESCENDING)
                            .get()
                            .addOnSuccessListener(messageSnapshots -> {
                                for (QueryDocumentSnapshot doc : messageSnapshots) {
                                    unifiedItems.add(doc.toObject(SupportMessage.class));
                                }
                                
                                // Sort combined list by date
                                sortItemsByDate();
                                
                                binding.progressBar.setVisibility(View.GONE);
                                adapter.notifyDataSetChanged();
                                binding.txtNoOrders.setVisibility(unifiedItems.isEmpty() ? View.VISIBLE : View.GONE);
                            })
                            .addOnFailureListener(e -> {
                                binding.progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Error loading messages: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sortItemsByDate() {
        Collections.sort(unifiedItems, (o1, o2) -> {
            com.google.firebase.Timestamp t1 = (o1 instanceof Order) ? ((Order) o1).getCreatedAt() : ((SupportMessage) o1).getCreatedAt();
            com.google.firebase.Timestamp t2 = (o2 instanceof Order) ? ((Order) o2).getCreatedAt() : ((SupportMessage) o2).getCreatedAt();
            
            if (t1 == null || t2 == null) return 0;
            return t2.compareTo(t1); // Descending order
        });
    }
}
