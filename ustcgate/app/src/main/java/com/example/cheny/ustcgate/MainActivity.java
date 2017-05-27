package com.example.cheny.ustcgate;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText idEdit,pwEdit;
    private Button ConButton;
    private TextView ResponseText;
    private RadioGroup type,exp;
    private HttpGet httpGet;
    private HttpClient httpClient;
    private HttpPost httpPost;
    private HttpResponse Response;
    private String Cookie_rn;   //用来标识会话的Cookie
    private String userip;

    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    String responstr = (String)msg.obj;
                    ResponseText.setText(responstr);
                    break;
                default:
                    break;
            }
        }
    };

    private String intToIp(int i) {

        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        idEdit = (EditText)findViewById(R.id.IDeditText);
        pwEdit = (EditText)findViewById(R.id.PWeditText);
        ConButton = (Button)findViewById(R.id.Connectbutton);
        ResponseText = (TextView)findViewById(R.id.ResponseTextView);
        type = (RadioGroup)findViewById(R.id.OutRadioGroup);
        exp = (RadioGroup)findViewById(R.id.radioGroup);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED){
            //申请INTERNET权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 0);
        }
        ConButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new wltLogin().start();
            }
        });
        ConnectivityManager cm=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info=cm.getActiveNetworkInfo();
        if(info!=null){
            Toast.makeText(MainActivity.this, "连网正常"+info.getTypeName(), Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(MainActivity.this, "未连网", Toast.LENGTH_SHORT).show();
        }
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        userip = intToIp(ipAddress);
        //userip = "210.45.73.6";
        ResponseText.setText(userip);
    }
    private int gettype(int checkedid){
        switch (checkedid){
            case R.id.Outradio1:
                return 0;
            case R.id.Outradio2:
                return 1;
            case R.id.Outradio3:
                return 2;
            case R.id.Outradio4:
                return 3;
            case R.id.Outradio5:
                return 4;
            case R.id.Outradio6:
                return 5;
            case R.id.Outradio7:
                return 6;
            case R.id.Outradio8:
                return 7;
            case R.id.Outradio9:
                return 8;
            default:
                break;
        }
        return -1;
    }
    private int getexp(int checkedid){
        switch (checkedid){
            case R.id.Timeradio1:
                return 3600;
            case R.id.Timeradio2:
                return 14400;
            case R.id.Timeradio3:
                return 39600;
            case R.id.Timeradio4:
                return 50400;
            case R.id.Timeradio5:
                return 0;
            default:
                break;
        }
        return -1;
    }
    class wltLogin extends Thread{
        @Override
        public void run(){
            Looper.prepare();
            String userid = idEdit.getText().toString();
            String userpw = pwEdit.getText().toString();
            if(userid.equals("")){
                Toast.makeText(MainActivity.this,"请填写用户名",Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
            else if(userpw.equals("")){
                Toast.makeText(MainActivity.this,"请填写密码",Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
            else{
                /*登陆*/
                String urlstr = "http://wlt.ustc.edu.cn/cgi-bin/ip";
                httpPost = new HttpPost(urlstr);
                List<NameValuePair> params=new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("cmd","login"));
                params.add(new BasicNameValuePair("ip",userip));
                params.add(new BasicNameValuePair("url","URL"));
                params.add(new BasicNameValuePair("name",userid));
                params.add(new BasicNameValuePair("password",userpw));
                params.add(new BasicNameValuePair("go","��¼�ʻ�"));
                try{
                    httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                }catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }
                httpClient = new DefaultHttpClient();
                try{
                    Response = httpClient.execute(httpPost);
                }catch (IOException e){
                    e.printStackTrace();
                }
                if(Response.getStatusLine().getStatusCode() == 200){
                    Header[] Cookie = Response.getHeaders("Set-Cookie");
                    if(Cookie.length == 0){
                        Toast.makeText(MainActivity.this,"用户名或密码不正确",Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        return;
                    }
                    Cookie_rn = Cookie[0].getValue();
                    Message msg = new Message();
                    msg.obj = String.valueOf(Response.getStatusLine().getStatusCode());
                    handler.sendMessage(msg);
                }
                else {
                    Message msg = new Message();
                    msg.obj = String.valueOf("登陆失败");
                    handler.sendMessage(msg);
                }

            /*设置*/
                urlstr = "http://wlt.ustc.edu.cn/cgi-bin/ip" +
                        "?cmd=set&url=URL&type=" + String.valueOf(gettype(type.getCheckedRadioButtonId()))
                        + "&exp=" + String.valueOf(getexp(exp.getCheckedRadioButtonId()))
                        + "&go=+%BF%AA%CD%A8%CD%F8%C2%E7+";
                httpGet = new HttpGet(urlstr);
                httpGet.setHeader("Cookie", Cookie_rn);
                httpClient = new DefaultHttpClient();
                try{
                    Response = httpClient.execute(httpGet);
                }
                catch (IOException e){
                    e.printStackTrace();
                }
                if(Response.getStatusLine().getStatusCode() == 200){
                    Message msg = new Message();
                    msg.obj = "设置成功";
                    handler.sendMessage(msg);
                }
                else{
                    Message msg = new Message();
                    msg.obj = "设置失败";
                    handler.sendMessage(msg);
                }

            /*退出*/
                urlstr = "http://wlt.ustc.edu.cn/cgi-bin/ip?cmd=logout";
                httpGet = new HttpGet(urlstr);
                httpGet.setHeader("Cookie", Cookie_rn);
                httpClient = new DefaultHttpClient();
                try{
                    Response = httpClient.execute(httpGet);
                }
                catch (IOException e){
                    e.printStackTrace();
                }
                if(Response.getStatusLine().getStatusCode() == 200){
                    Message msg = new Message();
                    msg.obj = "退出成功";
                    handler.sendMessage(msg);
                }
                else{
                    Message msg = new Message();
                    msg.obj = "退出失败";
                    handler.sendMessage(msg);
                }
                Message msg = new Message();
                msg.obj = "设置成功";
                handler.sendMessage(msg);
            }
        }
    }
}