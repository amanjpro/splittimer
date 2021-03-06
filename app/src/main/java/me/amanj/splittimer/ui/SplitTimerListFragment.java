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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


import java.util.ArrayList;
import java.util.List;

import me.amanj.splittimer.R;
import me.amanj.splittimer.util.IO;
import me.amanj.splittimer.messages.Send;
import me.amanj.splittimer.messages.Message;
import me.amanj.splittimer.messages.MessageTag;
import me.amanj.splittimer.util.CSVSupport;
import me.amanj.splittimer.util.Configurations;
import me.amanj.splittimer.model.SplitTimerListAdapter;
import me.amanj.splittimer.util.TimeInformation;

public class SplitTimerListFragment extends Fragment {

    private static final EventBus bus = EventBus.getDefault();
    private SplitTimerListAdapter mAdapter;
    private RecyclerView recyclerView;
    final static String TAG = SplitTimerListFragment.class.getCanonicalName();

    // newInstance constructor for creating fragment with arguments
    public static SplitTimerListFragment newInstance() {
        SplitTimerListFragment fragment = new SplitTimerListFragment();
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true);

        View rootView = inflater.inflate(R.layout.fragment_stored_splittimer_list, container, false);
        LinearLayoutManager llm = new LinearLayoutManager(this.getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.splittimer_list_recycler);
        recyclerView.setLayoutManager(llm);
        mAdapter = new SplitTimerListAdapter(getActivity());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);
        setUpItemTouchHelper();
        setUpAnimationDecoratorHelper();
        return rootView;
    }


    /**
     * This is the standard support library way of implementing "swipe to delete" feature. You can
     * do custom drawing in onChildDraw method but whatever you draw will disappear once the swipe
     * is over, and while the items are animating to their new position the recycler view
     * background will be visible. That is rarely an desired effect.
     */
    private void setUpItemTouchHelper() {

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            // we want to cache these and not allocate anything repeatedly in the onChildDraw method
            Drawable background;
            Drawable xMark;
            int xMarkMargin;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.GRAY);
                xMark = ContextCompat.getDrawable(getActivity(), R.drawable.ic_clear_24dp);
                xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                xMarkMargin = (int)
                        getActivity().getResources().getDimension(R.dimen.ic_clear_margin);
                initiated = true;
            }

            // not important, we don't want drag & drop
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView,
                                    RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                if (mAdapter.isPendingRemoval(position)) {
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int swipedPosition = viewHolder.getAdapterPosition();
                xMark.setVisible(false, false);
                viewHolder.itemView.invalidate();
                int newPosition = swipedPosition;
                mAdapter.pendingRemoval(swipedPosition);
                if(swipedPosition == mAdapter.getItemCount() - 1)
                    newPosition = 0;
                if(mAdapter.getItemCount() >= 1 && mAdapter.getLastOpened() == swipedPosition)
                    mAdapter.updateShowFragment(newPosition + 1, MessageTag.SWIPED);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                    RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState,
                                    boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                // not sure why, but this method get's called for viewholder that are
                // already swiped away
                if (viewHolder.getAdapterPosition() == -1) {
                    // not interested in those
                    return;
                }

                if (!initiated) {
                    init();
                }

                // draw red background
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(),
                        itemView.getRight(), itemView.getBottom());
                background.draw(c);

                // draw x mark
                int itemHeight = itemView.getBottom() - itemView.getTop();
                int intrinsicWidth = xMark.getIntrinsicWidth();
                int intrinsicHeight = xMark.getIntrinsicWidth();

                int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
                int xMarkRight = itemView.getRight() - xMarkMargin;
                int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int xMarkBottom = xMarkTop + intrinsicHeight;
                xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

                xMark.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY,
                        actionState, isCurrentlyActive);
            }

        };
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * We're gonna setup another ItemDecorator that will draw the red background in the empty
     * space while the items are animating to their new positions after an item is removed.
     */
    private void setUpAnimationDecoratorHelper() {
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {

            // we want to cache this and not allocate anything repeatedly in the onDraw method
            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.GRAY);
                initiated = true;
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

                if (!initiated) {
                    init();
                }

                // only if animation is in progress
                if (parent.getItemAnimator().isRunning()) {

                    // some items might be animating down and some items might be animating up to
                    // close the gap left by the removed item
                    // this is not exclusive, both movement can be happening at the same time
                    // to reproduce this leave just enough items so the first one and the last one
                    // would be just a little off screen then remove one from the middle

                    // find first child with translationY > 0
                    // and last one with translationY < 0
                    // we're after a rect that is not covered in recycler-view views at this point
                    // in time
                    View lastViewComingDown = null;
                    View firstViewComingUp = null;

                    // this is fixed
                    int left = 0;
                    int right = parent.getWidth();

                    // this we need to find out
                    int top = 0;
                    int bottom = 0;

                    // find relevant translating views
                    int childCount = parent.getLayoutManager().getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = parent.getLayoutManager().getChildAt(i);
                        if (child.getTranslationY() < 0) {
                            // view is coming down
                            lastViewComingDown = child;
                        } else if (child.getTranslationY() > 0) {
                            // view is coming up
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child;
                            }
                        }
                    }

                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        // views are coming down AND going up to fill the void
                        top = lastViewComingDown.getBottom() + (int) ViewCompat.getTranslationY(lastViewComingDown);
                        bottom = firstViewComingUp.getTop() + (int) ViewCompat.getTranslationY(firstViewComingUp);
                    } else if (lastViewComingDown != null) {
                        // views are going down to fill the void
                        top = lastViewComingDown.getBottom() + (int) ViewCompat.getTranslationY(lastViewComingDown);
                        bottom = lastViewComingDown.getBottom();
                    } else if (firstViewComingUp != null) {
                        // views are coming up to fill the void
                        top = firstViewComingUp.getTop();
                        bottom = firstViewComingUp.getTop() + (int) ViewCompat.getTranslationY(firstViewComingUp);
                    }

                    background.setBounds(left, top, right, bottom);
                    background.draw(c);

                }
                super.onDraw(c, parent, state);
            }

        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("timeInfos", mAdapter.dump());
        outState.putInt("lastOpened", mAdapter.getLastOpened());
        outState.putBoolean("laterStart", true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        if(savedInstanceState != null) {
            mAdapter.setLastOpened(savedInstanceState.getInt("lastOpened"));
            String[] timeInfos = savedInstanceState.getString("timeInfos").split("\n");
            Log.d(TAG, "Restored the timeInfos, size is: " + timeInfos.length);
            List<TimeInformation> times = new ArrayList<>();
            for(String time: timeInfos) {
                TimeInformation tinfo = CSVSupport.fromCSV(time);
                if(tinfo != null)
                    times.add(tinfo);
            }
            mAdapter.addAll(times);
        }

    }

    public void unsetLastOpen() {
        mAdapter.setLastOpened(-1);
    }
    public void redrawAll() {
        mAdapter.redrawAll();
    }


    @Subscribe
    public void onEvent(Message event) {
        if (event.tag() == MessageTag.SAVE)
            mAdapter.add(((Send<TimeInformation>) event).receive());
        else if (event.tag() == MessageTag.CLEAR_HISTORY) {
            mAdapter.clear();
            removeDetailedPane();
        } else if(event.tag() == MessageTag.DELETED || event.tag() == MessageTag.SWIPED) {
            int index     = ((Send<Integer>) event).receive();
            if((mAdapter.getItemCount() > 0 && event.tag() == MessageTag.DELETED) ||
                    (mAdapter.getItemCount() > 1 && event.tag() == MessageTag.SWIPED)) {
                final int nextIndex = index >= mAdapter.getItemCount()? 0 : index;
                bus.post(new Send<TimeInformation>() {
                    public MessageTag tag() {
                        return MessageTag.MAYBE_OPEN;
                    }
                    public TimeInformation receive() {
                        return (TimeInformation) mAdapter.getItem(nextIndex);
                    }
                });
            } else {
                removeDetailedPane();
            }
        }
    }


    private void removeDetailedPane() {
        mAdapter.setLastOpened(-1);
        bus.post(new Send<Void>() {
            public MessageTag tag() {
                return MessageTag.REMOVE_DETAILED_PANE;
            }

            public Void receive() {
                return null;
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
        if(mAdapter.getItemCount() == 0) {
            Log.i(TAG, "Loading entries command is being executed:  " + mAdapter.getItemCount());
            mAdapter.addAll(IO.loadEntries(getContext()));
            Log.i(TAG, "Loading entries command has executed:  " + mAdapter.getItemCount());
        }
    }

    @Override
    public void onPause() {
        bus.unregister(this);
        super.onPause();
        IO.saveEntriesToFile(getContext(), mAdapter.dump());
    }



}
