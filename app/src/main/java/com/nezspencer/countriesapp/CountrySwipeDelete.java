package com.nezspencer.countriesapp;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class CountrySwipeDelete extends ItemTouchHelper.SimpleCallback {

    private RecyclerView appRecycler;
    private CountriesRecyclerAdapter adapter;
    private static final float ANCHOR_MEASURED_FROM_RIGHT = 0.7f;

    private boolean canSwipeBack;
    private float anchorPoint = 200;
    private RecyclerView.ViewHolder currentHolder;
    private Canvas previousCanvas;
    private float prevDX;
    private float prevDY;

    public CountrySwipeDelete(CountriesRecyclerAdapter adapter, RecyclerView appRecycler) {
        super(0, ItemTouchHelper.LEFT);
        this.adapter = adapter;
        this.appRecycler = appRecycler;
    }


    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        adapter.deleteCountry(viewHolder.getAdapterPosition());
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (canSwipeBack) {
            canSwipeBack = false;
            return 0;
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        getDefaultUIUtil().clearView(((CountriesRecyclerAdapter.Holder) viewHolder).getViewToSwipe());
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {

        if (currentHolder != null) {
            if (currentHolder.getAdapterPosition() != viewHolder.getAdapterPosition())
                ((CountriesRecyclerAdapter.Holder) currentHolder).shouldStopAtAnchor = false;
            getDefaultUIUtil().onDraw(c, recyclerView,
                    ((CountriesRecyclerAdapter.Holder) currentHolder).getViewToSwipe(),
                    0F, dY, actionState, false);
        }

        if (viewHolder != null) {
            CountriesRecyclerAdapter.Holder holder = ((CountriesRecyclerAdapter.Holder)
                    viewHolder);
            currentHolder = viewHolder;
            previousCanvas = c;
            prevDX = dX;
            prevDY = dY;

            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                if (holder.shouldStopAtAnchor)
                    dX = Math.min(dX, -anchorPoint);
                else
                    setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            getDefaultUIUtil().onDraw(c, recyclerView,
                    ((CountriesRecyclerAdapter.Holder) viewHolder).getViewToSwipe(),
                    dX, dY, actionState, isCurrentlyActive);
        }

    }

    @Override
    public void onSelectedChanged(final RecyclerView.ViewHolder viewHolder, final int actionState) {

        if (viewHolder != null) {
            ((CountriesRecyclerAdapter.Holder) viewHolder).getViewToSwipe()
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                                CountriesRecyclerAdapter.Holder holder =
                                        ((CountriesRecyclerAdapter.Holder) currentHolder);
                                holder.shouldStopAtAnchor
                                        = false;
                                getDefaultUIUtil().onDraw(previousCanvas, appRecycler, holder
                                                .getViewToSwipe(), prevDX,
                                        prevDY, ItemTouchHelper.ACTION_STATE_SWIPE, false);
                            }

                        }
                    });
            getDefaultUIUtil().onSelected(((CountriesRecyclerAdapter.Holder) viewHolder)
                    .getViewToSwipe());
        }

    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        // get the view which is currently swiped
        RelativeLayout itemLayout = (RelativeLayout) ((CountriesRecyclerAdapter.Holder) viewHolder).getViewToSwipe();
        // calculate relative horizontal displacement
        // with proportion dXRelative : 1 = dX : (layoutWidth / 3)
        float dXRelative = dX / itemLayout.getWidth() * 3;
        // check size boundaries
        if (dXRelative > 1) {
            dXRelative = 1;
        }
        if (dXRelative < 0) {
            dXRelative = 0;
        }

        // call draw over
        getDefaultUIUtil().onDrawOver(c, recyclerView, itemLayout, dX, dY, actionState,
                isCurrentlyActive);
    }


    @Override
    public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
        return ANCHOR_MEASURED_FROM_RIGHT;
    }

    private void setTouchListener(final Canvas c,
                                  final RecyclerView recyclerView,
                                  final RecyclerView.ViewHolder viewHolder,
                                  final float dX, final float dY,
                                  final int actionState, final boolean isCurrentlyActive) {

        final CountriesRecyclerAdapter.Holder holder = ((CountriesRecyclerAdapter.Holder)
                viewHolder);
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                canSwipeBack = event.getAction() == MotionEvent.ACTION_CANCEL ||
                        event.getAction() == MotionEvent.ACTION_UP;
                if (canSwipeBack) {
                    holder.shouldStopAtAnchor = dX < -anchorPoint;
                    if (holder.shouldStopAtAnchor)
                        setTouchDownListener(c, recyclerView, viewHolder, dX, dY, actionState,
                                isCurrentlyActive);
                }
                return false;
            }
        });
    }

    private void setTouchDownListener(final Canvas c,
                                      final RecyclerView recyclerView,
                                      final RecyclerView.ViewHolder viewHolder,
                                      final float dX, final float dY,
                                      final int actionState, final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    setTouchUpListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
                return false;
            }
        });
    }

    private void setTouchUpListener(final Canvas c,
                                    final RecyclerView recyclerView,
                                    final RecyclerView.ViewHolder viewHolder,
                                    final float dX, final float dY,
                                    final int actionState, final boolean isCurrentlyActive) {
        final CountriesRecyclerAdapter.Holder holder = ((CountriesRecyclerAdapter.Holder)
                viewHolder);
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    setItemsClickable(recyclerView, true);
                    canSwipeBack = false;
                    holder.shouldStopAtAnchor = false;
                    onChildDraw(c, recyclerView, viewHolder, 0F, dY, actionState, isCurrentlyActive);

                }
                return false;
            }
        });
    }

    private void setItemsClickable(RecyclerView recyclerView,
                                   boolean isClickable) {
        for (int i = 0; i < recyclerView.getChildCount(); ++i) {
            recyclerView.getChildAt(i).setClickable(isClickable);
        }
    }
}
