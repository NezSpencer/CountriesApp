package com.nezspencer.countriesapp;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import java.util.ArrayList;

public class CountriesViewModel extends ViewModel implements CountriesRequestListener {

    private Api api;
    private String requestUrl;

    public CountriesViewModel(@NonNull String url) {
        requestUrl = url;
        fetchCountries();
    }

    private final MutableLiveData<Status> networkStatusLiveData = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Country>> countriesLiveData = new MutableLiveData<>();

    public void fetchCountries() {
        api = new Api(this);
        networkStatusLiveData.setValue(Status.LOADING);
        api.getCountries(requestUrl);
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
