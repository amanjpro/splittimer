/*
 * Copyright (c) 2016, Amanj Sherwany <http://www.amanj.me>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list
 *     of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 *     of conditions and the following disclaimer in the documentation and/or other
 *     materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without specific prior
 *     written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.amanj.splittimer.ui;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import me.amanj.splittimer.R;

public class SaveDialog extends DialogFragment {

    private final static String TAG = SaveDialog.class.getCanonicalName();

    private ResultReceiver resultReceiver;

    public static SaveDialog newInstance(String name, ResultReceiver receiver) {
        SaveDialog dialog = newInstance(receiver);
        dialog.getArguments().putBoolean("isRename", true);
        dialog.getArguments().putString("name", name);
        return dialog;
    }
    public static SaveDialog newInstance(ResultReceiver receiver) {
        SaveDialog frag = new SaveDialog();
        Bundle args = new Bundle();
        args.putParcelable("receiver", receiver);
        args.putBoolean("isRename", false);
        frag.setArguments(args);
        return frag;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if(savedInstanceState == null) {
            resultReceiver = getArguments().getParcelable("receiver");
        } else {
            resultReceiver = savedInstanceState.getParcelable("receiver");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("receiver", resultReceiver);
    }


    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.fragment_save_dialog, null);
        final EditText editView = (EditText) view.findViewById(R.id.save_name_edit);

        if(getArguments() != null && getArguments().getBoolean("isRename")) {
            editView.setText(getArguments().getString("name"));
//            editView.selectAll();
            editView.setSelectAllOnFocus(true);
        }

        editView.setSingleLine();
        final InputMethodManager mgr = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        builder.setView(view).setPositiveButton(
            R.string.save_dialog_ok_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // do nothing
                }
            }
        ).setNegativeButton(R.string.save_dialog_cancel_text,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SaveDialog.this.getDialog().cancel();
                    resultReceiver.send(Activity.RESULT_CANCELED, null);
                    mgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            }
        );
        // only will trigger it if no physical keyboard is open
        mgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        AlertDialog dialog =  builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        editView.requestFocus();

        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String activityName = editView.getText().toString();
                if(activityName.length() > 0) {
                    Bundle bundle = new Bundle();
                    bundle.putString("name", activityName);
                    resultReceiver.send(Activity.RESULT_OK, bundle);
                    SaveDialog.this.getDialog().dismiss();
                    mgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),
                        R.string.error_message_no_name,
                        Toast.LENGTH_LONG).show();
                }
            }
        });
        return dialog;
    }



}
