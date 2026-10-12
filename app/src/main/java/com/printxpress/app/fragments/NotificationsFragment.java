package com.printxpress.app.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.printxpress.app.adapters.NotificationAdapter;
import com.printxpress.app.databinding.FragmentNotificationsBinding;
import com.printxpress.app.models.Notification;
import com.printxpress.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private static final String TAG = "NotificationsFragment";
    private FragmentNotificationsBinding binding;
    private List<Notification> notificationList;
    private NotificationAdapter adapter;
    private ListenerRegistration notifListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        loadNotifications();

        binding.btnMarkRead.setOnClickListener(v -> markAllAsRead());
    }

    private void setupRecyclerView() {
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(getContext(), notificationList);
        binding.rvNotifications.setAdapter(adapter);
    }

    private void loadNotifications() {
        String uid = FirebaseHelper.getInstance().getCurrentUserId();
        if (uid == null) return;

        showLoading(true);
        FirebaseFirestore db = FirebaseHelper.getInstance().getFirestore();

        notifListener = db.collection("notifications")
                .whereEqualTo("uid", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading notifications", error);
                        showLoading(false);
                        return;
                    }

                    if (value == null) {
                        showLoading(false);
                        return;
                    }

                    notificationList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Notification n = doc.toObject(Notification.class);
                        notificationList.add(n);
                    }

                    adapter.notifyDataSetChanged();
                    showLoading(false);

                    boolean isEmpty = notificationList.isEmpty();
                    binding.txtEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    binding.rvNotifications.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                });
    }

    private void markAllAsRead() {
        String uid = FirebaseHelper.getInstance().getCurrentUserId();
        if (uid == null) return;

        FirebaseFirestore db = FirebaseHelper.getInstance().getFirestore();
        db.collection("notifications")
                .whereEqualTo("uid", uid)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(getContext(), "No unread notifications.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.update(doc.getReference(), "isRead", true);
                    }
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "All notifications marked as read.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
    }

    private void showLoading(boolean isLoading) {
        if (binding == null) return;
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            binding.rvNotifications.setVisibility(View.GONE);
            binding.txtEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (notifListener != null) notifListener.remove();
        binding = null;
    }
}
