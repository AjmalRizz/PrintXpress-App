package com.printxpress.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.printxpress.app.R;
import com.printxpress.app.databinding.ItemOrderBinding;
import com.printxpress.app.models.Order;
import com.printxpress.app.utils.DateTimeHelper;
import com.printxpress.app.utils.PriceHelper;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final Context context;
    private final List<Order> orders;
    private final OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onViewDetailsClick(Order order);
    }

    public OrderAdapter(Context context, List<Order> orders, OnOrderClickListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(LayoutInflater.from(context), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orders.get(position), listener, context);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderBinding binding;

        public OrderViewHolder(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Order order, final OnOrderClickListener listener, Context context) {
            binding.txtOrderId.setText("Order #" + order.getOrderId().substring(0, Math.min(8, order.getOrderId().length())));
            binding.txtProductName.setText(order.getProductName());
            binding.txtQuantity.setText("Qty: " + order.getQuantity());
            binding.txtPrice.setText(PriceHelper.formatLKR(order.getTotalPrice()));
            binding.txtDate.setText(DateTimeHelper.getTimeAgo(order.getCreatedAt()));

            // Status badge with color-coded background
            String status = order.getStatus();
            binding.txtStatus.setText(status);
            int bgResId;
            switch (status) {
                case "Processing": bgResId = R.drawable.bg_status_processing; break;
                case "Printing":   bgResId = R.drawable.bg_status_printing;   break;
                case "Ready":      bgResId = R.drawable.bg_status_ready;      break;
                case "Completed":  bgResId = R.drawable.bg_status_completed;  break;
                case "Cancelled":  bgResId = R.drawable.bg_status_cancelled;  break;
                default:           bgResId = R.drawable.bg_status_pending;    break;
            }
            binding.txtStatus.setBackground(ContextCompat.getDrawable(context, bgResId));

            binding.btnViewDetails.setOnClickListener(v -> {
                if (listener != null) listener.onViewDetailsClick(order);
            });
        }
    }
}
