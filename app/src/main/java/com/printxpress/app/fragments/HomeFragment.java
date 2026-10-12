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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.printxpress.app.ProductDetailActivity;
import com.printxpress.app.ProductListActivity;
import com.printxpress.app.R;
import com.printxpress.app.adapters.CategoryAdapter;
import com.printxpress.app.adapters.ProductAdapter;
import com.printxpress.app.databinding.FragmentHomeBinding;
import com.printxpress.app.models.Product;
import com.printxpress.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private List<Product> productList;
    private ProductAdapter productAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupBanners();
        setupCategoriesRecyclerView();
        setupProductsRecyclerView();
        loadProductsFromFirestore();
        loadUserDetails();

        binding.btnSearch.setOnClickListener(v -> {
            enableSearch();
        });
    }

    private void loadUserDetails() {
        String uid = FirebaseHelper.getInstance().getCurrentUserId();
        if (uid == null) return;

        FirebaseHelper.getInstance().getFirestore()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        if (name != null && !name.isEmpty()) {
                            binding.txtWelcomeUser.setText("Welcome, " + name + "!");
                            binding.txtWelcomeUser.setTextColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.accent));
                        }
                        
                        String imageUrl = documentSnapshot.getString("profileImageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(HomeFragment.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_person)
                                    .into(binding.imgProfileHome);
                        }
                    }
                });
    }

    private void enableSearch() {
        if (binding.edtSearch.getVisibility() == View.VISIBLE) {
            binding.edtSearch.setVisibility(View.GONE);
            binding.txtHeaderTitle.setVisibility(View.VISIBLE);
            binding.txtWelcomeUser.setVisibility(View.VISIBLE);
            binding.edtSearch.setText("");
        } else {
            binding.edtSearch.setVisibility(View.VISIBLE);
            binding.txtHeaderTitle.setVisibility(View.GONE);
            binding.txtWelcomeUser.setVisibility(View.GONE);
            binding.edtSearch.requestFocus();
        }

        binding.edtSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                productAdapter.filter(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void setupBanners() {
        // Load specific images for Vesak, New Year, and Bulk Printing
        Glide.with(this)
                .load("https://images.unsplash.com/photo-1574169208507-84376144848b?q=80&w=800") // New Year Special (Festive/Gold)
                .placeholder(R.drawable.placeholder_banner)
                .into(binding.imgBanner1);

        Glide.with(this)
                .load("https://images.unsplash.com/photo-1543157145-f78c636d023d?q=80&w=800") // Vesak Festival (Lanterns/Buddhist theme)
                .placeholder(R.drawable.placeholder_banner)
                .into(binding.imgBanner2);

        Glide.with(this)
                .load("https://images.unsplash.com/photo-1563986768609-322da13575f3?q=80&w=800") // Bulk Printing (Industrial/Machines)
                .placeholder(R.drawable.placeholder_banner)
                .into(binding.imgBanner3);
    }

    private void setupCategoriesRecyclerView() {
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        
        List<CategoryAdapter.CategoryItem> categories = new ArrayList<>();
        categories.add(new CategoryAdapter.CategoryItem("Business Cards", R.drawable.ic_category_businesscard));
        categories.add(new CategoryAdapter.CategoryItem("Posters", R.drawable.ic_category_poster));
        categories.add(new CategoryAdapter.CategoryItem("Banners", R.drawable.ic_category_banner));
        categories.add(new CategoryAdapter.CategoryItem("Flyers", R.drawable.ic_category_flyer));
        categories.add(new CategoryAdapter.CategoryItem("Stickers", R.drawable.ic_category_sticker));
        categories.add(new CategoryAdapter.CategoryItem("Mugs", R.drawable.ic_category_mug));
        categories.add(new CategoryAdapter.CategoryItem("T-Shirts", R.drawable.ic_category_tshirt));

        CategoryAdapter categoryAdapter = new CategoryAdapter(categories, categoryName -> {
            Intent intent = new Intent(getActivity(), ProductListActivity.class);
            intent.putExtra("category_name", categoryName);
            startActivity(intent);
        });
        binding.rvCategories.setAdapter(categoryAdapter);
    }

    private void setupProductsRecyclerView() {
        binding.rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(getContext(), productList, product -> {
            Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.getProductId());
            startActivity(intent);
        });
        binding.rvProducts.setAdapter(productAdapter);
    }

    private void loadProductsFromFirestore() {
        binding.progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseHelper.getInstance().getFirestore();
        db.collection("products")
                .whereEqualTo("isAvailable", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.progressBar.setVisibility(View.GONE);
                    productList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);
                        productList.add(product);
                    }
                    productAdapter.updateList(productList);
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error loading products", e);
                    Toast.makeText(getContext(), "Failed to load products: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
