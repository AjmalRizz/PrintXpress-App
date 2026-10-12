package com.printxpress.app.utils;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.printxpress.app.models.Product;
import com.printxpress.app.models.ProductVariant;
import java.util.ArrayList;
import java.util.List;

public class DatabaseSeeder {
    private static final String TAG = "DatabaseSeeder";

    public interface SeedCallback {
        void onSeedComplete(boolean success);
    }

    public static void seedProductsIfEmpty(SeedCallback callback) {
        FirebaseFirestore db = FirebaseHelper.getInstance().getFirestore();
        db.collection("products")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "Products collection is empty. Seeding data...");
                        performSeeding(db, callback);
                    } else {
                        Log.d(TAG, "Products already seeded.");
                        if (callback != null) {
                            callback.onSeedComplete(true);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking empty products collection", e);
                    if (callback != null) {
                        callback.onSeedComplete(false);
                    }
                });
    }

    private static void performSeeding(FirebaseFirestore db, SeedCallback callback) {
        WriteBatch batch = db.batch();

        List<Product> products = new ArrayList<>();
        List<ProductVariant> variants = new ArrayList<>();

        // Product 1: Business Card
        String p1 = "p_business_card";
        products.add(new Product(p1, "Business Cards", "Standard Business Card", "Matte finish premium business cards printed in bulk.", 2500.0, "", true));
        variants.add(new ProductVariant("v1_1", p1, "90x55mm", "Standard Matte 300gsm", 0.0));
        variants.add(new ProductVariant("v1_2", p1, "90x55mm", "Glossy Laminated 350gsm", 500.0));

        // Product 2: A3 Poster
        String p2 = "p_a3_poster";
        products.add(new Product(p2, "Posters", "A3 Promo Poster", "Vibrant colors on glossy photo quality paper.", 350.0, "", true));
        variants.add(new ProductVariant("v2_1", p2, "A3 (297x420mm)", "Glossy Photo Paper 150gsm", 0.0));
        variants.add(new ProductVariant("v2_2", p2, "A2 (420x594mm)", "Satin Art Paper 200gsm", 300.0));

        // Product 3: Vinyl Banner
        String p3 = "p_vinyl_banner";
        products.add(new Product(p3, "Banners", "Outdoor Vinyl Banner", "Heavy duty flex banners perfect for outdoor advertising.", 4500.0, "", true));
        variants.add(new ProductVariant("v3_1", p3, "3ft x 6ft", "Standard Flex 340gsm", 0.0));
        variants.add(new ProductVariant("v3_2", p3, "4ft x 8ft", "Premium Blockout 440gsm", 2500.0));

        // Product 4: A5 Flyer
        String p4 = "p_a5_flyer";
        products.add(new Product(p4, "Flyers", "A5 Marketing Flyer", "Double-sided full color flyers to promote your business.", 1800.0, "", true));
        variants.add(new ProductVariant("v4_1", p4, "A5 (148x210mm)", "Art Paper 128gsm", 0.0));
        variants.add(new ProductVariant("v4_2", p4, "A4 (210x297mm)", "Art Paper 150gsm", 1000.0));

        // Product 5: Circle Sticker
        String p5 = "p_circle_sticker";
        products.add(new Product(p5, "Stickers", "Custom Circle Sticker", "Easy to peel custom stickers for product labeling.", 1200.0, "", true));
        variants.add(new ProductVariant("v5_1", p5, "5cm Diameter", "Paper Sticker", 0.0));
        variants.add(new ProductVariant("v5_2", p5, "5cm Diameter", "Waterproof Vinyl Sticker", 800.0));

        // Product 6: Ceramic Mug
        String p6 = "p_ceramic_mug";
        products.add(new Product(p6, "Mugs", "White Ceramic Mug", "High quality sublimation printed white mugs, dishwasher safe.", 1500.0, "", true));
        variants.add(new ProductVariant("v6_1", p6, "11oz Standard", "White Ceramic", 0.0));
        variants.add(new ProductVariant("v6_2", p6, "11oz Magic Mug", "Heat-Sensitive Color Changing", 600.0));

        // Product 7: Cotton T-Shirt
        String p7 = "p_cotton_tshirt";
        products.add(new Product(p7, "T-Shirts", "Custom Printed T-Shirt", "100% Cotton round neck t-shirt with DTF custom design printing.", 2200.0, "", true));
        variants.add(new ProductVariant("v7_1", p7, "Medium (M)", "Cotton 160gsm", 0.0));
        variants.add(new ProductVariant("v7_2", p7, "Large (L)", "Premium Cotton 190gsm", 300.0));

        // Add products to batch
        for (Product product : products) {
            batch.set(db.collection("products").document(product.getProductId()), product);
        }

        // Add variants to batch
        for (ProductVariant variant : variants) {
            batch.set(db.collection("productVariants").document(variant.getVariantId()), variant);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Database seeded successfully.");
                    if (callback != null) {
                        callback.onSeedComplete(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error seeding database", e);
                    if (callback != null) {
                        callback.onSeedComplete(false);
                    }
                });
    }
}
