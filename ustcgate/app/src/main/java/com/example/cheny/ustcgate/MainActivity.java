package com.example.cheny.ustcgate;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.net.ConnectivityManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private EditText idEdit,pwEdit;
    private Button ConButton;
    private TextView ResponseText;
    private URL url;
    private HttpURLConnection conn;
    private InputStream is;
    private OutputStream os;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        idEdit = (EditText)findViewById(R.id.IDeditText);
        pwEdit = (EditText)findViewById(R.id.PWeditText);
        ConButton = (Button)findViewById(R.id.Connectbutton);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED){
            //申请INTERNET权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 0);
        }
        ConButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wltLogin();
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
    }
    public void wltLogin(){
        // TODO Auto-generated method stub
        String urlData="http://wlt.ustc.edu.cn/cgi-bin/ip";
        try{
            url = new URL(urlData);
        }catch (MalformedURLException Me){
            Me.printStackTrace();
        }
        try {
            //打开服务器
            conn=(HttpURLConnection) url.openConnection();
            //设置输入输出流
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //设置请求的方法为Post
            conn.setRequestMethod("POST");
            //Post方式不能缓存数据，则需要手动设置使用缓存的值为false
            conn.setUseCaches(false);
            //连接数据库
            conn.connect();
            /**写入参数**/
            os = conn.getOutputStream();
            //封装写给服务器的数据（这里是要传递的参数）
            DataOutputStream dos=new DataOutputStream(os);
            //写方法：name是key值不能变，编码方式使用UTF-8可以用中文
            dos.writeBytes("name="+ URLEncoder.encode(idEdit.getText().toString(), "UTF-8"));
            //关闭外包装流
            dos.close();
            /**读服务器数据**/
            is=conn.getInputStream();
            BufferedReader br=new BufferedReader(new InputStreamReader(is));
            String line=null;
            StringBuffer sb=new StringBuffer();
            while((line=br.readLine())!=null){
                sb.append(line);
            }
            ResponseText.setText(sb.toString());
            System.out.println(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}