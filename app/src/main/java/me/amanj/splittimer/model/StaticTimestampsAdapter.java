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



import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import me.amanj.splittimer.R;
import me.amanj.splittimer.util.TimeInformation;
import me.amanj.splittimer.util.Timestamp;


public class StaticTimestampsAdapter extends RecyclerView.Adapter<StaticTimestampsAdapter.ViewHolder> {

    protected TimeInformation mItems = new TimeInformation();
    protected final Context mContext;

    public StaticTimestampsAdapter(Context context) {
        mContext = context;
    }

    public void setName(String name) {
        mItems.setName(name);
    }
    public void setTimeInformation(TimeInformation tinfo) {
        mItems = tinfo;
        notifyDataSetChanged();
    }


    public long getStoringTime() { return mItems.getStoringTime(); }
    public String getName() { return mItems.getName(); }

    public void mergeWithAbove(int position) {
        int index = posToIndex(position);
        mItems.mergeLaps(index, index + 1);
        notifyDataSetChanged();
    }

    public void mergeWithBelow(int position) {
        int index = posToIndex(position);
        mItems.mergeLaps(index - 1, index);
        notifyDataSetChanged();
    }

    private int posToIndex(int position) {
        return mItems.countLaps() - position - 1;
    }

    public void add(Long item) {
        mItems.addLap(item);
        notifyDataSetChanged();
    }

    public TimeInformation getAdapterContent() {
        return (TimeInformation) mItems.clone();
    }

    public void clear() {
        mItems.clearLaps();
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return mItems.countLaps();
    }


    public Object getItem(int pos) {
        return mItems.lapAt(posToIndex(pos));
    }

    public long getElapsedItem(int pos) {
        return mItems.elapsedTimeAt(posToIndex(pos));
    }
    public long getLastLapTime() {
        return mItems.lapAt(mItems.countLaps() - 1);
    }
    public long getLastElapsedTime() {
        return mItems.getElapsedTime();
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    static public class ViewHolder extends RecyclerView.ViewHolder
            implements PopupMenu.OnMenuItemClickListener {
        private TextView infoView, lapView, elapsedView;
        private ImageView overflow;
        private final View row;
        private final StaticTimestampsAdapter outer;

        public ViewHolder(View row, StaticTimestampsAdapter outer) {
            super(row);
            this.row = row;
            this.outer = outer;
        }

        public ImageView getOverflowIcon() {
            if(overflow == null) {
                overflow = (ImageView) row.findViewById(R.id.lap_item_overflow);
                overflow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu popup = new PopupMenu(view.getContext(), view);
                        popup.inflate(R.menu.context_menu_laps);
                        popup.setOnMenuItemClickListener(ViewHolder.this);
                        if(outer.getItemCount() == 1) {
                            popup.getMenu().getItem(0).setEnabled(false);
                            popup.getMenu().getItem(1).setEnabled(false);
                        } else if(getLayoutPosition() == outer.getItemCount() - 1) {
                            popup.getMenu().getItem(1).setEnabled(false);
                        } else if(getLayoutPosition() == 0) {
                            popup.getMenu().getItem(0).setEnabled(false);
                        }
                        popup.show();
                    }
                });
            }
            return overflow;
        }

        public TextView getInfoView() {
            if(infoView == null) {
                infoView = (TextView) row.findViewById(R.id.lap_item_info_caption);
            }
            return infoView;
        }

        public TextView getLapView() {
            if(lapView == null) {
                lapView = (TextView) row.findViewById(R.id.lap_item_lap_data);
            }
            return lapView;
        }

        public TextView getElapsedView() {
            if(elapsedView == null) {
                elapsedView = (TextView) row.findViewById(R.id.lap_item_split_data);
            }
            return elapsedView;
        }

         // Process clicks on Context Menu Items
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.merge_with_above:
                    outer.mergeWithAbove(getLayoutPosition());
                    return true;
                case R.id.merge_with_below:
                    outer.mergeWithBelow(getLayoutPosition());
                    return true;
                default:
                    return false;
            }
        }

    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public StaticTimestampsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.lap_item, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView, this);
        return viewHolder;
    }


    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(StaticTimestampsAdapter.ViewHolder holder, final int position) {
        final Long lapInfo = (Long) getItem(position);
        final Long elapsedInfo = getElapsedItem(position);

        final TextView infoView = holder.getInfoView();
        final TextView lapView = holder.getLapView();
        final TextView elapsedView = holder.getElapsedView();

        infoView.setText(String.format(mContext.getResources().getString(
                R.string.lap_ordinal_items), posToIndex(position) + 1));

        lapView.setText(Timestamp.timeStampToString(lapInfo));
        elapsedView.setText(Timestamp.timeStampToString(elapsedInfo));
    }
}

