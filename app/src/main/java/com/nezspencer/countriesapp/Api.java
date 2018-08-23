package com.nezspencer.countriesapp;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class Api {

    private CountriesRequestListener listener;

    Api(CountriesRequestListener listener) {
        this.listener = listener;
    }

    public void getCountries(@NonNull final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL appUrl = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) appUrl.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader
                                (connection.getInputStream()));
                        StringBuilder builder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            builder.append(line+"\n");
                        }
                        bufferedReader.close();

                        JSONArray array = new JSONArray(builder.toString());
                        listener.onRequestSuccess(parseJsonArrayToPojoList(array));
                    }
                    else {
                        Log.e("Check", ""+responseCode);
                        listener.onRequestFailed(connection.getResponseMessage());
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private ArrayList<Country> parseJsonArrayToPojoList(@NonNull JSONArray array) throws JSONException{

        ArrayList<Country> countries = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject countryObject = array.getJSONObject(i);
            Country country = new Country();
            //get Name
            country.setName(countryObject.getString("name"));

            //get Language
            JSONObject language0 = countryObject.getJSONArray("languages").getJSONObject(0);
            String langName = language0.getString("name");
            String nativeLangName = language0.getString("nativeName");
            if (TextUtils.isEmpty(nativeLangName)) {
                country.setLanguage(language0.getString("name"));
            }
            else
                country.setLanguage(langName.concat(" ( ").concat(nativeLangName).concat(" )"));
            //get currency
            JSONObject currency0 = countryObject.getJSONArray("currencies").getJSONObject
                    (0);
            country.setCurrency(currency0.getString("name").concat(" ( ")
                    .concat(currency0.getString("symbol").concat(" )")));

            //set to list
            countries.add(country);
        }

        return countries;
    }
}
