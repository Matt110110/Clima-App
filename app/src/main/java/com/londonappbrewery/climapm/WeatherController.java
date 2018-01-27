package com.londonappbrewery.climapm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class WeatherController extends AppCompatActivity {
    final int REQUEST_CODE = 123;
    // Constants:
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "f9f9595e24a57fcbda9e0211fb91766e";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    // TODO: Set LOCATION_PROVIDER here:
    String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;


    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    // TODO: Declare a LocationManager and a LocationListener here:
    LocationManager mLocationManager;
    LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);


        // TODO: Add an OnClickListener to the changeCityButton here:
        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(WeatherController.this, ChangeCityController.class);
                startActivity(newIntent);
            }
        });
    }


    // TODO: Add onResume() here:
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Clima", "onResume() called");
        Intent newIntent = getIntent();
        String city = newIntent.getStringExtra("City");
        if (city != null) {
            getWeatherForNewCity(city);
        } else {
            getWeatherForCurrentLocation();
        }
        Log.d("Clima", "Getting weather for the current location");

    }

    // TODO: Add getWeatherForNewCity(String city) here:
    private void getWeatherForNewCity(String city) {
        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", APP_ID);
        letsDoSomeNetworking(params);
    }

    // TODO: Add getWeatherForCurrentLocation() here:
    private void getWeatherForCurrentLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Clima", "onLocationChanged() called");
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());
                Log.d("Clima", "Longitude is: " + longitude);
                Log.d("Clima", "Latitude is: " + latitude);
                RequestParams params = new RequestParams();
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appid", APP_ID);
                letsDoSomeNetworking(params);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Clima", "onProviderDisabled() is callBack is received.");
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Clima", "onRequestPermission is granted.");
                getWeatherForCurrentLocation();
            }
            else {
                Log.d("Clima", "onRequestPermission is denied.");
            }
        }
    }

    // TODO: Add letsDoSomeNetworking(RequestParams params) here:
    private void letsDoSomeNetworking(RequestParams params) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                super.onSuccess(statusCode, headers, response);
                Log.d("Clima", " Success! JSON : " + response.toString());
                WeatherDataModel weatherDataModel = WeatherDataModel.fromJson(response);
                updateUI(weatherDataModel);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.e("Clima", "Fail " + errorResponse.toString());
                Log.d("Clima", "Status : " + statusCode);
                Toast.makeText(WeatherController.this, "Request Failed", Toast.LENGTH_SHORT).show();;
            }
        });
    }


    // TODO: Add updateUI() here:

    private void updateUI(WeatherDataModel weatherDataModel) {
        mTemperatureLabel.setText(weatherDataModel.getTemperature());
        mCityLabel.setText(weatherDataModel.getCity());
        mWeatherImage.setImageResource(getResources().getIdentifier(weatherDataModel.getIconName(), "drawable", getPackageName()));
    }

    // TODO: Add onPause() here:


    @Override
    protected void onPause() {
        super.onPause();
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }
}
