package com.printxpress.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.printxpress.app.EditProfileActivity;
import com.printxpress.app.FAQActivity;
import com.printxpress.app.GuidelinesActivity;
import com.printxpress.app.LoginActivity;
import com.printxpress.app.R;
import com.printxpress.app.SavedAddressesActivity;
import com.printxpress.app.databinding.FragmentProfileBinding;
import com.printxpress.app.models.User;
import com.printxpress.app.utils.FirebaseHelper;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadUserProfile();

        binding.menuEditProfile.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), EditProfileActivity.class)));

        binding.menuSavedAddresses.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), SavedAddressesActivity.class)));

        binding.menuGuidelines.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), GuidelinesActivity.class)));

        binding.menuFaq.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), FAQActivity.class)));

        binding.btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void loadUserProfile() {
        String uid = FirebaseHelper.getInstance().getCurrentUserId();
        if (uid == null) return;

        FirebaseFirestore db = FirebaseHelper.getInstance().getFirestore();
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            binding.txtName.setText(user.getName());
                            binding.txtEmail.setText(user.getEmail());

                            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                                Glide.with(ProfileFragment.this)
                                        .load(user.getProfileImageUrl())
                                        .placeholder(R.drawable.ic_person)
                                        .into(binding.imgAvatar);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load user profile", e);
                    Toast.makeText(getContext(), "Failed to load profile.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.menu_logout)
                .setMessage(R.string.logout_confirm)
                .setPositiveButton(R.string.menu_logout, (dialog, which) -> performLogout())
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void performLogout() {
        FirebaseHelper.getInstance().getAuth().signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
