package com.example.myrog.cabtaxidriver.Common;

import com.example.myrog.cabtaxidriver.Remote.IGoogleAPI;
import com.example.myrog.cabtaxidriver.Remote.RetrofitClient;

/**
 * Created by My Rog on 11/19/2017.
 */

public class Common {
    public static final String baseURL = "https://maps.googleapis.com";
    public static IGoogleAPI getGoogleAPI(){
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }
}
