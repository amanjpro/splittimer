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

package me.amanj.splittimer.model;

/**
 * Created by Amanj Sherwany on 8/4/16.
 */



import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Handler;


import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.amanj.splittimer.R;
import me.amanj.splittimer.messages.MessageTag;
import me.amanj.splittimer.messages.Send;
import me.amanj.splittimer.ui.SaveDialog;
import me.amanj.splittimer.ui.SplitTimerListFragment;


public class SplitTimerListAdapter extends RecyclerView.Adapter<SplitTimerListAdapter.ViewHolder> {

    private static EventBus bus = EventBus.getDefault();
    private final List<TimeInformation> mItems = new ArrayList<>();
    private final List<TimeInformation> itemsPendingRemoval = new ArrayList<>();
    private static final int PENDING_REMOVAL_TIMEOUT = 5000; // 3sec
    private Handler handler = new Handler(); // hanlder for running delayed runnables
    // map of items to pending runnables, so we can cancel a removal if need be
    private Map<TimeInformation, Runnable> pendingRunnables = new HashMap<>();
    private int lastOpened = -1;


    public int getLastOpened() { return lastOpened; }
    public void setLastOpened(int lastOpened) { this.lastOpened = lastOpened; }

    private final Context mContext;
    private final static String TAG = SplitTimerListAdapter.class.getCanonicalName();

    public SplitTimerListAdapter(Context context) {
        mContext = context;
    }

    public void addAll(List<TimeInformation> mItems) {
        this.mItems.addAll(mItems);
        notifyDataSetChanged();
    }

    public void add(TimeInformation item) {
        mItems.add(item);
        notifyDataSetChanged();
    }

    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    public void rename(String name, int position) {
        mItems.get(position).setName(name);
        notifyItemChanged(position);
    }

    public void remove(int index) {
        TimeInformation item = mItems.get(index);
        if (itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.remove(item);
        }
        if (mItems.contains(item)) {
            mItems.remove(index);
            notifyDataSetChanged();
        }
    }

    public void updateShowFragment(final int index){
        bus.post(new Send<Integer>() {
            @Override
            public Integer receive() {
               return index;
            }

            @Override
            public MessageTag tag() {
                return MessageTag.DELETED;
            }
        });
    }

    public void pendingRemoval(final int position) {
        final TimeInformation item = mItems.get(position);
        if (!itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.add(item);
            // this will redraw row in "undo" state
            notifyItemChanged(position);
            // let's create, store and post a runnable to remove the item
            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    int index = mItems.indexOf(item);
                    remove(index);
                    notifyItemChanged(position);
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunnables.put(item, pendingRemovalRunnable);
        }
    }

    public boolean isPendingRemoval(int position) {
        TimeInformation item = mItems.get(position);
        return itemsPendingRemoval.contains(item);
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public SplitTimerListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.stored_splittimer_list_item, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView, this);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(SplitTimerListAdapter.ViewHolder holder, final int position) {


        final TimeInformation tinfo = (TimeInformation) getItem(position);

        final TextView nameView = holder.getTitleView();
        final TextView timeView = holder.getTimeView();
        final Button undoButton = holder.getUndoButton();
        final ImageView overflow = holder.getOverflowIcon();

        if (itemsPendingRemoval.contains(tinfo)) {
            // we need to show the "undo" state of the row
            holder.itemView.setBackgroundColor(Color.GRAY);
            nameView.setVisibility(View.INVISIBLE);
            timeView.setVisibility(View.GONE);
            overflow.setVisibility(View.GONE);
            undoButton.setVisibility(View.VISIBLE);
            undoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // user wants to undo the removal, let's cancel the pending task
                    Runnable pendingRemovalRunnable = pendingRunnables.get(tinfo);
                    pendingRunnables.remove(tinfo);
                    if (pendingRemovalRunnable != null)
                        handler.removeCallbacks(pendingRemovalRunnable);
                    itemsPendingRemoval.remove(tinfo);
                    // this will rebind the row in "normal" state
                    notifyItemChanged(mItems.indexOf(tinfo));
                }
            });
        } else {

            // we need to show the "normal" state
            //holder.itemView.setBackgroundColor(Color.WHITE);
            nameView.setVisibility(View.VISIBLE);
            timeView.setVisibility(View.VISIBLE);
            overflow.setVisibility(View.VISIBLE);
            undoButton.setVisibility(View.GONE);
            undoButton.setOnClickListener(null);


            if(tinfo != null) {
                nameView.setText(tinfo.getName());
                Date date = new Date(tinfo.getStoringTime());
                String formattedDate = DateFormat.getInstance().format(date);
                timeView.setText(formattedDate);
            }


            holder.itemView.setLongClickable(true);

            holder.itemView.setOnClickListener(new ListView.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Notify the hosting Activity that a selection has been made.
                    Log.i(TAG, "List item clicked at: " + position);
                    bus.post(new Send<TimeInformation>() {
                        public MessageTag tag() {
                            return MessageTag.OPEN;
                        }
                        public TimeInformation receive() {
                            final TimeInformation tinfo = (TimeInformation) getItem(position);
                            SplitTimerListAdapter.this.lastOpened = position;
                            return tinfo;
                        }
                    });
                }
            });
        }
    }





    public String dump() {
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for(TimeInformation tinfo: mItems) {
            Log.d(TAG, " TINFO " + tinfo);
            if(!isFirst) {
                builder.append("\n");
            } else {
                isFirst = false;
            }
            builder.append(CSVSupport.toCSV(tinfo));
        }
        return builder.toString();
    }


    @Override
    public int getItemCount() {
        return mItems.size();
    }


    public Object getItem(int pos) {
        return mItems.get(pos);
    }


    @Override
    public long getItemId(int pos) {
        return pos;
    }

    static public class ViewHolder extends RecyclerView.ViewHolder
            implements PopupMenu.OnMenuItemClickListener {
        private TextView titleView, timeView;
        private ImageView overflow;
        private Button undoButton;
        private final View row;
        private SplitTimerListAdapter outer;

        public ViewHolder(View row, SplitTimerListAdapter outer) {
            super(row);
            this.row = row;
            this.outer = outer;
        }

        public ImageView getOverflowIcon() {
            if (overflow == null) {
                overflow = (ImageView) row.findViewById(R.id.stored_list_item_icon_overflow);
                overflow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu popup = new PopupMenu(view.getContext(), view);
                        popup.inflate(R.menu.context_menu_entries);
                        popup.setOnMenuItemClickListener(ViewHolder.this);
                        popup.show();
                    }
                });
            }
            return overflow;
        }
        public Button getUndoButton() {
            if (undoButton == null) {
                undoButton = (Button) row.findViewById(R.id.undo_button);
            }
            return undoButton;
        }

        public TextView getTitleView() {
            if (titleView == null) {
                titleView = (TextView) row.findViewById(R.id.saved_item_name);
            }
            return titleView;
        }

        public TextView getTimeView() {
            if (timeView == null) {
                timeView = (TextView) row.findViewById(R.id.saved_item_time);
            }
            return timeView;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            final int index = getLayoutPosition();
            switch (item.getItemId()) {
                case R.id.context_menu_delete_entry:
                    outer.remove(index);
                    if(outer.lastOpened == index)
                        outer.updateShowFragment(index);
                    return true;
                case R.id.context_menu_rename_entry:
                    SaveDialog newDialog = SaveDialog.newInstance(new ResultReceiver(null) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            final String result = resultData.getString("name");
                            if (resultCode == Activity.RESULT_OK) {
                                outer.rename(result, index);
                                if(outer.lastOpened == index) {
                                    bus.post(new Send<String>() {
                                        @Override
                                        public String receive() {
                                            return result;
                                        }

                                        @Override
                                        public MessageTag tag() {
                                            return MessageTag.RENAMED;
                                        }
                                    });
                                }
                            }
                        }
                    });
                    newDialog.getDialog();
                    newDialog.show(((AppCompatActivity) outer.mContext).getSupportFragmentManager(),
                            Configurations.SAVE_DIALOG_TAG);
                    return true;
                default:
                    return false;
            }
        }
    }
}

