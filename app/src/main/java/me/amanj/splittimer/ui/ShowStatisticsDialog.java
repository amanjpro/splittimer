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
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


import me.amanj.splittimer.R;
import me.amanj.splittimer.util.Timestamp;

public class ShowStatisticsDialog extends DialogFragment {

    final static String TAG = ShowStatisticsDialog.class.getCanonicalName();
    private TextView fastestLap, slowestLap, averageLap;

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


    public static ShowStatisticsDialog newInstance(long fastest, long slowest, long average) {
        ShowStatisticsDialog dialog = new ShowStatisticsDialog();
        Bundle args = new Bundle();
        args.putLong("fastest", fastest);
        args.putLong("slowest", slowest);
        args.putLong("average", average);
        dialog.setArguments(args);
        return dialog;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_statistics, null);
        fastestLap = (TextView) view.findViewById(R.id.show_fastest_lap_view);
        slowestLap = (TextView) view.findViewById(R.id.show_slowest_lap_view);
        averageLap = (TextView) view.findViewById(R.id.show_average_lap_view);
        builder.setView(view).setPositiveButton(
                R.string.save_dialog_ok_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }
        ).setCancelable(false);

        builder.setTitle(R.string.statistics_dialog);
        AlertDialog dialog = builder.create();
        Bundle args = getArguments();
        setStatistics(args.getLong("fastest"), args.getLong("slowest"), args.getLong("average"));
        return dialog;
    }

    private void setStatistics(long fastest, long slowest, long average) {
        fastestLap.setText(Timestamp.timeStampToString(fastest));
        slowestLap.setText(Timestamp.timeStampToString(slowest));
        averageLap.setText(Timestamp.timeStampToString(average));
    }
}
