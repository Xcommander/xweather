package com.example.xulinchao.xweather;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.UiThread;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.xulinchao.xweather.gson.Forecast;
import com.example.xulinchao.xweather.gson.Weather;
import com.example.xulinchao.xweather.util.HttpUtil;
import com.example.xulinchao.xweather.util.MyApplication;
import com.example.xulinchao.xweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weatherLayout;
    /**
     * 当前的城市和时间,basic.xml
     **/
    private TextView titleCity;
    private TextView titleUpdateTime;
    /**
     * 当前温度和天气情况,now.xml
     **/
    private TextView degree;
    private TextView weatherInfo;
    /**
     * 未来几天的预报,forecast.xml
     **/
    private LinearLayout forecastLayout;
    private TextView forecastDate;
    private TextView forecastInfo;
    private TextView forecastMax;
    private TextView forecastMin;
    /**
     * 空气质量, aqi.xml
     **/
    private TextView aqiText;
    private TextView pm25Text;

    /**
     * 生活建议, suggestion.xml
     **/

    private TextView comfort;
    private TextView carWash;
    private TextView sport;

    /**
     * 背景图片
     **/
    private ImageView imageView;

    /**
     * 刷新天气
     **/
    public SwipeRefreshLayout refreshWeather;

    /**
     * 获取drawerlayout控件，用来左右
     * **/
    public DrawerLayout drawerLayout;
    private Button NvButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String weatherCityId;
        super.onCreate(savedInstanceState);
        /**
         * 适配状态栏
         * **/
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();

            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);
        /**
         * 初始化各种控件
         * **/
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degree = (TextView) findViewById(R.id.degree_text);
        weatherInfo = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfort = (TextView) findViewById(R.id.comfort_text);
        carWash = (TextView) findViewById(R.id.car_wash_text);
        sport = (TextView) findViewById(R.id.sport_text);

        imageView = (ImageView) findViewById(R.id.image_view);
        refreshWeather = (SwipeRefreshLayout) findViewById(R.id.refresh_weather);
        refreshWeather.setColorSchemeResources(R.color.colorPrimary);

        /**
         * 侧滑功能
         * **/
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        NvButton=(Button)findViewById(R.id.nav_button);
        NvButton.setOnClickListener(v -> {
            drawerLayout.openDrawer(Gravity.START);
        });


        /**
         * 获取默认的sharedPreference，判断是否有缓存，
         * 有的话，直接取出来，
         * 没有的话，从服务器获取
         * **/


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String bingPic = sharedPreferences.getString("bingPic", null);
        if (!TextUtils.isEmpty(bingPic)) {
            Glide.with(this).load(bingPic).into(imageView);

        } else {
            loadPic();
        }
        String weatherData = sharedPreferences.getString("weather", null);
        if (!TextUtils.isEmpty(weatherData)) {
            Weather weather = Utility.handleWeatherResopnse(weatherData);
            weatherCityId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            weatherCityId = weatherId;
            requestWeather(weatherId);


        }


        refreshWeather.setOnRefreshListener(() -> {
                    requestWeather(weatherCityId);

                }
        );


    }

    /**
     * 获取每日一图
     **/
    public void loadPic() {
        String address = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(WeatherActivity.this, "背景图片更新失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                SharedPreferences.Editor Editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity
                        .this).edit();
                Editor.putString("bingPic", data);
                Editor.apply();
                runOnUiThread(() -> {
                    Glide.with(WeatherActivity.this).load(data).into(imageView);
                });


            }
        });
    }

    /**
     * @param weather {@link Weather}
     **/

    public void showWeatherInfo(Weather weather) {
        titleCity.setText(weather.basic.cityName);
        titleUpdateTime.setText(weather.basic.update.updateTime.split(" ")[1]);
        degree.setText(weather.now.temperature + "℃");
        weatherInfo.setText(weather.now.more.info);
        forecastLayout.removeAllViews();
        for (Forecast f : weather.forecastList
                ) {
            View v = getLayoutInflater().inflate(R.layout.forecast_item, forecastLayout, false);
            forecastDate = (TextView) v.findViewById(R.id.date_text);
            forecastInfo = (TextView) v.findViewById(R.id.info_text);
            forecastMax = (TextView) v.findViewById(R.id.max_text);
            forecastMin = (TextView) v.findViewById(R.id.min_text);
            forecastDate.setText(f.date);
            forecastInfo.setText(f.more.info);
            forecastMax.setText(f.temperature.max);
            forecastMin.setText(f.temperature.min);
            forecastLayout.addView(v);

        }

        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }

        comfort.setText(weather.suggestion.comfort.info);
        carWash.setText(weather.suggestion.carWash.info);
        sport.setText(weather.suggestion.sport.info);
        weatherLayout.setVisibility(View.VISIBLE);


    }

    /**
     * 从服务器获取数据
     **/
    public void requestWeather(String weatherId) {
        String address = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=439e3a63914443bfaccc11dcba9c54b1";
        Log.e("xulinchao", "requestWeather: " + address);
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Log.e("xulinchao", "onFailure: ");
                    Toast.makeText(WeatherActivity.this,
                            "从服务器获取数据失败", Toast.LENGTH_SHORT).show();
                });


            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String data = response.body().string();
                final Weather weather = Utility.handleWeatherResopnse(data);
                runOnUiThread(() -> {
                    if (weather != null && "ok".equals(weather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity
                                .this).edit();
                        editor.putString("weather", data);
                        editor.apply();
                        showWeatherInfo(weather);
                    } else {
                        Toast.makeText(WeatherActivity.this, "从服务器获取天气失败....", Toast.LENGTH_SHORT)
                                .show();
                    }
                    refreshWeather.setRefreshing(false);
                });


            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
