package com.printxpress.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.Timestamp;
import com.printxpress.app.databinding.ActivityFaqBinding;
import com.printxpress.app.models.SupportMessage;
import com.printxpress.app.utils.FirebaseHelper;

public class FAQActivity extends AppCompatActivity {

    private ActivityFaqBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFaqBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnSendMessage.setOnClickListener(v -> handleSendMessage());
    }

    private void handleSendMessage() {
        if (binding.edtSupportMessage.getText() == null) return;
        
        String messageText = binding.edtSupportMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Please enter a message.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseHelper.getInstance().getCurrentUserId();
        if (uid == null) {
            Toast.makeText(this, "Please log in to send a message.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSendMessage.setEnabled(false);

        String email = "";
        if (FirebaseHelper.getInstance().getAuth().getCurrentUser() != null) {
            email = FirebaseHelper.getInstance().getAuth().getCurrentUser().getEmail();
        }

        String messageId = FirebaseHelper.getInstance().getFirestore().collection("support_messages").document().getId();

        SupportMessage supportMessage = new SupportMessage(
                messageId,
                uid,
                email,
                messageText,
                Timestamp.now()
        );

        FirebaseHelper.getInstance().getFirestore()
                .collection("support_messages")
                .document(messageId)
                .set(supportMessage)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSendMessage.setEnabled(true);
                    binding.edtSupportMessage.setText("");
                    Toast.makeText(this, "Message sent to Admin successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSendMessage.setEnabled(true);
                    Toast.makeText(this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
