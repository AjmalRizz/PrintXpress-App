package com.printxpress.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.printxpress.app.OrderDetailActivity;
import com.printxpress.app.R;
import com.printxpress.app.adapters.OrderAdapter;
import com.printxpress.app.databinding.FragmentMyOrdersBinding;
import com.printxpress.app.models.Order;
import com.printxpress.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class MyOrdersFragment extends Fragment {

    private static final String TAG = "MyOrdersFragment";
    private FragmentMyOrdersBinding binding;
    private List<Order> orderList;
    private OrderAdapter adapter;
    private ListenerRegistration ordersListener;
    private String currentFilter = "active"; // "active", "completed", "cancelled"

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupTabLayout();
        loadOrders();
    }

    private void setupRecyclerView() {
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        orderList = new ArrayList<>();
        adapter = new OrderAdapter(getContext(), orderList, order -> {
            Intent intent = new Intent(getActivity(), OrderDetailActivity.class);
            intent.putExtra("order_id", order.getOrderId());
            startActivity(intent);
        });
        binding.rvOrders.setAdapter(adapter);
    }

    private void setupTabLayout() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tab_active));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tab_completed));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tab_cancelled));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: currentFilter = "active"; break;
                    case 1: currentFilter = "completed"; break;
                    case 2: currentFilter = "cancelled"; break;
                }
                loadOrders();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadOrders() {
        if (ordersListener != null) {
            ordersListener.remove();
        }

        String uid = FirebaseHelper.getInstance().getCurrentUserId();
        if (uid == null) return;

        showLoading(true);

        FirebaseFirestore db = FirebaseHelper.getInstance().getFirestore();
        Query query = db.collection("orders")
                .whereEqualTo("uid", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        ordersListener = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error listening for orders", error);
                showLoading(false);
                return;
            }

            if (value == null) {
                showLoading(false);
                return;
            }

            orderList.clear();
            for (QueryDocumentSnapshot doc : value) {
                Order order = doc.toObject(Order.class);
                if (matchesFilter(order.getStatus())) {
                    orderList.add(order);
                }
            }

            adapter.notifyDataSetChanged();
            showLoading(false);

            binding.layoutEmpty.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
            binding.rvOrders.setVisibility(orderList.isEmpty() ? View.GONE : View.VISIBLE);
        });
    }

    private boolean matchesFilter(String status) {
        switch (currentFilter) {
            case "active":
                return "Pending".equals(status) || "Processing".equals(status) || "Printing".equals(status) || "Ready".equals(status);
            case "completed":
                return "Completed".equals(status);
            case "cancelled":
                return "Cancelled".equals(status);
            default:
                return false;
        }
    }

    private void showLoading(boolean isLoading) {
        if (binding == null) return;
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            binding.rvOrders.setVisibility(View.GONE);
            binding.layoutEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (ordersListener != null) {
            ordersListener.remove();
        }
        binding = null;
    }
}
