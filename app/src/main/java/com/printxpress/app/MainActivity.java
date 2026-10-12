package com.printxpress.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.badge.BadgeDrawable;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.printxpress.app.databinding.ActivityMainBinding;
import com.printxpress.app.fragments.HomeFragment;
import com.printxpress.app.fragments.MyOrdersFragment;
import com.printxpress.app.fragments.NotificationsFragment;
import com.printxpress.app.fragments.ProfileFragment;
import com.printxpress.app.utils.DatabaseSeeder;
import com.printxpress.app.utils.FirebaseHelper;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private ListenerRegistration notificationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Ensure user is logged in
        if (!FirebaseHelper.getInstance().isLoggedIn()) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Run Database Seeder
        DatabaseSeeder.seedProductsIfEmpty(success -> {
            if (success) {
                Log.d(TAG, "Products check & seed completed.");
            } else {
                Log.e(TAG, "Failed to seed products database.");
            }
        });

        // Setup bottom navigation selection
        binding.bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_orders) {
                selectedFragment = new MyOrdersFragment();
            } else if (itemId == R.id.nav_notifications) {
                selectedFragment = new NotificationsFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        // Set default fragment on startup
        if (savedInstanceState == null) {
            String goToTab = getIntent().getStringExtra("go_to_tab");
            if ("orders".equals(goToTab)) {
                binding.bottomNav.setSelectedItemId(R.id.nav_orders);
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, new HomeFragment())
                        .commit();
            }
        }

        // Setup real-time listener for unread notifications count
        setupNotificationBadgeListener();
    }

    private void setupNotificationBadgeListener() {
        String uid = FirebaseHelper.getInstance().getCurrentUserId();
        if (uid == null) return;

        FirebaseFirestore db = FirebaseHelper.getInstance().getFirestore();
        Query query = db.collection("notifications")
                .whereEqualTo("uid", uid)
                .whereEqualTo("isRead", false);

        notificationListener = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen failed for unread notifications.", error);
                return;
            }

            if (value != null) {
                int unreadCount = value.size();
                BadgeDrawable badge = binding.bottomNav.getOrCreateBadge(R.id.nav_notifications);
                if (unreadCount > 0) {
                    badge.setVisible(true);
                    badge.setNumber(unreadCount);
                    badge.setBackgroundColor(getResources().getColor(R.color.error));
                    badge.setBadgeTextColor(getResources().getColor(R.color.text_white));
                } else {
                    badge.setVisible(false);
                    binding.bottomNav.removeBadge(R.id.nav_notifications);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationListener != null) {
            notificationListener.remove();
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.bottomNav.getSelectedItemId() != R.id.nav_home) {
            binding.bottomNav.setSelectedItemId(R.id.nav_home);
        } else {
            super.onBackPressed();
        }
    }
}
