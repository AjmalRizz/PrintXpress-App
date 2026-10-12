package com.printxpress.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.printxpress.app.databinding.ActivityProductDetailBinding;
import com.printxpress.app.models.Product;
import com.printxpress.app.models.ProductVariant;
import com.printxpress.app.utils.FirebaseHelper;
import com.printxpress.app.utils.PriceHelper;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {

    private ActivityProductDetailBinding binding;
    private String productId;
    private Product product;
    private List<ProductVariant> variants;
    
    private List<String> sizes;
    private List<String> materials;
    private ArrayAdapter<String> sizeAdapter;
    private ArrayAdapter<String> materialAdapter;

    private int quantity = 1;
    private double currentTotalPrice = 0.0;
    private ProductVariant selectedVariant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        productId = getIntent().getStringExtra("product_id");
        if (productId == null) {
            Toast.makeText(this, "Product not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnPlus.setOnClickListener(v -> adjustQuantity(1));
        binding.btnMinus.setOnClickListener(v -> adjustQuantity(-1));
        binding.btnPlaceOrder.setOnClickListener(v -> navigateToOrderForm());

        sizes = new ArrayList<>();
        materials = new ArrayList<>();
        variants = new ArrayList<>();

        setupSpinnerAdapters();
        loadProductDetails();
    }

    private void setupSpinnerAdapters() {
        sizeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sizes);
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSize.setAdapter(sizeAdapter);

        materialAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, materials);
        materialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerMaterial.setAdapter(materialAdapter);

        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSelectedVariantAndPrice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        };

        binding.spinnerSize.setOnItemSelectedListener(spinnerListener);
        binding.spinnerMaterial.setOnItemSelectedListener(spinnerListener);
    }

    private void loadProductDetails() {
        showLoading(true);
        FirebaseFirestore db = FirebaseHelper.getInstance().getFirestore();
        
        db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        product = documentSnapshot.toObject(Product.class);
                        if (product != null) {
                            renderProductInfo();
                            loadProductVariants();
                        }
                    } else {
                        showLoading(false);
                        Toast.makeText(ProductDetailActivity.this, "Product does not exist.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(ProductDetailActivity.this, "Failed to load product details: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void renderProductInfo() {
        binding.txtProductName.setText(product.getName());
        binding.txtCategoryBadge.setText(product.getCategory());
        binding.txtBasePrice.setText(PriceHelper.formatLKR(product.getBasePrice()));
        binding.txtDescription.setText(product.getDescription());

        // Smart Category-based placeholder logic
        String placeholderUrl = "https://images.unsplash.com/photo-1517646287270-a5a9ca602e5c?q=80&w=800"; // Default
        if (product.getCategory() != null) {
            switch (product.getCategory()) {
                case "Business Cards":
                    placeholderUrl = "https://images.unsplash.com/photo-1593032465175-481ac7f401a0?q=80&w=800";
                    break;
                case "Posters":
                    placeholderUrl = "https://images.unsplash.com/photo-1583939411023-14783179e581?q=80&w=800";
                    break;
                case "Banners":
                    placeholderUrl = "https://images.unsplash.com/photo-1533228100845-08145b01de14?q=80&w=800";
                    break;
                case "Flyers":
                    placeholderUrl = "https://images.unsplash.com/photo-1562654501-a0ccc0fc3fb1?q=80&w=800";
                    break;
                case "Stickers":
                    placeholderUrl = "https://images.unsplash.com/photo-1572375992501-4b0892d50c69?q=80&w=800";
                    break;
                case "Mugs":
                    placeholderUrl = "https://images.unsplash.com/photo-1514228742587-6b1558fcca3d?q=80&w=800";
                    break;
                case "T-Shirts":
                    placeholderUrl = "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?q=80&w=800";
                    break;
            }
        }

        String imageUrl = (product.getImageUrl() != null && !product.getImageUrl().trim().isEmpty()) 
                ? product.getImageUrl() : placeholderUrl;

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_product)
                .error(R.drawable.placeholder_product)
                .centerCrop()
                .into(binding.imgProduct);
    }

    private void loadProductVariants() {
        FirebaseFirestore db = FirebaseHelper.getInstance().getFirestore();
        db.collection("productVariants")
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    showLoading(false);
                    variants.clear();
                    sizes.clear();
                    materials.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        ProductVariant variant = doc.toObject(ProductVariant.class);
                        if (variant != null) {
                            variants.add(variant);
                            if (!sizes.contains(variant.getSize())) {
                                sizes.add(variant.getSize());
                            }
                            if (!materials.contains(variant.getMaterial())) {
                                materials.add(variant.getMaterial());
                            }
                        }
                    }

                    sizeAdapter.notifyDataSetChanged();
                    materialAdapter.notifyDataSetChanged();

                    // Trigger initial price calculation
                    updateSelectedVariantAndPrice();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(ProductDetailActivity.this, "Failed to load product variants: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void adjustQuantity(int delta) {
        int newQty = quantity + delta;
        if (newQty >= 1 && newQty <= 500) {
            quantity = newQty;
            binding.txtQuantity.setText(String.valueOf(quantity));
            calculateTotalPrice();
        }
    }

    private void updateSelectedVariantAndPrice() {
        if (variants.isEmpty() || binding.spinnerSize.getSelectedItem() == null || binding.spinnerMaterial.getSelectedItem() == null) {
            return;
        }

        String selectedSize = binding.spinnerSize.getSelectedItem().toString();
        String selectedMaterial = binding.spinnerMaterial.getSelectedItem().toString();

        selectedVariant = null;
        for (ProductVariant v : variants) {
            if (v.getSize().equals(selectedSize) && v.getMaterial().equals(selectedMaterial)) {
                selectedVariant = v;
                break;
            }
        }

        // If exact combination not found, default to first variant
        if (selectedVariant == null && !variants.isEmpty()) {
            selectedVariant = variants.get(0);
        }

        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        if (product == null) return;

        double base = product.getBasePrice();
        double modifier = (selectedVariant != null) ? selectedVariant.getPriceModifier() : 0.0;
        currentTotalPrice = PriceHelper.calculateTotal(base, modifier, quantity);

        binding.txtTotalCalculatedPrice.setText(PriceHelper.formatLKR(currentTotalPrice));
    }

    private void navigateToOrderForm() {
        if (product == null || selectedVariant == null) {
            Toast.makeText(this, "Specifications are still loading.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(ProductDetailActivity.this, OrderFormActivity.class);
        intent.putExtra("product_id", product.getProductId());
        intent.putExtra("product_name", product.getName());
        intent.putExtra("product_image_url", product.getImageUrl());
        intent.putExtra("variant_id", selectedVariant.getVariantId());
        intent.putExtra("selected_size", selectedVariant.getSize());
        intent.putExtra("selected_material", selectedVariant.getMaterial());
        intent.putExtra("quantity", quantity);
        intent.putExtra("total_price", currentTotalPrice);
        startActivity(intent);
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnPlaceOrder.setEnabled(!isLoading);
        binding.spinnerSize.setEnabled(!isLoading);
        binding.spinnerMaterial.setEnabled(!isLoading);
        binding.btnMinus.setEnabled(!isLoading);
        binding.btnPlus.setEnabled(!isLoading);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
