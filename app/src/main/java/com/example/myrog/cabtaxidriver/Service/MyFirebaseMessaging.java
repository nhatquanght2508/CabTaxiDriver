package com.example.myrog.cabtaxidriver.Service;

import android.content.Intent;

import com.example.myrog.cabtaxidriver.CustomerCall;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

/**
 * Created by My Rog on 12/2/2017.
 */

public class MyFirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Bởi vì Firebase sẽ gửi tin nhắn với nội dung bao gồm Lat and Long , nên cần Convert tin nhắn thành 2 cái này
        LatLng customer_location = new Gson().fromJson(remoteMessage.getNotification().getBody(),LatLng.class);
        Intent intent = new Intent(getBaseContext(), CustomerCall.class);
        intent.putExtra("lat",customer_location.latitude);
        intent.putExtra("lng",customer_location.longitude);
        intent.putExtra("customer",remoteMessage.getNotification().getTitle());
        startActivity(intent);

    }
}
