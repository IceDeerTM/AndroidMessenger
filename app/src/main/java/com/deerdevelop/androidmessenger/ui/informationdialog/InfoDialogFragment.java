package com.deerdevelop.androidmessenger.ui.informationdialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.deerdevelop.androidmessenger.R;

public class InfoDialogFragment extends DialogFragment {
    String textMessage;
    public InfoDialogFragment() {
        super();
    }

    public InfoDialogFragment(String textMessage)
    {
        super();
        this.textMessage = textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Уведомление:")
                .setMessage(textMessage)
                .setIcon(R.drawable.ic_warning)
                .setPositiveButton("Принято", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Закрываем окно
                        dialog.cancel();
                    }
                });
        return builder.create();
    }
}