package com.printxpress.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.printxpress.app.adapters.ProductAdapter;
import com.printxpress.app.databinding.ActivityProductListBinding;
import com.printxpress.app.models.Product;
import com.printxpress.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends AppCompatActivity {

    private ActivityProductListBinding binding;
    private String categoryName;
    private List<Product> products;
    private ProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        categoryName = getIntent().getStringExtra("category_name");
        if (categoryName == null) {
            Toast.makeText(this, "Category not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.txtTitle.setText(categoryName);
        binding.btnBack.setOnClickListener(v -> finish());

        setupRecyclerView();
        loadFilteredProducts();
    }

    private void setupRecyclerView() {
        binding.rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        products = new ArrayList<>();
        adapter = new ProductAdapter(this, products, product -> {
            Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getProductId());
            startActivity(intent);
        });
        binding.rvProducts.setAdapter(adapter);
    }

    private void loadFilteredProducts() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.txtEmptyState.setVisibility(View.GONE);

        FirebaseFirestore db = FirebaseHelper.getInstance().getFirestore();
        db.collection("products")
                .whereEqualTo("category", categoryName)
                .whereEqualTo("isAvailable", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.progressBar.setVisibility(View.GONE);
                    products.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);
                        products.add(product);
                    }
                    adapter.notifyDataSetChanged();

                    if (products.isEmpty()) {
                        binding.txtEmptyState.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProductListActivity.this, "Failed to load products: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
