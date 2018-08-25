package com.nezspencer.countriesapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.widget.Toast;

import com.nezspencer.countriesapp.databinding.ActivityCountryBinding;

import java.util.ArrayList;

public class CountryActivity extends AppCompatActivity {


    public static final String REQUEST_URL = "https://restcountries.eu/rest/v2/all";
    private CountriesRecyclerAdapter adapter;
    private ActivityCountryBinding binding;
    public static final String TAG = CountryActivity.class.getName();

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

        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(new CountrySwipeDelete(adapter,
                binding.rvCountriesList));
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
