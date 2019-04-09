package com.example.along.agv_controlpicture;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import socket.TcpClient;

public class MainActivity extends Activity implements View.OnClickListener{


    //***变量***//
    private static boolean receive_flag=false;
    //***连接ip、端口用***//
    private static Button bnConnect,bnSend;     //连接、发送
    private static EditText edIp,edPort,edData; //IP地址、端口号、输入数据 文本框
    //***跳转界面***//
    private static Button bnBuildPic;           //开始遥控并显示图像
    //***接收图像***//
    private static ImageView imageView;
    //***多线程***//
    private Handler handler =new Handler(Looper.getMainLooper());
    //***单例模式***//
    private TcpClient client=TcpClient.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //***连接ip、端口用***//
        bnConnect=this.findViewById(R.id.bn_connect);
        bnSend=this.findViewById(R.id.bn_send);
        edIp=this.findViewById(R.id.ed_ip);
        edPort=this.findViewById(R.id.ed_port);
        edData=this.findViewById(R.id.ed_dat);
        //***跳转界面***//
        bnBuildPic=this.findViewById(R.id.bn_buildPic);
        //***接收图像***//
        //imageView=this.findViewById(R.id.image_View);
        //***监听***//
        bnConnect.setOnClickListener(this);
        bnBuildPic.setOnClickListener(this);

        refreshUI(false);  //刷新界面
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.bn_connect:
                connect();
                //Intent getPic=new Intent(MainActivity.this,Control_getPic.class);
                //startActivity(getPic);
                break;
            case R.id.bn_buildPic:
                Intent getPic=new Intent(MainActivity.this,Control_getPic.class);
                receive_flag=true;
                startActivity(getPic);
                break;
        }
    }

    private void connect(){
        if(client.isConnected()){
            client.stop();
        } else {
            try {
                String hostIP = edIp.getText().toString();               //获取App里写的IP地址
                int port = Integer.parseInt(edPort.getText().toString());//获取APP里写的端口号，string->int
                client.connect(hostIP, port);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "端口错误", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void refreshUI(final boolean isConnected){
        handler.post(new Runnable() {
            @Override
            public void run() {
                edPort.setEnabled(!isConnected);  //端口
                edIp.setEnabled(!isConnected);
                bnConnect.setText(isConnected?"断开":"连接");
            }
        });
    }

    public static Button getBnConnect() {
        return bnConnect;
    }

    public static Button getBnSend() {
        return bnSend;
    }

    public static EditText getEdIp() {
        return edIp;
    }

    public static EditText getEdData() {
        return edData;
    }

    public static EditText getEdPort() {
        return edPort;
    }

    public static ImageView getImageView() {
        return imageView;
    }

    public static boolean isReceive_flag() {
        return receive_flag;
    }
}
