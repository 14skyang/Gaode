package com.ysk.gaode;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.idst.nls.internal.utils.L;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.List;


import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
//此程序其实并不需要key和lib包
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";
    TextView destination;
    TextView responseText;
    TextView Latitude ;
    TextView Longitude;
    //经纬度
    private String location;
    private String latitude;//纬度
    private  String longitude;//经度
    private String dizhi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        responseText=(TextView)findViewById(R.id.response_text);
        Latitude=(TextView)findViewById(R.id.latitude);
        Longitude=(TextView)findViewById(R.id.longitude);
        destination=(TextView)findViewById(R.id.dizhi);
        Button sendrequest=(Button)findViewById(R.id.bt);
        Button daohang=(Button)findViewById(R.id.bt_daohang);
        sendrequest.setOnClickListener(this);
        daohang.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.bt){
            dizhi=destination.getText().toString().trim();//此处地址可以说建筑名字
            if (dizhi.equals("")){
                Toast.makeText(this, "地址不能为空", Toast.LENGTH_SHORT).show();
            }
            else  {
                HttpUtil.sendOkHttpRequest("https://restapi.amap.com/v3/geocode/geo?address=" + dizhi + "&key=2d7ad20513168e8d162b70eee75de8b5", new okhttp3.Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        //得到服务器返回的具体内容
                        String responseData = response.body().string();

                        Log.e(TAG, "responseData=======" + responseData);
                        parseJSONWithGSON(responseData);
                        showResponse(responseData);

                    }

                    @Override
                    public void onFailure(Call call, IOException e) {

                    }
                });
            }
        }
        if (v.getId()==R.id.bt_daohang){
            setUpGaodeAppByMine();
        }
    }

    private void showResponse(final String response){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //UI操作
                responseText.setText(response);
                Longitude.setText(longitude);
                Latitude.setText(latitude);
            }
        });
    }
    private void parseJSONWithGSON(String jsonData){
        Gson gson =new Gson();
        Location jdata=gson.fromJson(jsonData,Location .class);//解析掉第一层
        List<GeocodesBean> beanList= jdata .getGeocodes();//getGeocodes()得到的就是一个数组[]，封装为list
        Log.e("TAG","location:"+beanList.get(0).getLocation());//list的第一个（即序号0）的数据中就含有location
        location=beanList.get(0).getLocation();
        longitude=location.substring(0,location.indexOf(","));//截取","前的字符串，经度
        latitude=location.substring(location.indexOf(",")+1,location.length());//截取","后的字符串，纬度
    }

    void setUpGaodeAppByMine(){
        try {
            Intent intent = Intent.getIntentOld("androidamap://route?sourceApplication=softname&sname=我的位置&dlat="+latitude+"&dlon="+longitude+"&dname="+dizhi+"&dev=0&m=0&t=0");//其中t=0是驾车，
            //直接写入参数 我的位置 就可以了，这样进入高德或者百度地图app中直接就给定位了获取到当前位置了。不用在自己的项目中通过sdk获取到当前位置的坐标然后传入参数
            if(isInstallByread("com.autonavi.minimap")){
                startActivity(intent);
                Log.e(TAG, "高德地图客户端已经安装") ;
            }else {
                Log.e(TAG, "没有安装高德地图客户端") ;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    /**
     * 判断是否安装目标应用
     * @param packageName 目标应用安装后的包名
     * @return 是否已安装目标应用
     */
    private boolean isInstallByread(String packageName) {
        return new File("/data/data/" + packageName).exists();
    }
}
