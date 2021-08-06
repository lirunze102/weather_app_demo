package com.example.test;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.test.adapter.AreaAdapter;
import com.example.test.adapter.CityAdapter;
import com.example.test.adapter.ProvinceAdapter;
import com.example.test.aqi.AirQualityData;
import com.example.test.bean.CityResponse;
import com.example.test.data.WeatherApiUtil;
import com.example.test.data.WeatherNow;
import com.example.test.forecast.DailyForecast;
import com.example.test.forecast.WeatherForecast;
import com.example.test.utils.LiWindow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.test.utils.RecyclerViewAnimation.runLayoutAnimationRight;

public class MainActivity extends AppCompatActivity {
    private List<String> list;//字符串列表
    private List<CityResponse> provinceList;//省列表数据
    private List<CityResponse.CityBean> citylist;//市列表数据
    private List<CityResponse.CityBean.AreaBean> arealist;//区/县列表数据
    ProvinceAdapter provinceAdapter;//省数据适配器
    CityAdapter cityAdapter;//市数据适配器
    AreaAdapter areaAdapter;//县/区数据适配器
    String provinceTitle;//标题
    LiWindow liWindow;//自定义弹窗
    private Context context;
    AtomicInteger requestCount=new AtomicInteger(0);

    SwipeRefreshLayout swipeRefreshLayout;
    private static final String KEY_WEATHER_ID="weather_id";
    private static final String KEY_AQI_ID="aqi_id";
    private static final String KEY_DATE_ID="now_date";
    String weather_id = "大兴区";
    String aqi_id = "北京市";
    String now_date="2021-04-21";

    TextView tv_city, tv_update_time,tv_parent, tv_temp, tv_temp2, tv_temp3, tv_weather_info;
    ImageView iv,iv2,iv_cond;
    LinearLayout forecastLayout;
    TextView tv_aqi,tv_pm25;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather);
        context=getApplicationContext();
        tv_city = findViewById(R.id.title_city_tv);
        tv_update_time = findViewById(R.id.title_pub_time_tv);
        tv_parent=findViewById(R.id.title_parent);
        tv_temp = findViewById(R.id.now_temp_tv);
        tv_temp2 = findViewById(R.id.now_tmp_tv2);
        tv_temp3 = findViewById(R.id.now_tmp_tv3);
        tv_weather_info = findViewById(R.id.now_cond_tv);
        iv_cond=findViewById(R.id.now_cond_iv);
        tv_aqi=findViewById(R.id.aqi_text);
        tv_pm25=findViewById(R.id.pm25_text);
        forecastLayout= (LinearLayout) findViewById(R.id.forecast_layout);


        iv=findViewById(R.id.city_select);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCityWindow();
            }
        });
        iv2=findViewById(R.id.city_select2);
        iv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context,MapActivity.class));
            }
        });

        swipeRefreshLayout= (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateData();
            }
        });

        loadWeatherId();
        updateData();
    }


    private void loadWeatherId() {
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        weather_id=sharedPreferences.getString(KEY_WEATHER_ID,"大兴区");
        aqi_id=sharedPreferences.getString(KEY_AQI_ID,"北京市");
        now_date=sharedPreferences.getString(KEY_DATE_ID,"2021-04-21");
    }

    private void saveWeatherId() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_WEATHER_ID,weather_id);
        editor.putString(KEY_AQI_ID,aqi_id);
        editor.apply();// async save;
    }
    private void savedate() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_DATE_ID,now_date);
        editor.apply();// async save;
    }

    private void updateRefreshState() {
        if (requestCount.incrementAndGet() == 3) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void updateWeatherIcon(String cond_code, ImageView iv_cond)
    {
        String url =
                String.format("https://cdn.heweather.com/cond_icon/%s.png",
                        cond_code);
        Glide.with(this).load(Uri.parse(url)).into(iv_cond);
    }

    private void updateWeatherNow() {
        WeatherApiUtil.getWeatherNow(this, weather_id, new WeatherApiUtil.OnWeatherNowFinished() {
            @Override
            public void onFinished(WeatherNow data) {
                if(data!=null){
                    tv_city.setText(weather_id);
                    String str1,str2;
                    String str=data.update.loc;
                    str1=str.substring(11);
                    tv_update_time.setText("上次更新时间：" +str1);
                    str2=str.substring(0,10);
                    now_date=str2;
                    savedate(); 
                    tv_parent.setText(aqi_id);
                    tv_temp.setText(data.now.tmp+"℃");
                    tv_weather_info.setText(data.now.cond_txt);
                    updateWeatherIcon(data.now.cond_code,iv_cond);
                    iv_cond.setColorFilter(getResources().getColor(R.color.colorLight));
                }
                updateRefreshState();
            }

        });
    }

    private void updateWeatherForecast() {
        WeatherApiUtil.getWeatherForecast(this, weather_id, new WeatherApiUtil.OnWeatherForecastFinished() {
            @Override
            public void onFinished(WeatherForecast data) {
                if(data!=null){
                    forecastLayout.removeAllViews();
                    List<DailyForecast> forecastList = data.dailyForecastList;
                    for (int i = 0; i < forecastList.size(); i++) {
                        DailyForecast f = forecastList.get(i);
                        View v = LayoutInflater.from(context).inflate(R.layout.forecast_item, null, false);
                        TextView item_date_text=v.findViewById(R.id.item_date_text);
                        TextView item_max_text=v.findViewById(R.id.item_max_text);
                        TextView item_min_text=v.findViewById(R.id.item_min_text);
                        ImageView item_iv_day_con=v.findViewById(R.id.item_iv_day_con);
                        item_iv_day_con.setColorFilter(getResources().getColor(R.color.colorLight));
                        ImageView item_iv_night_con=v.findViewById(R.id.item_iv_night_con);
                        item_iv_night_con.setColorFilter(getResources().getColor(R.color.colorLight));
                        item_date_text.setText(f.date);
                        item_max_text.setText(f.tmp_max+"℃");
                        item_min_text.setText(f.tmp_min+"℃");
                        updateWeatherIcon(f.cond_code_d,item_iv_day_con);
                        updateWeatherIcon(f.cond_code_n,item_iv_night_con);
                        forecastLayout.addView(v);
                        if(f.date==now_date.substring(0,10)){
                            tv_temp2.setText(f.tmp_max+"℃");
                            tv_temp3.setText(" / "+f.tmp_min+"℃");
                        }
                    }
                }
                updateRefreshState();
            }
        });
    }
    private void updateWeatherAqi(){
        WeatherApiUtil.getAirQualityData(this, aqi_id, new WeatherApiUtil.OnAirQualityFinished() {
            @Override
            public void onFinished(AirQualityData data) {
                if(data!=null&&data.status.equalsIgnoreCase("ok")){
                    tv_aqi.setText(data.airNowCity.aqi);
                    tv_pm25.setText(data.airNowCity.pm25);
                }else{
                    tv_aqi.setText("--");
                    tv_pm25.setText("--");
                }
                updateRefreshState();
            }
        });
    }

    private void updateData() {
        swipeRefreshLayout.setRefreshing(true);
        requestCount.set(0);
//        Toast.makeText(MainActivity.this,"刷新",Toast.LENGTH_SHORT).show();
        updateWeatherNow();
        updateWeatherForecast();
        updateWeatherAqi();
    }



    private void showCityWindow() {
        provinceList = new ArrayList<>();
        citylist = new ArrayList<>();
        arealist = new ArrayList<>();
        list = new ArrayList<>();
        liWindow = new LiWindow(MainActivity.this);
        final View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.weather_city_list, null);
        ImageView areaBack = (ImageView) view.findViewById(R.id.iv_back_area);
        ImageView cityBack = (ImageView) view.findViewById(R.id.iv_back_city);
        TextView windowTitle = (TextView) view.findViewById(R.id.tv_title);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv);
        liWindow.showRightPopupWindow(view);
        initCityData(recyclerView,areaBack,cityBack,windowTitle);//加载城市列表数据
    }


    //点击事件
    private void initCityData(final RecyclerView recyclerView, final ImageView areaBack, final ImageView cityBack, final TextView windowTitle) {
        //初始化省数据 读取省数据并显示到列表中
        try {
            InputStream inputStream = getResources().getAssets().open("city.txt");//读取数据
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer = new StringBuffer();
            String lines = bufferedReader.readLine();
            while (lines != null) {
                stringBuffer.append(lines);
                lines = bufferedReader.readLine();
            }

            final JSONArray Data = new JSONArray(stringBuffer.toString());
            //循环这个文件数组、获取数组中每个省对象的名字
            for (int i = 0; i < Data.length(); i++) {
                JSONObject provinceJsonObject = Data.getJSONObject(i);
                String provinceName = provinceJsonObject.getString("name");
                CityResponse response = new CityResponse();
                response.setName(provinceName);
                provinceList.add(response);
            }

            //定义省份显示适配器
            provinceAdapter = new ProvinceAdapter(R.layout.item_city_list, provinceList);
            LinearLayoutManager manager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(manager);
            recyclerView.setAdapter(provinceAdapter);
            provinceAdapter.notifyDataSetChanged();
            runLayoutAnimationRight(recyclerView);//动画展示
            provinceAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                @Override
                public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                    try {
                        //返回上一级数据
                        cityBack.setVisibility(View.VISIBLE);
                        cityBack.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                recyclerView.setAdapter(provinceAdapter);
                                provinceAdapter.notifyDataSetChanged();
                                cityBack.setVisibility(View.GONE);
                                windowTitle.setText("中国");
                            }
                        });

                        //根据当前位置的省份所在的数组位置、获取城市的数组
                        JSONObject provinceObject = Data.getJSONObject(position);
                        windowTitle.setText(provinceList.get(position).getName());
                        provinceTitle = provinceList.get(position).getName();
                        final JSONArray cityArray = provinceObject.getJSONArray("city");

                        //更新列表数据
                        if (citylist != null) {
                            citylist.clear();
                        }

                        for (int i = 0; i < cityArray.length(); i++) {
                            JSONObject cityObj = cityArray.getJSONObject(i);
                            String cityName = cityObj.getString("name");
                            CityResponse.CityBean response = new CityResponse.CityBean();
                            response.setName(cityName);
                            citylist.add(response);
                        }

                        cityAdapter = new CityAdapter(R.layout.item_city_list, citylist);
                        LinearLayoutManager manager1 = new LinearLayoutManager(context);
                        recyclerView.setLayoutManager(manager1);
                        recyclerView.setAdapter(cityAdapter);
                        cityAdapter.notifyDataSetChanged();
                        runLayoutAnimationRight(recyclerView);

                        cityAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                            @Override
                            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                                aqi_id=citylist.get(position).getName();
                                try {
                                    //返回上一级数据
                                    areaBack.setVisibility(View.VISIBLE);
                                    areaBack.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            recyclerView.setAdapter(cityAdapter);
                                            cityAdapter.notifyDataSetChanged();
                                            areaBack.setVisibility(View.GONE);
                                            windowTitle.setText(provinceTitle);
                                            arealist.clear();
                                        }
                                    });
                                    //根据当前城市数组位置 获取地区数据
                                    windowTitle.setText(citylist.get(position).getName());
                                    JSONObject cityJsonObj = cityArray.getJSONObject(position);
                                    JSONArray areaJsonArray = cityJsonObj.getJSONArray("area");
                                    if (arealist != null) {
                                        arealist.clear();
                                    }
                                    if(list != null){
                                        list.clear();
                                    }
                                    for (int i = 0; i < areaJsonArray.length(); i++) {
                                        list.add(areaJsonArray.getString(i));
                                    }
                                    Log.i("list", list.toString());
                                    for (int j = 0; j < list.size(); j++) {
                                        CityResponse.CityBean.AreaBean response = new CityResponse.CityBean.AreaBean();
                                        response.setName(list.get(j).toString());
                                        arealist.add(response);
                                    }
                                    areaAdapter = new AreaAdapter(R.layout.item_city_list, arealist);
                                    LinearLayoutManager manager2 = new LinearLayoutManager(context);

                                    recyclerView.setLayoutManager(manager2);
                                    recyclerView.setAdapter(areaAdapter);
                                    areaAdapter.notifyDataSetChanged();
                                    runLayoutAnimationRight(recyclerView);

                                    areaAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                                        @Override
                                        public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {

                                            Toast.makeText(MainActivity.this,arealist.get(position).getName(),Toast.LENGTH_SHORT).show();
                                            weather_id=arealist.get(position).getName();

                                            saveWeatherId();
                                            liWindow.closePopupWindow();
                                            updateData();
                                        }
                                    });


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }


}
