package com.example.xulinchao.xweather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xulinchao.xweather.db.City;
import com.example.xulinchao.xweather.db.County;
import com.example.xulinchao.xweather.db.Province;
import com.example.xulinchao.xweather.util.HttpUtil;
import com.example.xulinchao.xweather.util.MyApplication;
import com.example.xulinchao.xweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by xulinchao on 2017/5/29.
 */

public class ChooseAreaFragment extends Fragment {
    /**
     * 用来标记当前页面
     **/
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private int currentLevel;
    /**
     * 所用到的控件
     **/
    private TextView textView;
    private ListView listView;
    private Button button;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    /**
     * 选中的省市，以及省市县区的列表--从数据库中读取
     **/
    private Province selectedProvince;
    private City selectedCity;
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    /**
     * 加载的dialog
     **/
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        textView = (TextView) (view).findViewById(R.id.title_text);
        button = (Button) (view).findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(MyApplication.getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener((parent,view, position,id)->{
            if (currentLevel == LEVEL_PROVINCE) {
                selectedProvince = provinceList.get(position);
                queryCites();
            } else if (currentLevel == LEVEL_CITY) {
                selectedCity = cityList.get(position);
                queryCounties();
            }
        });

        button.setOnClickListener((v)->{
            if (currentLevel == LEVEL_COUNTY) {
                queryCites();
            } else if (currentLevel == LEVEL_CITY) {
                queryProvinces();
            }
        });
        queryProvinces();
    }

    /**
     * 查询各省的数据
     **/
    public void queryProvinces() {
        button.setVisibility(View.GONE);
        textView.setText("中国");
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList
                    ) {
                dataList.add(province.getProvinceName());

            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");

        }

    }

    /**
     * 查询各个市的信息
     **/
    public void queryCites() {
        textView.setText(selectedProvince.getProvinceName());
        button.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceId = ?", String.valueOf(selectedProvince.getProvinceCode())).find(City
                .class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City c :
                    cityList) {
                dataList.add(c.getCityName());

            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceId = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceId;
            queryFromServer(address, "city");

        }

    }

    /**
     * 查询各个县的信息
     **/
    public void queryCounties() {
        textView.setText(selectedCity.getCityName());
        button.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityId = ?", String.valueOf(selectedCity.getCityCode())).find(
                County.class
        );
        if (countyList.size() > 0) {
            dataList.clear();
            for (County c :
                    countyList) {
                dataList.add(c.getCountyName());

            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;

        } else {
            int provinceId = selectedProvince.getProvinceCode();
            int cityId = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceId + "/" + cityId;
            queryFromServer(address, "county");

        }


    }

    /**
     * 从服务器上查询
     **/
    public void queryFromServer(String address, String type) {
        showDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> {
                    closeDialog();
                    Toast.makeText(getActivity(), "从服务器加载失败！", Toast.LENGTH_SHORT).show();

                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(data);

                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(data, selectedProvince.getProvinceCode());

                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(data, selectedCity.getCityCode());

                }
                if (result) {
                    getActivity().runOnUiThread(() -> {
                        closeDialog();
                        if ("province".equals(type)) {
                            queryProvinces();

                        } else if ("city".equals(type)) {
                            queryCites();

                        } else if ("county".equals(type)) {
                            queryCounties();

                        }

                    });
                } else {
                    getActivity().runOnUiThread(() -> {
                        closeDialog();
                        Toast.makeText(getActivity(), "从服务器加载失败！", Toast.LENGTH_SHORT).show();

                    });
                }

            }
        });
    }

    /**
     * 加载进度条
     **/
    public void showDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载.....");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭加载框
     **/
    public void closeDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }


}
