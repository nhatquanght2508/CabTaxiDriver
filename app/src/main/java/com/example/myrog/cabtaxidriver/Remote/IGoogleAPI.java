package com.example.myrog.cabtaxidriver.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by My Rog on 11/19/2017.
 */

public interface IGoogleAPI {
    @GET
    Call<String> getPath(@Url String url);
}
