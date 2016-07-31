package tw.com.px.pxsport;

import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class BikeLightCheck extends AppCompatActivity {

    private MyApp myApp;
    private Toolbar myToolbar;
    private enum ledLight { lefeLight, rightLight, allLight };
    private String leftStatus = "OFF", rightStatus = "OFF", allStatus = "OFF";
    private GetImage getImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bike_light_check);

        myApp = (MyApp)getApplication();
        getImage = new GetImage();

        //Toolbar
        myToolbar = (Toolbar)findViewById(R.id.blc_myToorBar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("車燈檢查");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //背景
        BitmapDrawable bitmapDrawable = getImage.getBimapDrawable(getResources(), R.drawable.bike_check_background);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.blc_layout);
        layout.setBackground(bitmapDrawable);

        if (myApp.bluetoothInput != null){ new AcceptMsgThread().start(); }

    }


    //右燈
    public void rightLight(View v)
    {
        new ControlLed(ledLight.rightLight).sendMsg();
    }
    //左燈
    public void leftLight(View v)
    {
        new ControlLed(ledLight.lefeLight).sendMsg();
    }
    //全檢測
    public void allLight(View v)
    {
        new ControlLed(ledLight.allLight).sendMsg();
    }

    //控制車燈(送出訊息)
    private class ControlLed
    {
        ledLight light;
        public ControlLed(ledLight light)
        {
            this.light = light;
        }
        public void sendMsg()
        {
            Log.i("siang", "left="+leftStatus);
            Log.i("siang", "right="+rightStatus);
            String msg = "";
            switch (light)
            {
                case lefeLight:
                    if (rightStatus.equals("OFF") && allStatus.equals("OFF"))
                        msg = leftStatus.equals("OFF") ? "leftOn" : "leftOff";
                    break;
                case rightLight:
                    if (leftStatus.equals("OFF") && allStatus.equals("OFF"))
                        msg = rightStatus.equals("OFF") ? "rightOn" : "rightOff";
                    break;
                case allLight:
                    if (leftStatus.equals("OFF") && rightStatus.equals("OFF"))
                        msg = allStatus.equals("ON") ? "" : "allBlinking";
                    break;
            }
            //送出訊息
            if (myApp.bluetoothOutput != null && !msg.isEmpty())
            {
                try {
                    myApp.bluetoothOutput.write(msg.getBytes());
                    myApp.bluetoothOutput.flush();
                } catch (Exception e) {
                    Log.i("siang", e.toString());
                }
            }
        }
    }

    //接收Arduino回傳訊息
    private class AcceptMsgThread extends Thread
    {
        @Override
        public void run() {
            String data;
            InputStreamReader ir = new InputStreamReader(myApp.bluetoothInput);
            BufferedReader bufferedReader = new BufferedReader(ir);
            //持續等待接收
            while (true)
            {

                try {
                    while ( ( data = bufferedReader.readLine() ) != null )
                    {
                        switch (data)
                        {
                            case "isLeftOn":
                                leftStatus = "ON";
                                break;
                            case "isLeftOff":
                                leftStatus = "OFF";
                                break;
                            case "isRightOn":
                                rightStatus = "ON";
                                break;
                            case "isRightOff":
                                rightStatus = "OFF";
                                break;
                            case "allLightOn":
                                allStatus = "ON";
                                break;
                            case "allLightOff":
                                allStatus = "OFF";
                                break;
                        }
                    }
                } catch (Exception e) {
                    Log.i("siang", e.toString());
                    break;
                }
            }
        }
    }

    //回上一頁
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


}
