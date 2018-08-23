package com.nezspencer.countriesapp;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import java.util.ArrayList;

public class CountriesViewModel extends ViewModel implements CountriesRequestListener {

    private Api api;

    private final MutableLiveData<Status> networkStatusLiveData = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Country>> countriesLiveData = new MutableLiveData<>();

    public void fetchCountries(@NonNull String url) {
        api = new Api(this);
        networkStatusLiveData.setValue(Status.LOADING);
        api.getCountries(url);
    }

    @Override
    public void onRequestSuccess(ArrayList<Country> countries) {
        countriesLiveData.postValue(countries);
        networkStatusLiveData.postValue(Status.LOADED);
    }

    @Override
    public void onRequestFailed(String errorMessage) {
        networkStatusLiveData.postValue(Status.error(errorMessage));
    }

    public MutableLiveData<ArrayList<Country>> getCountriesLiveData() {
        return countriesLiveData;
    }

    public MutableLiveData<Status> getNetworkStatusLiveData() {
        return networkStatusLiveData;
    }
}
