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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.DateFormat;
import java.util.Date;

import me.amanj.splittimer.R;
import me.amanj.splittimer.messages.Message;
import me.amanj.splittimer.messages.MessageTag;
import me.amanj.splittimer.messages.Send;
import me.amanj.splittimer.model.StaticTimestampsAdapter;
import me.amanj.splittimer.util.TimeInformation;
import me.amanj.splittimer.util.Timestamp;

/**
 * Created by Amanj Sherwany on 8/6/16.
 */

public class ShowTimeInformationFragment extends Fragment {
    private static EventBus bus = EventBus.getDefault();
    private TextView totalTimeDisplay, name;
    private RecyclerView recyclerView;
    private StaticTimestampsAdapter timestampsAdapter;
    final static String TAG = ShowTimeInformationFragment.class.getCanonicalName();

    // newInstance constructor for creating fragment with arguments
    public static ShowTimeInformationFragment newInstance() {
        ShowTimeInformationFragment fragmentFirst = new ShowTimeInformationFragment();
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
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("has_content", timestampsAdapter != null);
        outState.putSerializable("adapter_content", timestampsAdapter.getAdapterContent());
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        recyclerView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        if(savedInstanceState != null && savedInstanceState.getBoolean("has_content")) {
            timestampsAdapter.setTimeInformation(
                    (TimeInformation) savedInstanceState.getSerializable("adapter_content"));
            name.setText(getTitle());
            totalTimeDisplay.setText(
                    Timestamp.timeStampToString(timestampsAdapter.getLastElapsedTime()));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "Entering onCreateView for LapTimesInformationDisplayFragment");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_show_time_information, container, false);

        name = (TextView) view.findViewById(R.id.show_time_information_name);

        totalTimeDisplay =
                (TextView) view.findViewById(R.id.text_view_total_time_display);
        recyclerView = (RecyclerView) view.findViewById(R.id.laps_list_view);
        LinearLayoutManager llm = new LinearLayoutManager(this.getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);        timestampsAdapter =
                new StaticTimestampsAdapter(view.getContext().getApplicationContext());
        recyclerView.setAdapter(timestampsAdapter);
        recyclerView.setHasFixedSize(true);


        return view;

    }


    public String getTitle() {
        if(timestampsAdapter == null) return "";
        Date date = new Date(timestampsAdapter.getStoringTime());
        return timestampsAdapter.getName() + " - " + DateFormat.getInstance().format(date);
    }

    @Subscribe
    public void onEvent(Message event) {
        if(event.tag() == MessageTag.SHOW) {
            TimeInformation tinfo = ((Send<TimeInformation>) event).receive();
            timestampsAdapter.setTimeInformation(tinfo);
            totalTimeDisplay.setText(
                    Timestamp.timeStampToString(tinfo.getElapsedTime()));
            name.setText(getTitle());
        } else if(event.tag() == MessageTag.RENAMED) {
            String tinfo = ((Send<String>) event).receive();
            timestampsAdapter.setName(tinfo);
            name.setText(getTitle());
        }
    }
}
