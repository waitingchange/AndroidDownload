package com.example.change.myapplication;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    ProgressBar pb;
    TextView tv;
    ImageView imageView;
    int fileSize;
    int downLoadFileSize;
    String fileEx, fileNa, filename;

//
    String fileUrl = "http://s1.music.126.net/download/android/CloudMusic_official_3.7.3_153912.apk";
    String dirStr = Environment.getExternalStorageDirectory() + File.separator + "/myDownload/";
    //用来接收线程发送来的文件下载量，进行UI界面的更新
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {//定义一个Handler，用于处理下载线程与UI间通讯
            if (!Thread.currentThread().isInterrupted()) {
                switch (msg.what) {
                    case 0:
                        pb.setMax(fileSize);
                    case 1:
                        pb.setProgress(downLoadFileSize);
                        int result = downLoadFileSize * 100 / fileSize;
                        tv.setText(result + "%");
                        break;
                    case 2:
                        Toast.makeText(MainActivity.this, "文件下载完成", Toast.LENGTH_SHORT).show();
//                        FileInputStream fis = null;
//                        try {
//                            fis = new FileInputStream(Environment.getExternalStorageDirectory() + File.separator + "/ceshi/" + filename);
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        }
//                        Bitmap bitmap = BitmapFactory.decodeStream(fis); ///把流转化为Bitmap图
//                        imageView.setImageBitmap(bitmap);
                        break;

                    case -1:
                        String error = msg.getData().getString("error");
                        Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pb = (ProgressBar) findViewById(R.id.progressBar);
        tv = (TextView) findViewById(R.id.textView);
        imageView = (ImageView) findViewById(R.id.imageView);
        tv.setText("0%");

        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    public void run() {
                        try {
                            //下载文件，参数：第一个URL，第二个存放路径
                            down_file(fileUrl, dirStr);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

            }
        });


    }

    /**
     * 文件下载
     *
     * @param url：文件的下载地址
     * @param path：文件保存到本地的地址
     * @throws IOException
     */
    public void down_file(String url, String path) throws IOException {
        //下载函数
        filename = url.substring(url.lastIndexOf("/") + 1);
        //获取文件名
        URL myURL = new URL(url);
        URLConnection conn = myURL.openConnection();
        conn.connect();
        InputStream is = conn.getInputStream();
        this.fileSize = conn.getContentLength();//根据响应获取文件大小
        if (this.fileSize <= 0) throw new RuntimeException("无法获知文件大小 ");
        if (is == null) throw new RuntimeException("stream is null");
        File file1 = new File(path);
        File file2 = new File(path + filename);
        if (!file1.exists()) {
            file1.mkdirs();
            Log.d("down_file", "down_file: " + filename);
        }
        if (!file2.exists()) {
            file2.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(path + filename);
        //把数据存入路径+文件名
        byte buf[] = new byte[1024];
        downLoadFileSize = 0;
        sendMsg(0);
        do {
            //循环读取
            int numread = is.read(buf);
            if (numread == -1) {
                break;
            }
            fos.write(buf, 0, numread);
            downLoadFileSize += numread;

            sendMsg(1);//更新进度条
        } while (true);

        sendMsg(2);//通知下载完成

        try {
            is.close();
        } catch (Exception ex) {
            Log.e("tag", "error: " + ex.getMessage(), ex);
        }

    }

    //在线程中向Handler发送文件的下载量，进行UI界面的更新
    private void sendMsg(int flag) {
        Message msg = new Message();
        msg.what = flag;
        handler.sendMessage(msg);
    }
}
