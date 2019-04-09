package socket;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import com.example.along.agv_controlpicture.Control_getPic;
import com.example.along.agv_controlpicture.MainActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Integer.valueOf;


public class TcpClient implements Runnable{
    private static TcpClient mTcpClient=null;
    private Bitmap mybitmap;
    private Socket socket;
    private int port;
    private String hostIP;
    private boolean connect = false;
    private boolean runFlag;
    protected DataInputStream in;
    protected DataOutputStream out;
    byte[] picLenBuff = new byte[4];

    private Handler handler =new Handler(Looper.getMainLooper());
    List<Byte>picList=new ArrayList<>();

    /**
     * 单例模式
     * @return
     */
    public static TcpClient getInstance(){
        if(mTcpClient==null){
            synchronized (TcpClient.class){
                if(mTcpClient==null){
                    mTcpClient=new TcpClient();
                }
            }
        }
        return mTcpClient;
    }

    public void connect(String hostIP, int port) {
        this.hostIP = hostIP;
        this.port = port;
        new Thread(this).start();
    }

    @Override
    public void run(){
        try {
            socket=new Socket(hostIP,port);
            runFlag=true;
            refreshUI(true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        in=new DataInputStream(socket.getInputStream());
                        out=new DataOutputStream(socket.getOutputStream());
                    }catch (IOException e){
                        e.printStackTrace();
                        runFlag=false;
                    }

                    while (runFlag) {
                        /**
                         * 如果按下跳转键 就开始传输图像+遥控
                         */
                        if(MainActivity.isReceive_flag()) {
                            ctl_showpic();
                        }
                    }

                    try {
                        in.close();
                        out.close();
                        socket.close();

                        in = null;
                        out = null;
                        socket = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    connect = false;
                    refreshUI(false);
                }
            }).start();
            connect=true;
        }catch (Exception e){
            e.printStackTrace();

        }
    }

    public void stop() {
        runFlag = false;
        try {
            socket.shutdownInput();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return connect;
    }

    public void send(String s) {
        if (out != null) {
            try {
                Log.i("发送函数","开始");
                OutputStream outputStream=socket.getOutputStream();
                outputStream.write(s.getBytes());
                Log.i("发送函数","成功");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshUI(final boolean isConnected){
        handler.post(new Runnable() {
            @Override
            public void run() {
                MainActivity.getEdPort().setEnabled(!isConnected);  //端口
                MainActivity.getEdIp().setEnabled(!isConnected);
                MainActivity.getBnConnect().setText(isConnected?"断开":"连接");
            }
        });
    }
    public static int byteArrayToInt(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }
    public static byte[] intToByteArray(int a) {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    private void ctl_showpic(){
        try {
            /**
             * 发送传输图像指令
             */
            String send_pic = "AA00000";
            out.write(send_pic.getBytes());
            out.flush();
            /**
             * 获取相关信息
             */
            in.read(picLenBuff, 0, 4);
            int getPicLen = byteArrayToInt(Arrays.copyOfRange(picLenBuff, 0, 4));  // 图像数组大小
            System.out.println("图像数组大小：" + getPicLen);

            /**
             * 接收图像
             */
            int offset = 0;
            byte[] bitmapbuff = new byte[getPicLen];
            while (offset < getPicLen) {
                int picL = in.read(bitmapbuff, offset, getPicLen - offset);
                offset += picL;
            }
            mybitmap = BitmapFactory.decodeByteArray(bitmapbuff, 0, offset);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Control_getPic.getImageView().setImageBitmap(mybitmap);
                    Log.i("显示图像", "ing");
                }
            });

        } catch (IOException e) {
            runFlag = false;
        }
    }


}