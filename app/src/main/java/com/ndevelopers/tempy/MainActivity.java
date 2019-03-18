package com.ndevelopers.tempy;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ndevelopers.tempy.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather currentWeather;
    private ImageView iconImageView;

    final double latitude = 37.8267;
    final double longitude = 122.4233;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getForecast(latitude, longitude);

        Log.d(TAG, "Main UI code is running, hurra!");

    }

    private void getForecast(double latitude, double longitude) {
        final ActivityMainBinding binding = DataBindingUtil.setContentView(MainActivity.this,
                R.layout.activity_main);

        TextView darkSky = findViewById(R.id.darkSkyAttribution);
        TextView dark = findViewById(R.id.darkSkyAttribution);

        darkSky.setMovementMethod(LinkMovementMethod.getInstance()); //hace funcionar el enlace

        iconImageView = findViewById(R.id.iconImageView);

        String apiKey = "683c6948e70da33639b4750934accc0d";
        String forecastURL = "https://api.darksky.net/forecast/" + apiKey + "/" + latitude + "," + longitude;

        if (isNetworkAvailable()) {


            OkHttpClient client = new OkHttpClient(); //instanciando okhttp

            Request request = new Request.Builder()
                    .url(forecastURL)
                    .build(); //  realizando una peticion por medio de la url

            Call call = client.newCall(request); //preparando la llamada de la peticion

            call.enqueue(new Callback() { //callback que permite saber la respuesta a la llamada y el fallo
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        //Response response = call.execute(); // metodo sincrono
                        String jsonData = response.body().string(); // JSON que se encuentra en la URL
                        Log.v(TAG, jsonData);

                        if (response.isSuccessful()) {

                            currentWeather = getCurrentDetails(jsonData);

                            final CurrentWeather displayWeather = new CurrentWeather(

                                        currentWeather.getLocationLabel(),
                                        currentWeather.getIcon(),
                                        currentWeather.getTime(),
                                        currentWeather.getTemperature(),
                                        currentWeather.getHumidity(),
                                        currentWeather.getPrecipChance(),
                                        currentWeather.getSummary(),
                                        currentWeather.getTimeZone()

                            );

                            binding.setWeather(displayWeather);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Drawable drawable = getResources().getDrawable(displayWeather.getIconId());
                                    iconImageView.setImageDrawable(drawable);
                                }
                            });



                        } else {
                            alertUserAboutError(); // metodo de alerta
                        }
                    } catch (IOException e) { // Obtiene el error del response
                        Log.e(TAG, "IO Exception Caught: ", e);
                    } catch (JSONException e){ //Obtiene el error del JSON EXCEPTION
                        Log.e(TAG, "JSONException caught", e);
                    }
                }
            });
        }
    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException{

        JSONObject forecast = new JSONObject(jsonData); //objeto JSON

        String timezone = forecast.getString("timezone"); // obtiene la propiedad el objeto JSON
        Log.i(TAG,"JSON" + timezone);

        JSONObject currently = forecast.getJSONObject("currently"); //objeto dentro de un objeto
        //Log.i(TAG, "quepedotasdaes" + currently);

        CurrentWeather currentWeather = new CurrentWeather();

        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setLocationLabel("Alcatraz Island, CA");
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));

        currentWeather.setTimeZone(timezone);

        Log.d(TAG, currentWeather.getFormattedTime());

        return currentWeather;

                    /*try {
                        JSONObject forecast = new JSONObject(jsonData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }*/

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;

        if(networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;

        }else{
            Toast.makeText(this, getString(R.string.network_unavailable_message), Toast.LENGTH_SHORT).show();
        }

        return isAvailable;
    }

    private void alertUserAboutError() {

        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");

    }

    public void refreshOnClick(View view){
        Toast.makeText(this, "Refreshing data", Toast.LENGTH_SHORT).show();
        getForecast(latitude, longitude);
    }

}
