package com.printxpress.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.printxpress.app.R;
import com.printxpress.app.databinding.ItemProductBinding;
import com.printxpress.app.models.Product;
import com.printxpress.app.utils.PriceHelper;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final Context context;
    private final List<Product> products;
    private List<Product> productsFull;
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> products, OnProductClickListener listener) {
        this.context = context;
        this.products = products;
        this.productsFull = new ArrayList<>(products);
        this.listener = listener;
    }

    public void updateList(List<Product> newList) {
        this.productsFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        products.clear();
        if (text.isEmpty()) {
            products.addAll(productsFull);
        } else {
            text = text.toLowerCase();
            for (Product item : productsFull) {
                if (item.getName().toLowerCase().contains(text) || 
                    item.getCategory().toLowerCase().contains(text)) {
                    products.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product, listener);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductBinding binding;

        public ProductViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Product product, final OnProductClickListener listener) {
            binding.txtProductName.setText(product.getName());
            binding.txtProductPrice.setText(PriceHelper.formatLKR(product.getBasePrice()));

            // Determine a relevant placeholder based on category
            String placeholderUrl = "https://images.unsplash.com/photo-1517646287270-a5a9ca602e5c?q=80&w=400"; // Default
            if (product.getCategory() != null) {
                switch (product.getCategory()) {
                    case "Business Cards":
                        placeholderUrl = "https://images.unsplash.com/photo-1593032465175-481ac7f401a0?q=80&w=400";
                        break;
                    case "Posters":
                        placeholderUrl = "https://images.unsplash.com/photo-1583939411023-14783179e581?q=80&w=400";
                        break;
                    case "Banners":
                        placeholderUrl = "https://images.unsplash.com/photo-1533228100845-08145b01de14?q=80&w=400";
                        break;
                    case "Flyers":
                        placeholderUrl = "https://images.unsplash.com/photo-1562654501-a0ccc0fc3fb1?q=80&w=400";
                        break;
                    case "Stickers":
                        placeholderUrl = "https://images.unsplash.com/photo-1572375992501-4b0892d50c69?q=80&w=400";
                        break;
                    case "Mugs":
                        placeholderUrl = "https://images.unsplash.com/photo-1514228742587-6b1558fcca3d?q=80&w=400";
                        break;
                    case "T-Shirts":
                        placeholderUrl = "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?q=80&w=400";
                        break;
                }
            }

            // Load product image using Glide with high-quality category placeholder fallback
            String imageUrl = (product.getImageUrl() != null && !product.getImageUrl().trim().isEmpty()) 
                    ? product.getImageUrl() : placeholderUrl;

            Glide.with(itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .centerCrop()
                    .into(binding.imgProduct);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
    }
}
