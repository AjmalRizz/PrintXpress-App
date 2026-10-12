package com.printxpress.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.printxpress.app.R;
import com.printxpress.app.databinding.ItemNotificationBinding;
import com.printxpress.app.models.Notification;
import com.printxpress.app.utils.DateTimeHelper;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final Context context;
    private final List<Notification> notifications;

    public NotificationAdapter(Context context, List<Notification> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(LayoutInflater.from(context), parent, false);
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(notifications.get(position), context);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationBinding binding;

        public NotificationViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Notification notification, Context context) {
            binding.txtTitle.setText(notification.getTitle());
            binding.txtMessage.setText(notification.getMessage());
            binding.txtTime.setText(DateTimeHelper.getTimeAgo(notification.getCreatedAt()));

            // Show blue unread dot; hide if already read
            if (!notification.getIsRead()) {
                binding.viewDot.setVisibility(android.view.View.VISIBLE);
                binding.viewDot.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_status_processing));
                binding.txtTitle.setTextColor(ContextCompat.getColor(context, R.color.primary));
            } else {
                binding.viewDot.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_status_completed));
                binding.viewDot.setVisibility(android.view.View.VISIBLE);
                binding.txtTitle.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            }
        }
    }
}
