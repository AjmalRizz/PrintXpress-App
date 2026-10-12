package com.printxpress.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.printxpress.app.R;
import com.printxpress.app.databinding.ItemOrderBinding;
import com.printxpress.app.databinding.ItemSupportMessageBinding;
import com.printxpress.app.models.Order;
import com.printxpress.app.models.SupportMessage;
import com.printxpress.app.utils.DateTimeHelper;
import com.printxpress.app.utils.PriceHelper;

import java.util.List;

public class AdminUnifiedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ORDER = 1;
    private static final int TYPE_MESSAGE = 2;

    private final Context context;
    private final List<Object> items;
    private final OnAdminItemClickListener listener;

    public interface OnAdminItemClickListener {
        void onOrderClick(Order order);
    }

    public AdminUnifiedAdapter(Context context, List<Object> items, OnAdminItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof Order) {
            return TYPE_ORDER;
        } else {
            return TYPE_MESSAGE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ORDER) {
            ItemOrderBinding binding = ItemOrderBinding.inflate(LayoutInflater.from(context), parent, false);
            return new OrderViewHolder(binding);
        } else {
            ItemSupportMessageBinding binding = ItemSupportMessageBinding.inflate(LayoutInflater.from(context), parent, false);
            return new MessageViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_ORDER) {
            ((OrderViewHolder) holder).bind((Order) items.get(position), listener, context);
        } else {
            ((MessageViewHolder) holder).bind((SupportMessage) items.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderBinding binding;

        public OrderViewHolder(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Order order, final OnAdminItemClickListener listener, Context context) {
            binding.txtOrderId.setText("Order #" + order.getOrderId().substring(0, Math.min(8, order.getOrderId().length())));
            binding.txtProductName.setText(order.getProductName());
            binding.txtQuantity.setText("Qty: " + order.getQuantity());
            binding.txtPrice.setText(PriceHelper.formatLKR(order.getTotalPrice()));
            binding.txtDate.setText(DateTimeHelper.getTimeAgo(order.getCreatedAt()));

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
                if (listener != null) listener.onOrderClick(order);
            });
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemSupportMessageBinding binding;

        public MessageViewHolder(ItemSupportMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SupportMessage message) {
            binding.txtUserEmail.setText(message.getUserEmail());
            binding.txtMessageBody.setText(message.getMessage());
            binding.txtMessageDate.setText(DateTimeHelper.getTimeAgo(message.getCreatedAt()));
        }
    }
}
