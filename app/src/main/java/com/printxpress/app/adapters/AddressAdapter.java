package com.printxpress.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.printxpress.app.databinding.ItemAddressBinding;
import com.printxpress.app.models.Address;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private final Context context;
    private final List<Address> addresses;
    private final OnAddressActionListener listener;

    public interface OnAddressActionListener {
        void onSetDefault(Address address);
        void onDelete(Address address);
    }

    public AddressAdapter(Context context, List<Address> addresses, OnAddressActionListener listener) {
        this.context = context;
        this.addresses = addresses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAddressBinding binding = ItemAddressBinding.inflate(LayoutInflater.from(context), parent, false);
        return new AddressViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        holder.bind(addresses.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        private final ItemAddressBinding binding;

        public AddressViewHolder(ItemAddressBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Address address, final OnAddressActionListener listener) {
            binding.txtLabel.setText(address.getLabel());
            binding.txtFullAddress.setText(address.getFullAddress());
            binding.txtCity.setText(address.getCity());

            // Show/hide default badge
            binding.txtDefaultBadge.setVisibility(address.getIsDefault() ? View.VISIBLE : View.GONE);
            // If already default, hide the "Set as Default" button
            binding.btnSetDefault.setVisibility(address.getIsDefault() ? View.GONE : View.VISIBLE);

            binding.btnSetDefault.setOnClickListener(v -> {
                if (listener != null) listener.onSetDefault(address);
            });

            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(address);
            });
        }
    }
}
