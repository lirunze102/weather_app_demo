package com.example.test.data;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.Toast;


import com.example.test.aqi.AirQualityData;
import com.example.test.forecast.WeatherForecast;
import com.example.test.utility.HttpUtil;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class WeatherApiUtil {
    public static final String API_KEY="c3b9910e2e8c42dd8a44edcb20fa17a1";// register an account and use your own key
    public interface OnWeatherNowFinished{ public void onFinished(WeatherNow data); }
    public static void getWeatherNow(final Activity activity, String weather_id, final OnWeatherNowFinished listener){
        String url=String.format("https://free-api.heweather.net/s6/weather/now?location=%s&key=%s",weather_id,API_KEY);
        HttpUtil.getOkHttpAsync(activity, url, new HttpUtil.SimpleAsyncCall() {
            @Override
            public void onFailure(String e) {
                showToast(activity,e);
                listener.onFinished(null);
            }
            @Override
            public void onResponse(String response) {
                if(!TextUtils.isEmpty(response)){
                    try {
                        JSONArray heWeather6 = new JSONObject(response).getJSONArray("HeWeather6");
                        String s= heWeather6.get(0).toString();
                        WeatherNow data = new Gson().fromJson(s, WeatherNow.class);
                        listener.onFinished(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onFinished(null);
                    }
                }else { listener.onFinished(null); }
            }
        });
    }
    private static void showToast(Activity activity,String s){
        Toast.makeText(activity,s,Toast.LENGTH_SHORT).show();}

    public interface OnWeatherForecastFinished{
        public void onFinished(WeatherForecast data);
    }
    public static void getWeatherForecast(final Activity activity, String weather_id, final OnWeatherForecastFinished listener){
        String url=String.format("https://free-api.heweather.net/s6/weather/forecast?location=%s&key=%s",weather_id,API_KEY);
        HttpUtil.getOkHttpAsync(activity, url, new HttpUtil.SimpleAsyncCall() {
            @Override
            public void onFailure(String e) {
                showToast
                        (activity,e);
                listener.onFinished(null);
            }
            @Override
            public void onResponse(String response) {
                if(!TextUtils.isEmpty(response)){
                    try {
                        JSONArray heWeather6 = new JSONObject(response).getJSONArray("HeWeather6");
                        String s= heWeather6.get(0).toString();
                        WeatherForecast data = new Gson().fromJson(s, WeatherForecast.class);
                        if(data!=null&&data.status.equalsIgnoreCase("ok")){
                            listener.onFinished(data);
                            return;
                        }
                    } catch (JSONException e) { e.printStackTrace(); }
                }
                listener.onFinished(null);
            }
        });
    }

    public interface OnAirQualityFinished{
        public void onFinished(AirQualityData data);
    }
    public static void getAirQualityData(final Activity activity, String aqi_id, final OnAirQualityFinished listener){
        String url=String.format("https://free-api.heweather.net/s6/air/now?location=%s&key=%s",aqi_id,API_KEY);
        HttpUtil.getOkHttpAsync(activity, url, new HttpUtil.SimpleAsyncCall() {
            @Override
            public void onFailure(String e) {
                showToast
                        (activity,e);
                listener.onFinished(null);
            }
            @Override
            public void onResponse(String response) {
                if(!TextUtils.isEmpty(response)){
                    try {
                        JSONArray heWeather6 = new JSONObject(response).getJSONArray("HeWeather6");
                        String s= heWeather6.get(0).toString();
                        AirQualityData data = new Gson().fromJson(s, AirQualityData.class);
                        if(data!=null&&data.status.equalsIgnoreCase("ok")){
                            listener.onFinished(data);
                            return;
                        }
                    } catch (JSONException e) {e.printStackTrace();}
                }
                listener.onFinished(null);
            }
        });
    }
}