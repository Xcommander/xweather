package com.example.xulinchao.xweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by xulinchao on 2017/5/30.
 */

public class Weather {
    public String staus;
    public Basic basic;
    public Aqi aqi;
    public Now now;
    public Suggestion suggestion;
    @SerializedName("daily_forecast")
    List<Forecast> forecastList;

}