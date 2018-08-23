package com.nezspencer.countriesapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.nezspencer.countriesapp.databinding.ItemCountryBinding;

import java.util.ArrayList;

public class CountriesRecyclerAdapter extends RecyclerView.Adapter<CountriesRecyclerAdapter.Holder> {

    private ArrayList<Country> countries;
    public CountriesRecyclerAdapter(ArrayList<Country> countries) {
        this.countries = countries;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCountryBinding binding = ItemCountryBinding.inflate(LayoutInflater.from(parent
                .getContext()));
        return new Holder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Country country = countries.get(holder.getAdapterPosition());
        holder.binding.tvCountryName.setText(country.getName());
        holder.binding.tvCountryLanguage.setText(country.getLanguage());
        holder.binding.tvCountryCurrency.setText(country.getCurrency());
    }

    @Override
    public int getItemCount() {
        return countries.size();
    }

    public void refreshList(@NonNull ArrayList<Country> countryArrayList) {
        countries.clear();
        countries.addAll(countryArrayList);
        notifyDataSetChanged();
    }

    public void deleteCountry(int position) {
        countries.remove(position);
        notifyItemRemoved(position);
    }

    class Holder extends RecyclerView.ViewHolder{
        ItemCountryBinding binding;
        public Holder(ItemCountryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }
}
