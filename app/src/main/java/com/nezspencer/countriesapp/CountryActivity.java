package com.nezspencer.countriesapp;

import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.Adapter;

import com.nezspencer.countriesapp.databinding.ActivityCountryBinding;

import java.util.ArrayList;

public class CountryActivity extends AppCompatActivity {

    private ProgressDialog dialog;
    private ArrayList<Country> countries;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCountryBinding binding = DataBindingUtil.setContentView(this, R.layout
                .activity_country);
        countries = new ArrayList<>();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please eait loading...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        final CountriesRecyclerAdapter adapter = new CountriesRecyclerAdapter(countries);
        binding.rvCountriesList.addItemDecoration(new DividerItemDecoration(this,
                LinearLayoutManager.VERTICAL));
        binding.rvCountriesList.setAdapter(adapter);
        CountriesViewModel viewModel = ViewModelProviders.of(this).get(CountriesViewModel.class);
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
                if (status.resultCode == Code.SUCCESS) {
                    // you can fetch data
                    dismissDialog();

                }
                else if (status.resultCode == Code.LOADING) {
                    showDialog();
                }
            }
        });
        viewModel.fetchCountries("https://restcountries.eu/rest/v2/all");
    }

    private void showDialog(){
        if (dialog != null && !dialog.isShowing())
            dialog.show();
    }

    private void dismissDialog() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }
}
