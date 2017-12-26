package com.example.myrog.cabtaxidriver;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myrog.cabtaxidriver.Common.Common;
import com.example.myrog.cabtaxidriver.Model.FCMResponse;
import com.example.myrog.cabtaxidriver.Model.Notification;
import com.example.myrog.cabtaxidriver.Model.Sender;
import com.example.myrog.cabtaxidriver.Model.Token;
import com.example.myrog.cabtaxidriver.Remote.IFCMService;
import com.example.myrog.cabtaxidriver.Remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerCall extends AppCompatActivity {
    TextView txtTime,txtDistance, txtAddress;
    Button btnAccept,btnDecline;
    MediaPlayer mediaPlayer;

    IGoogleAPI mService;
    IFCMService mFCMService;

    //Khai báo biến cục bộ , thông qua đó xác nhận idcustomer từ app Rider bằng cách này để xác nhận vị trí
    String customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_call);

        mService = Common.getGoogleAPI();
        mFCMService = Common.getFCMService();

        txtTime = (TextView) findViewById(R.id.txtTime);
        txtDistance = (TextView) findViewById(R.id.txtDistance);
        txtAddress = (TextView) findViewById(R.id.txtAddress);

        btnAccept = (Button) findViewById(R.id.btnAccept);
        btnDecline = (Button) findViewById(R.id.btnDecline);
        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (!TextUtils.isEmpty(customerId))
                   // cancelBooking(customerId);
            }
        });

        mediaPlayer = MediaPlayer.create(this,R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        if (getIntent()!=null)
        {
            double lat = getIntent().getDoubleExtra("lat",-1.0);
            double lng =getIntent().getDoubleExtra("lng",-1.0);
            customerId = getIntent().getStringExtra("customer");

            getDirection(lat,lng);
        }
    }

//    private void cancelBooking(String customerId) {
//        Token token = new Token(customerId);
//        Notification notification = new Notification("Notice","Tài xế đã từ chối yêu cầu của bạn !");
//       Sender sender = new Sender(notification,token.getToken());
//       mFCMService.sendMessage(sender)
//               .enqueue(new Callback<FCMResponse>() {
//                   @Override
//                   public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
//                        if (response.body().success==1)
//                        {
//                            Toast.makeText(CustomerCall.this, "Cancelled", Toast.LENGTH_SHORT).show();
//                            finish();
//                        }
//                   }
//
//                   @Override
//                   public void onFailure(Call<FCMResponse> call, Throwable t) {
//
//                   }
//               });
//    }

    private void getDirection(double lat,double lng) {

        String requestAPI = null;
        try{
            requestAPI ="https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin=" + Common.mlastlocation.getLatitude()+","+Common.mlastlocation.getLongitude()+"&"+
                    "destination="+lng+","+lat+"&"+
                    "key="+ getResources().getString(R.string.google_direction_api);
            Log.d("NHATTRUONG'S HOME",requestAPI);
            mService.getPath(requestAPI)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                JSONObject jsonObject =new JSONObject(response.body().toString());

                                JSONArray routes = jsonObject.getJSONArray("routes");

                                //sau khi get routes , chỉ cần get phần tử đầu tiên của routes
                                JSONObject object = routes.getJSONObject(0);

                                // sau khi get phần tử đầu tiên , get mảng với tên là "legs"
                                JSONArray legs = object.getJSONArray("legs");

                                //và get phần tử đầu tiên của mảng
                                JSONObject legsObjects = legs.getJSONObject(0);

                                //get distance
                                JSONObject distance = legsObjects.getJSONObject("distance");
                                txtDistance.setText(distance.getString("text"));

                                //get Address
                                String address = legsObjects.getString("end_address ");
                                txtAddress.setText(address);

                                //get Time
                                JSONObject time = legsObjects.getJSONObject("duration");
                                txtTime.setText(time.getString("text"));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(CustomerCall.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        mediaPlayer.release();
        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();
        mediaPlayer.start();
    }
}
