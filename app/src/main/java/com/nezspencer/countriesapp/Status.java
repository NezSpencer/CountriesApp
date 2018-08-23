package com.nezspencer.countriesapp;

import android.support.annotation.NonNull;

public class Status {
    public static Status LOADED = new Status(Code.SUCCESS);
    public static Status LOADING = new Status(Code.LOADING);
    public final String errorMessage;
    public final Code resultCode;
    private Status(@NonNull Code code){
        this.errorMessage = null;
        this.resultCode = code;
    }

    private Status(@NonNull Code code, @NonNull String errorMessage) {
        this.errorMessage = errorMessage;
        this.resultCode = code;
    }

    public static Status error(@NonNull String errorMessage) {
        return new Status(Code.ERROR, errorMessage);
    }
}
