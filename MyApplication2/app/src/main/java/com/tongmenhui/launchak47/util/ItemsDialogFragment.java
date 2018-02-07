package com.tongmenhui.launchak47.util;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

/**
 * Created by super-zou on 18-2-8.
 */

public class ItemsDialogFragment extends DialogFragment {

        private String title;

        private String[] items;

        private DialogInterface.OnClickListener onClickListener;

        public void show(String title, String[] items, DialogInterface.OnClickListener onClickListener,
                         FragmentManager fragmentManager) {
            this.title = title;
            this.items = items;
            this.onClickListener = onClickListener;
            show(fragmentManager, "ItemsDialogFragment");
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(title).setItems(items, onClickListener);
            return builder.create();
        }


}
