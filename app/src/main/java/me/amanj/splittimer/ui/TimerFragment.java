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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import me.amanj.splittimer.R;
import me.amanj.splittimer.util.TimeRunner;
import me.amanj.splittimer.messages.Message;
import me.amanj.splittimer.messages.MessageTag;
import me.amanj.splittimer.messages.Send;
import me.amanj.splittimer.util.CSVSupport;
import org.greenrobot.eventbus.*;


import me.amanj.splittimer.util.Configurations;
import me.amanj.splittimer.util.TimeInformation;
import me.amanj.splittimer.util.Timestamp;
import me.amanj.splittimer.model.InteractiveTimestampsAdapter;



public class TimerFragment extends Fragment {

    private static EventBus bus = EventBus.getDefault();
    private final static String TAG = TimerFragment.class.getCanonicalName();
    private static int HALF_OPAQUE = 120;
    private static int FULL_OPAQUE = 255;
    private TimeRunner runner;
    private InteractiveTimestampsAdapter timestampsAdapter;
    private TextView currentTimeView, totalTimeDisplay;
    private boolean isRunning;
    private Timestamp tstamp;
    private RecyclerView listView;
    private ImageButton saveButton, lapButton, actionButton;

    // newInstance constructor for creating fragment with arguments
    public static TimerFragment newInstance() {
        TimerFragment fragmentFirst = new TimerFragment();
        Bundle args = new Bundle();
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    public void onPause() {
        bus.unregister(this);
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "Entering onCreateView for TimerFragment");
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_timer, container, false);
        isRunning = false;

        currentTimeView = (TextView) view.findViewById(R.id.current_time_view);
        totalTimeDisplay =
                (TextView) view.findViewById(R.id.text_view_total_time_display);
        actionButton =
                (ImageButton) view.findViewById(R.id.start_stop_toggle_button);
        lapButton = (ImageButton) view.findViewById(R.id.lap_button);
        saveButton = (ImageButton) view.findViewById((R.id.save_button));
        tstamp = new Timestamp();

        LinearLayoutManager llm = new LinearLayoutManager(this.getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        listView = (RecyclerView) view.findViewById(R.id.laps_list_view);
        timestampsAdapter =
                new InteractiveTimestampsAdapter(view.getContext().getApplicationContext());
        listView.setLayoutManager(llm);
        listView.setAdapter(timestampsAdapter);
        listView.setHasFixedSize(true);
        currentTimeView.setText(Timestamp.timeStampToString(0l));
        totalTimeDisplay.setText(Timestamp.timeStampToString(0l));

        saveButton.getBackground().setAlpha(HALF_OPAQUE);
        lapButton.getBackground().setAlpha(HALF_OPAQUE);

        ViewCompat.setActivated(saveButton, false);
        saveButton.setEnabled(false);
        saveButton.setClickable(false);
        lapButton.setEnabled(false);
        lapButton.setClickable(false);
        ViewCompat.setActivated(lapButton, false);

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isRunning) {
                    timestampsAdapter.clear();
                    runner = new TimeRunner(currentTimeView, totalTimeDisplay);
                    actionButton.setBackgroundResource(R.drawable.ic_action_stop);
                    saveButton.getBackground().setAlpha(HALF_OPAQUE);
                    lapButton.getBackground().setAlpha(FULL_OPAQUE);
                    ViewCompat.setActivated(lapButton, false);
                    lapButton.setEnabled(true);
                    lapButton.setClickable(true);
                    tstamp.start();
                    runner.execute(tstamp);
                } else {
                    actionButton.setBackgroundResource(R.drawable.ic_action_play);
                    saveButton.getBackground().setAlpha(FULL_OPAQUE);
                    long lastLap = tstamp.stop();
                    runner.cancel(true);
                    if(Configurations.shouldLapOnStop()) {
                        lapButton.getBackground().setAlpha(HALF_OPAQUE);
                        timestampsAdapter.add(lastLap);
                        ViewCompat.setActivated(lapButton, false);
                        lapButton.setEnabled(false);
                        lapButton.setClickable(false);
                        totalTimeDisplay.setText(
                                 Timestamp.timeStampToString(tstamp.getElapsedTime()));
                    }
                    currentTimeView.setText(Timestamp.timeStampToString(0l));
                }
                isRunning = !isRunning;
                ViewCompat.setActivated(saveButton, !isRunning);
                saveButton.setEnabled(!isRunning);
                saveButton.setClickable(!isRunning);
            }
        });


        lapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lap();
            }
        });




        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResultReceiver res = new ResultReceiver(null) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode == Activity.RESULT_OK) {
                            final TimeInformation tinfo = timestampsAdapter.getAdapterContent();
                            String result = resultData.getString("name");
                            tinfo.setName(result);
                            tinfo.setStoringTime(System.currentTimeMillis());
                            bus.post(new Send<TimeInformation>() {
                                public MessageTag tag() {
                                    return MessageTag.SAVE;
                                }
                                public TimeInformation receive() { return tinfo; }

                            });
                            Log.i(TAG, CSVSupport.toCSV(tinfo));
                        }
                    }
                };
                SaveDialog newFragment = SaveDialog.newInstance(res);
                newFragment.show(getActivity().getSupportFragmentManager(),
                        SaveDialog.TAG);
            }
        });


        ImageView overflowView = (ImageView) view.findViewById(R.id.overflow_show_statistics);
        overflowView.setVisibility(View.VISIBLE);
        overflowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(view.getContext(), view);
                popup.inflate(R.menu.time_fragment_list_menu);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.show_statics_menu_item:
                                ShowStatisticsDialog newFragment = ShowStatisticsDialog.newInstance(
                                        timestampsAdapter.getFastestLap(),
                                        timestampsAdapter.getSlowestLap(),
                                        timestampsAdapter.getAverageLap());
                                newFragment.show(getActivity().getSupportFragmentManager(),
                                    ShowStatisticsDialog.TAG);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popup.show();
            }
        });


        overflowView.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;


        return view;
    }


    // Process clicks on Context Menu Items
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.i(TAG, "ITEM SELECTED");
        AdapterView.AdapterContextMenuInfo menuInfo;
        switch (item.getItemId()) {
            case R.id.merge_with_above:
                menuInfo =
                        (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                timestampsAdapter.mergeWithAbove(menuInfo.position);
                return true;
            case R.id.merge_with_below:
                menuInfo =
                        (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                timestampsAdapter.mergeWithBelow(menuInfo.position);
                return true;
            default:
                return false;
        }
    }


    @Subscribe
    public void onEvent(Message event) {
        if(event.tag() == MessageTag.UPDATE_DISPLAYS && !isRunning) {
            long currentTime = timestampsAdapter.getItemCount() == 0? 0l :
                            timestampsAdapter.getLastLapTime();
            long elapsedTime = timestampsAdapter.getItemCount() == 0? 0l :
                    timestampsAdapter.getLastElapsedTime();
            currentTimeView.setText(
                            Timestamp.timeStampToString(currentTime));
            totalTimeDisplay.setText(
                    Timestamp.timeStampToString(elapsedTime));
        } else if(event.tag() == MessageTag.LAP && lapButton.isEnabled()) {
            lap();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isRunning", isRunning);
        outState.putBoolean("saveState", saveButton.isEnabled());
        outState.putBoolean("lapState", lapButton.isEnabled());
        outState.putSerializable("timestamps", timestampsAdapter.getAdapterContent());
        outState.putCharSequence("totalDisplay", totalTimeDisplay.getText());
        if(isRunning) {
            runner.cancel(true);
            outState.putSerializable("timestamp", tstamp);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        setRetainInstance(true);
        Log.d(TAG, "Entering onActivityCreated");
        if(savedInstanceState != null) {
            isRunning = savedInstanceState.getBoolean("isRunning");
            if(isRunning) {
                tstamp = (Timestamp) savedInstanceState.getSerializable("timestamp");
                runner = new TimeRunner(currentTimeView, totalTimeDisplay);
                runner.execute(tstamp);
            }
            timestampsAdapter.setTimeInformation(
                    (TimeInformation)savedInstanceState.getSerializable("timestamps"));
            boolean lapState = savedInstanceState.getBoolean("lapState");
            boolean saveState = savedInstanceState.getBoolean("saveState");

            ViewCompat.setActivated(lapButton, lapState);
            lapButton.setEnabled(lapState);
            lapButton.setClickable(lapState);
            if(lapState) {
                lapButton.getBackground().setAlpha(FULL_OPAQUE);
            } else {
                lapButton.getBackground().setAlpha(HALF_OPAQUE);
            }

            ViewCompat.setActivated(saveButton, saveState);
            saveButton.setEnabled(saveState);
            saveButton.setClickable(saveState);
            if(saveState) {
                saveButton.getBackground().setAlpha(FULL_OPAQUE);
            } else {
                saveButton.getBackground().setAlpha(HALF_OPAQUE);
            }

            if(isRunning) {
                actionButton.setBackgroundResource(R.drawable.ic_action_stop);
            } else {
                actionButton.setBackgroundResource(R.drawable.ic_action_play);
                totalTimeDisplay.setText(savedInstanceState.getCharSequence("totalDisplay"));
            }
        }
    }


    private void lap() {
        timestampsAdapter.add(tstamp.lap());
        if(!isRunning) {
            totalTimeDisplay.setText(
                    Timestamp.timeStampToString(tstamp.getElapsedTime()));
            lapButton.getBackground().setAlpha(HALF_OPAQUE);
            ViewCompat.setActivated(lapButton, false);
            lapButton.setEnabled(false);
            lapButton.setClickable(false);
        }
    }

}
