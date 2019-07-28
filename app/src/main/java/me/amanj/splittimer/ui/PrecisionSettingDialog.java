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


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.greenrobot.eventbus.EventBus;

import me.amanj.splittimer.R;
import me.amanj.splittimer.messages.MessageTag;
import me.amanj.splittimer.messages.Send;
import me.amanj.splittimer.util.Configurations;
import me.amanj.splittimer.util.Timestamp;

public class PrecisionSettingDialog extends DialogFragment {

    final static String TAG = PrecisionSettingDialog.class.getCanonicalName();
    private RadioButton fine, less_fine, gross, more_gross;
    private static final EventBus bus = EventBus.getDefault();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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

        final View view = inflater.inflate(R.layout.precision_chooser_dialog, null);


        fine = (RadioButton) view.findViewById(R.id.fine_precision);
        less_fine = (RadioButton) view.findViewById(R.id.less_fine_precision);
        gross = (RadioButton) view.findViewById(R.id.gross_precision);
        more_gross = (RadioButton) view.findViewById(R.id.more_gross_precision);
        setPrecision(Configurations.getCurrentPrecisionLevel());

        builder.setView(view).setPositiveButton(
                R.string.save_dialog_ok_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (fine.isChecked())
                            Configurations.updateCurrentFormat(Configurations.PRECISION_LEVEL_FOUR);
                        else if (less_fine.isChecked())
                            Configurations.updateCurrentFormat(Configurations.PRECISION_LEVEL_THREE);
                        else if (gross.isChecked())
                            Configurations.updateCurrentFormat(Configurations.PRECISION_LEVEL_TWO);
                        else if (more_gross.isChecked())
                            Configurations.updateCurrentFormat(Configurations.PRECISION_LEVEL_ONE);
                        Timestamp.updateFormatter(Configurations.getCurrentFormat());
                        bus.post(new Send<Void>() {
                            public MessageTag tag() {
                                return MessageTag.UPDATE_DISPLAYS;
                            }

                            public Void receive() {
                                return null;
                            }
                        });
                    }
                }
        ).setNegativeButton(R.string.save_dialog_cancel_text,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PrecisionSettingDialog.this.getDialog().cancel();
                    }
                }
        );
        builder.setTitle(R.string.precision_setting_title);
        AlertDialog dialog = builder.create();

        return dialog;
    }


    private void setPrecision(int precision) {
        if(precision == Configurations.PRECISION_LEVEL_FOUR) fine.setChecked(true);
        else if(precision == Configurations.PRECISION_LEVEL_THREE) less_fine.setChecked(true);
        else if(precision == Configurations.PRECISION_LEVEL_TWO) gross.setChecked(true);
        else if(precision == Configurations.PRECISION_LEVEL_ONE) more_gross.setChecked(true);
    }

}
