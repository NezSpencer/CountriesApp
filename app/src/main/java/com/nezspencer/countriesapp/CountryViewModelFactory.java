package com.nezspencer.countriesapp;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public class CountryViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private String url;

    public CountryViewModelFactory(@NonNull String requestUrl) {
        super();
        this.url = requestUrl;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new CountriesViewModel(url);
    }
}
