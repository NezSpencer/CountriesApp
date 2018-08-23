package com.nezspencer.countriesapp;

import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.nezspencer.countriesapp.databinding.ActivityCountryBinding;

import java.util.ArrayList;

public class CountryActivity extends AppCompatActivity {

    private ProgressDialog dialog;
    private ArrayList<Country> countries;
    public static final String REQUEST_URL = "https://restcountries.eu/rest/v2/all";
    private CountriesRecyclerAdapter adapter;
    private ActivityCountryBinding binding;

    public static final float ANCHOR_MEASURED_FROM_RIGHT = 0.4f;
    public static final float ANCHOR_MEASURED_FROM_LEFT = 0.6f;

    private ItemTouchHelper.SimpleCallback touchHelper = new ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT) {

        Drawable background;
        Drawable deleteIcon;
        int xMarkMargin;
        boolean initiated;

        private void init() {
            background = new ColorDrawable(Color.parseColor("#5E35B1"));
            deleteIcon = ContextCompat.getDrawable(CountryActivity.this, R.drawable.ic_delete);
            if (deleteIcon != null)
                deleteIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            adapter.deleteCountry(viewHolder.getAdapterPosition());
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            View itemView = viewHolder.itemView;
            if (viewHolder.getAdapterPosition() == -1) {
                // not interested in those
                return;
            }

            if (!initiated) {
                init();
            }

            // draw purple background
            background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
            background.draw(c);

            // draw bomb icon
            int itemHeight = itemView.getBottom() - itemView.getTop();
            int intrinsicWidth = deleteIcon.getIntrinsicWidth();
            int intrinsicHeight = deleteIcon.getIntrinsicWidth();

            int xMarkLeft = (int) (itemView.getRight() * ANCHOR_MEASURED_FROM_LEFT);
            int xMarkRight = xMarkLeft + intrinsicWidth;
            int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
            int xMarkBottom = xMarkTop + intrinsicHeight;
            deleteIcon.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

            deleteIcon.draw(c);

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

        @Override
        public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

        @Override
        public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
            return ANCHOR_MEASURED_FROM_RIGHT;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout
                .activity_country);
        countries = new ArrayList<>();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait loading...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        adapter = new CountriesRecyclerAdapter(countries);
        binding.rvCountriesList.addItemDecoration(new DividerItemDecoration(this,
                LinearLayoutManager.VERTICAL));
        binding.rvCountriesList.setAdapter(adapter);
        final CountriesViewModel viewModel = ViewModelProviders.of(this).get(CountriesViewModel.class);
        viewModel.getCountriesLiveData().observe(this, new Observer<ArrayList<Country>>() {
            @Override
            public void onChanged(@Nullable ArrayList<Country> countries) {
                if (countries == null)
                    viewModel.fetchCountries(REQUEST_URL);
                else
                    adapter.refreshList(countries);
            }
        });

        viewModel.getNetworkStatusLiveData().observe(this, new Observer<Status>() {
            @Override
            public void onChanged(@Nullable Status status) {
                if (status == null)
                    return;
                if (status.resultCode == Code.SUCCESS) {
                    // you can fetch data
                    dismissDialog();

                }
                else if (status.resultCode == Code.LOADING) {
                    showDialog();
                }
            }
        });

        binding.srlCountry.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewModel.fetchCountries(REQUEST_URL);
            }
        });

        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(touchHelper);
        mItemTouchHelper.attachToRecyclerView(binding.rvCountriesList);
    }

    private void showDialog(){
        if (dialog != null && !dialog.isShowing())
            dialog.show();
        if (!binding.srlCountry.isRefreshing())
            binding.srlCountry.setRefreshing(true);
    }

    private void dismissDialog() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        if (binding.srlCountry.isRefreshing())
            binding.srlCountry.setRefreshing(false);
    }
}
