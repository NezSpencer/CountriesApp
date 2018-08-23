package com.nezspencer.countriesapp;

import java.util.ArrayList;

public interface CountriesRequestListener {

    void onRequestSuccess(ArrayList<Country> countries);

    void onRequestFailed(String errorMessage);
}
