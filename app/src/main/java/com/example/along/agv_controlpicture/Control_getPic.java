package com.example.along.agv_controlpicture;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import socket.TcpClient;
import com.kongqw.rockerlibrary.view.RockerView;

public class Control_getPic extends Activity{// implements View.OnClickListener

    private RockerView rockerViewer;
    private static ImageView c_imageView;
    private static Button bnBegin;
    private static boolean begin2send_flag=false;
    private TextView mLogLeft;           //摇杆

    private TcpClient client=TcpClient.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_get_pic);

        //***接收图像***//
        c_imageView=this.findViewById(R.id.c_image_View);

        //***监听***//
        //bnBegin.setOnClickListener(this);

        mLogLeft = findViewById(R.id.log_left);
        rockerViewer = findViewById(R.id.rockerView);//定义摇杆*/
        control_rock();

    }

    /**
     * 摇杆操作
     */
    public void control_rock() {

            if (rockerViewer != null) {
                rockerViewer.setCallBackMode(RockerView.CallBackMode.CALL_BACK_MODE_STATE_CHANGE);
                rockerViewer.setOnShakeListener(RockerView.DirectionMode.DIRECTION_8, new RockerView.OnShakeListener() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void direction(RockerView.Direction direction) {
                        if (client.isConnected()) {
                            mLogLeft.setText(getDirection(direction));
                        }
                    }

                    @Override
                    public void onFinish() {
                        mLogLeft.setText("");
                        sendmes("S");
                    }
                });
            }
    }



    private String getDirection(RockerView.Direction direction) {
        String message = null;
        switch (direction) {
            case DIRECTION_LEFT:
                message = "左";
                sendmes("L");
                break;
            case DIRECTION_RIGHT:
                message = "右";
                sendmes("R");
                break;
            case DIRECTION_UP:
                message = "上";
                sendmes("U");
                break;
            case DIRECTION_DOWN:
                message = "下";
                sendmes("D");
                break;
            default:
                break;
        }
        return message;
    }


    public static ImageView getImageView() {
        return c_imageView;
    }
    /**
     * 摇杆方向
     */
    private void sendmes(final String mes) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String data = "CT"+"FF"+mes+"EF";
                    client.send(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static boolean isBegin2send_flag() {
        return begin2send_flag;
    }
}
