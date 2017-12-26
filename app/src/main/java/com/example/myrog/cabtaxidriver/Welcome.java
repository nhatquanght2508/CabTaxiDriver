package com.example.myrog.cabtaxidriver;

import android.*;
import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Interpolator;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myrog.cabtaxidriver.Common.Common;
import com.example.myrog.cabtaxidriver.Model.Token;
import com.example.myrog.cabtaxidriver.Remote.IGoogleAPI;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.xml.sax.helpers.LocatorImpl;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.R.attr.end;
import static android.R.attr.value;

public class Welcome extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener
{

    private GoogleMap mMap;

    //Init Play Services
    private static final int MY_PERMISSION_REQUEST_CODE =7000;
    private static final int PLAY_SERVICE_RES_REQUEST =7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private static int UPDATE_INTERVAL =8000;
    private static int FASTES_INTERVAL =8000;
    private static int DISPLACEMENT =15;

    DatabaseReference drivers;
    GeoFire geoFire;
    Marker mcurrent;
    MaterialAnimatedSwitch location_switch;
    SupportMapFragment mapFragment;

    //Animation Cab taxi
    private List<LatLng> polyLineList;
    private Marker carMarker;
    private float v;
    private double lat,lng;
    private Handler handler;
    private LatLng startPosition,endPosition,currentPosition;
    private int index,next;
   // private Button btnGo;
    private PlaceAutocompleteFragment places;
    private String destination;
    private PolylineOptions polylineOptions,blackPolylineOptions;
    private Polyline blackPolyline,greyPolyline;
    private IGoogleAPI mService;
    DatabaseReference onlineRef,currentUserRef;

    Runnable drawPathRunnable = new Runnable() {
        @Override
        public void run() {
            if (index<polyLineList.size()-1){
                index++;
                next=index+1;
            }
            if (index<polyLineList.size()-1){
                startPosition = polyLineList.get(index);
                endPosition = polyLineList.get(next);
            }
            final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0,1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    v= valueAnimator.getAnimatedFraction();
                    lng = v*endPosition.longitude+(1-v)*startPosition.longitude;
                    lat = v*endPosition.latitude+(1-v)*startPosition.latitude;
                    LatLng newPos = new LatLng(lat,lng);
                    carMarker.setPosition(newPos);
                    carMarker.setAnchor(0.5f,0.5f);
                    carMarker.setRotation(getBearing(startPosition,newPos));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                            .target(newPos)
                            .zoom(15.5f)
                            .build()
                    ));
                }
            });
            valueAnimator.start();
            handler.postDelayed(this,3000);
        }
    };

    private float getBearing(LatLng startPosition, LatLng endPosition) {
        double lat = Math.abs(startPosition.latitude-endPosition.latitude);
        double lng =Math.abs(startPosition.longitude-endPosition.longitude);
        if (startPosition.latitude<endPosition.latitude && startPosition.longitude < endPosition.longitude)
            return (float)(Math.toDegrees(Math.atan(lng/lat)));

        else if (startPosition.latitude>=endPosition.latitude && startPosition.longitude < endPosition.longitude)
            return (float)((90-Math.toDegrees(Math.atan(lng/lat)))+90);

        else if (startPosition.latitude>=endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            return (float)(Math.toDegrees(Math.atan(lng/lat))+180);

        else if (startPosition.latitude<endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            return (float)((90-Math.toDegrees(Math.atan(lng/lat)))+270);
        return  -1;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Present System
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        currentUserRef = FirebaseDatabase.getInstance().getReference(Common.driver_tbl)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //remove value từ driver_tbl when driver disconected
                currentUserRef.onDisconnect().removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //Init View
        location_switch = (MaterialAnimatedSwitch) findViewById(R.id.location_switch);
        location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean b) {
                if (b){
                    FirebaseDatabase.getInstance().goOnline(); // set Connected
                    startUpdateLocation();
                    displayLocation();
                    Snackbar.make(mapFragment.getView(),"Bạn đang online",Snackbar.LENGTH_SHORT).show();
                }
                else {
                    FirebaseDatabase.getInstance().goOffline();//Set disconnected
                    stopLocationUpdates();
                    mcurrent.remove();
                    mMap.clear();
                    handler.removeCallbacks(drawPathRunnable);
                    Snackbar.make(mapFragment.getView(),"Bạn đang offline",Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        //GeoFire

        polyLineList = new ArrayList<>();

        //place API Google
        places = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if(location_switch.isChecked()){
                    destination = place.getAddress().toString();
                    destination = destination.replace(" ","+");
                    getDirection();
                }
                else {
                    Toast.makeText(Welcome.this, "Hãy bật định vị", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(Welcome.this, ""+status.toString(), Toast.LENGTH_SHORT).show();
            }
        });


        drivers = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
        geoFire = new GeoFire(drivers);
        setUpLocation();
        mService = Common.getGoogleAPI();
        updateFirebaseToken();
    }

    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tbl);
        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(token);


    }

    private void getDirection() {
        currentPosition = new LatLng(Common.mlastlocation.getLatitude(),Common.mlastlocation.getLongitude());
        String requestAPI = null;
        try{
            requestAPI ="https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin=" +currentPosition.latitude+","+currentPosition.longitude+"&"+
                    "destination="+destination+"&"+
                    "key="+ getResources().getString(R.string.google_direction_api);
            Log.d("NHATTRUONG'S HOME",requestAPI);
            mService.getPath(requestAPI)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                JSONObject jsonObject =new JSONObject(response.body().toString());
                                JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                for (int i=0;i<jsonArray.length();i++){
                                    JSONObject route = jsonArray.getJSONObject(i);
                                    JSONObject poly = route.getJSONObject("overview_polyline");
                                    String polyline = poly.getString("points");
                                    polyLineList = decodePoly(polyline);
                                }
                                //Adjucting bounds
                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                for(LatLng latLng:polyLineList)
                                     builder.include(latLng);

                                LatLngBounds bounds = builder.build();
                                CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,2);
                                mMap.animateCamera(mCameraUpdate);
                                polylineOptions = new PolylineOptions();
                                polylineOptions.color(Color.GRAY);
                                polylineOptions.width(5);
                                polylineOptions.startCap(new SquareCap());
                                polylineOptions.endCap(new SquareCap());
                                polylineOptions.jointType(JointType.ROUND);
                                polylineOptions.addAll(polyLineList);
                                greyPolyline = mMap.addPolyline(polylineOptions);

                                blackPolylineOptions = new PolylineOptions();
                                blackPolylineOptions.color(Color.BLACK);
                                blackPolylineOptions.width(5);
                                blackPolylineOptions.startCap(new SquareCap());
                                blackPolylineOptions.endCap(new SquareCap());
                                blackPolylineOptions.jointType(JointType.ROUND);
                                blackPolyline = mMap.addPolyline(blackPolylineOptions);

                                mMap.addMarker(new MarkerOptions()
                                                .position(polyLineList.get(polyLineList.size()-1))
                                                .title("Lấy địa điểm"));
                                //Animation
                                ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0,100);

                                polyLineAnimator.setDuration(1500);
                                polyLineAnimator.setInterpolator(new LinearInterpolator());
                                polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        List<LatLng> points = greyPolyline.getPoints();
                                        int percentValue = (int)animation.getAnimatedValue();
                                        int size = points.size();
                                        int newPoints = (int)(size*(percentValue/100.0f));
                                        List<LatLng> p = points.subList(0,newPoints);
                                        blackPolyline.setPoints(p);


                                    }
                                });
                                polyLineAnimator.start();

                                carMarker =mMap.addMarker(new MarkerOptions().position(currentPosition)
                                        .flat(true)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cabtaxi5)));
                                handler = new Handler();
                                index= -1;
                                next =1;
                                handler.postDelayed(drawPathRunnable,3000);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(Welcome.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                        if (location_switch.isChecked()) {
                            displayLocation();
                        }
                    }
                }
        }
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                &&ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            //Request runtime permisstion
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            },MY_PERMISSION_REQUEST_CODE);
        }
        else {
            if (checkPlayServices()){
                buildGoogleApiClient();
                createLocationRequest();
                if (location_switch.isChecked()){
                    displayLocation();
                }
            }
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTES_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode!=ConnectionResult.SUCCESS){
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICE_RES_REQUEST).show();
            }
            else {
                Toast.makeText(this, "Dịch vụ không hổ trợ , hãy kiểm tra lại thiết bị", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                &&ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                &&ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        Common.mlastlocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (Common.mlastlocation !=null){
            if (location_switch.isChecked()){
                final double vido = Common.mlastlocation.getLatitude();
                final double kinhdo = Common.mlastlocation.getLongitude();

                //Update to Firebase
                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(vido, kinhdo), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        //Add Marker
                        if (mcurrent!=null){
                            mcurrent.remove(); // remove marker
                        }
                        mcurrent = mMap.addMarker(new MarkerOptions()

                                                    .position(new LatLng(vido,kinhdo))
                                                    .title("TUI NÈ !"));
                        //Move to your postition
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(vido,kinhdo),13.0f));
                        //Draw animation marker
                        rotateMarker(mcurrent,-360,mMap);
                    }
                });
            }
        }
        else {
            Log.d("ERROR","Không thể lấy vị trí hiện tại");
        }
    }

    private void rotateMarker(final Marker mcurrent,final int i, GoogleMap mMap) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = mcurrent.getRotation();
        final long duration = 1500;
        final android.view.animation.Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long eslaped = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float)eslaped/duration);
                float rot = t*i +(1-t)*startRotation;
                mcurrent.setRotation(-rot>180?rot/2:rot);
                if ((t<1.0)){
                    handler.postDelayed(this,15);

                }
            }
        });

    }

    private void startUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
            &&ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            return;
        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startUpdateLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Common.mlastlocation =  location;
        displayLocation();
    }


}
