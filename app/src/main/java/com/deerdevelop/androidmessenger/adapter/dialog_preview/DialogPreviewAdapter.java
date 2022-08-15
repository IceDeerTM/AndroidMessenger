package com.deerdevelop.androidmessenger.adapter.dialog_preview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.deerdevelop.androidmessenger.R;
import com.deerdevelop.androidmessenger.CallBackListener;
import com.deerdevelop.androidmessenger.model.DialogPreview;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DialogPreviewAdapter extends RecyclerView.Adapter<DialogPreviewAdapter.DialogItemViewHolder> {
    private ArrayList<DialogPreview> dialogPreviews;
    private static CallBackListener callBackListener;

    public DialogPreviewAdapter(ArrayList<DialogPreview> dialogPreviews, CallBackListener callBackListener) {
        this.dialogPreviews = dialogPreviews;
        this.callBackListener = callBackListener;
    }

    @NonNull
    @NotNull
    @Override
    public DialogItemViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_item, parent, false);
        return new DialogItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull DialogItemViewHolder holder, int position) {
        dialogPreviews.get(position).onBindViewHolder(holder);
    }

    public void updateDialogPreviewView(int positionDialogPreviewView) {
        //dialogPreviewViews.get(positionDialogPreviewView).updateInterface();
    }

    public int getIndexDialogPreviewView(DialogPreview dialogPreview) {
/*
        for (int i = 0; i < dialogPreviewViews.size(); i++) {
            if (dialogPreviewViews.get(i).compareWithDialogPreview(dialogPreview)) {
                return i;
            }
        }*/

        return -1;
    }
    @Override
    public int getItemCount() {
        return dialogPreviews.size();
    }

    public final class DialogItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView displayNameTextView, messageTextView, dateSendTextView;
        public ImageView photoImageView, onlineStatus;
        public String nickName;

        public DialogItemViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            displayNameTextView = itemView.findViewById(R.id.displayNameTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            dateSendTextView = itemView.findViewById(R.id.dateSendTextView);
            photoImageView = itemView.findViewById(R.id.photoImageView);
            onlineStatus = itemView.findViewById(R.id.onlineStatus);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {

                callBackListener.callBack(nickName);
            }
        }

    }
}