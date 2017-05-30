package com.example.xulinchao.xweather.util;

import android.text.TextUtils;

import com.example.xulinchao.xweather.db.City;
import com.example.xulinchao.xweather.db.County;
import com.example.xulinchao.xweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xulinchao on 2017/5/28.
 */

public class Utility {
    /**
     * 解析和返回各省的数据
     **/
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject j = jsonArray.getJSONObject(i);
                    Province p = new Province();
                    p.setProvinceCode(j.getInt("id"));
                    p.setProvinceName(j.getString("name"));
                    p.save();

                }
                return true;

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    /**
     * 解析和返回全省的市级数据
     **/
    public static boolean handleCityResponse(String response, int ProvinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    City city = new City();
                    city.setCityCode(jsonObject.getInt("id"));
                    city.setCityName(jsonObject.getString("name"));
                    city.setProvinceId(ProvinceId);
                    city.save();


                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
        return false;

    }

    /**
     * 解析和处理服务器返回来的数据
     **/
    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    County county = new County();
                    county.setWeatherId(jsonObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.setCountyName(jsonObject.getString("name"));
                    county.save();


                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


}
