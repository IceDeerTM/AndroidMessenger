package com.deerdevelop.androidmessenger.adapter.active_dialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.deerdevelop.androidmessenger.R;
import com.deerdevelop.androidmessenger.model.MessageAdapterType;
import com.deerdevelop.androidmessenger.model.MessageInterlocutor;
import com.deerdevelop.androidmessenger.ui.chat.ChatFragment;
import com.deerdevelop.androidmessenger.ui.informationdialog.Toaster;

import java.util.Objects;

public class ViewHolderFactory {
    public static class UserMessageViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        public TextView textViewMessage;
        public TextView textViewDate;
        public String messageId;


        public UserMessageViewHolder(View itemView) {
            super(itemView);
            textViewMessage = (TextView) itemView.findViewById(R.id.textViewMessageUser);
            textViewDate = (TextView) itemView.findViewById(R.id.textViewDateUser);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                MessageAdapter.callBackLongClickListener.onLongClickMessage(messageId);
            }
            return false;
        }
    }

    public static class InterlocutorMessageViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        public TextView textViewMessage;
        public TextView textViewDate;
        public String messageId;

        public InterlocutorMessageViewHolder(View itemView) {
            super(itemView);
            textViewMessage = (TextView) itemView.findViewById(R.id.textViewMessageInter);
            textViewDate = (TextView) itemView.findViewById(R.id.textViewDateInter);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {

                MessageAdapter.callBackLongClickListener.onLongClickMessage(messageId);
            }
            return false;
        }
    }

    public static RecyclerView.ViewHolder create(ViewGroup parent, int viewType) {

        switch (viewType) {
            case MessageAdapterType.MESSAGE_USER:
                View userMessageTypeView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_message_item, parent, false);
                return new ViewHolderFactory.UserMessageViewHolder(userMessageTypeView);

            case MessageAdapterType.MESSAGE_INTERLOCUTOR:
                View interlocutorMessageTypeView = LayoutInflater.from(parent.getContext()).inflate(R.layout.interlocutor_message_item, parent, false);
                return new ViewHolderFactory.InterlocutorMessageViewHolder(interlocutorMessageTypeView);

            default:
                return null;
        }
    }
}