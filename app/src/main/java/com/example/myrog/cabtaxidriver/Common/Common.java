package com.example.myrog.cabtaxidriver.Common;

import android.location.Location;

import com.example.myrog.cabtaxidriver.Remote.FCMClient;
import com.example.myrog.cabtaxidriver.Remote.IFCMService;
import com.example.myrog.cabtaxidriver.Remote.IGoogleAPI;
import com.example.myrog.cabtaxidriver.Remote.RetrofitClient;

/**
 * Created by My Rog on 11/19/2017.
 */

public class Common {



    public static final String driver_tbl  = "Drivers";
    public static final String user_driver_tbl  = "DriversInformation";
    public static final String user_rider_tbl  = "RidersInformation";
    public static final String pickup_request_tbl  = "PickupRequest";
    public static final String token_tbl  = "Tokens";
    public static Location mlastlocation=null;

    public static final String baseURL = "https://maps.googleapis.com";
    public static final String fcmURL = "https://fcm.googleapis.com/";


    public static IGoogleAPI getGoogleAPI(){
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }
    public static IFCMService getFCMService(){
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }
}
