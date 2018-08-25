package com.nezspencer.countriesapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nezspencer.countriesapp.databinding.ActivityCountryBinding;

import java.util.ArrayList;

public class CountryActivity extends AppCompatActivity {


    public static final String REQUEST_URL = "https://restcountries.eu/rest/v2/all";
    private CountriesRecyclerAdapter adapter;
    private ActivityCountryBinding binding;
    public static final String TAG = CountryActivity.class.getName();

    public static final float ANCHOR_MEASURED_FROM_RIGHT = 0.4f;

    private ItemTouchHelper.SimpleCallback touchHelper = new ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT) {


        boolean canSwipeBack;
        float anchorPoint = 200;
        private RecyclerView.ViewHolder currentHolder;
        private Canvas previousCanvas;
        private float prevDX;
        private float prevDY;

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
                                    getDefaultUIUtil().onDraw(previousCanvas, binding
                                                    .rvCountriesList, holder.getViewToSwipe(), prevDX,
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

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout
                .activity_country);

        setSupportActionBar(binding.toolbar);
        ArrayList<Country> countries = new ArrayList<>();

        adapter = new CountriesRecyclerAdapter(this, countries);
        binding.rvCountriesList.addItemDecoration(new DividerItemDecoration(this,
                LinearLayoutManager.VERTICAL));
        binding.rvCountriesList.setAdapter(adapter);

        final CountriesViewModel viewModel = ViewModelProviders.of(this, new
                CountryViewModelFactory(REQUEST_URL)).get
                (CountriesViewModel.class);

        viewModel.getCountriesLiveData().observe(this, new Observer<ArrayList<Country>>() {
            @Override
            public void onChanged(@Nullable ArrayList<Country> countries) {
                if (countries != null)
                    adapter.refreshList(countries);
            }
        });

        viewModel.getNetworkStatusLiveData().observe(this, new Observer<Status>() {
            @Override
            public void onChanged(@Nullable Status status) {
                if (status == null)
                    return;
                if (status.errorMessage != null) {
                    //request failed; log error and inform user to retry
                    Toast.makeText(CountryActivity.this, "Request failed, please reload",
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, status.errorMessage);
                    dismissProgress();
                }

                if (status.resultCode == Code.SUCCESS) {
                    dismissProgress();

                }
                else if (status.resultCode == Code.LOADING) {
                    showProgress();
                }
            }
        });

        binding.srlCountry.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewModel.fetchCountries();
            }
        });

        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(touchHelper);
        mItemTouchHelper.attachToRecyclerView(binding.rvCountriesList);
    }

    private void showProgress() {
        if (!binding.srlCountry.isRefreshing())
            binding.srlCountry.setRefreshing(true);
    }

    private void dismissProgress() {
        if (binding.srlCountry.isRefreshing())
            binding.srlCountry.setRefreshing(false);
    }
}
