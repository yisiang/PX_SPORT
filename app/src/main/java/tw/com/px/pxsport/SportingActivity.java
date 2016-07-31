package tw.com.px.pxsport;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.truizlop.fabreveallayout.FABRevealLayout;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

public class SportingActivity extends AppCompatActivity {

    private TextView tvTime, tvSpeed, tvMileage;
    private WebView webView;
    private MyLocationReceiver myLocationReceiver;
    private WebSettings webSettings;
    private MyUIHandler handler;
    private MyApp myApp;
    private boolean isStartLatLng;
    private FloatingActionButton startFab, pauseFab, stopFab;
    private int widthPixels;
    private FABRevealLayout fabRevealLayout;
    private enum ledLight { lefeLight, rightLight};
    private String leftStatus = "OFF", rightStatus = "OFF";
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sporting);

        tvTime = (TextView)findViewById(R.id.sa_time);

        tvSpeed = (TextView)findViewById(R.id.sa_speed);
        tvMileage = (TextView)findViewById(R.id.sa_mileage);
        webView = (WebView) findViewById(R.id.sa_webview);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        startFab = (FloatingActionButton) findViewById(R.id.sc_start);
        pauseFab = (FloatingActionButton) findViewById(R.id.sc_pause);
        stopFab = (FloatingActionButton) findViewById(R.id.sc_stop);
        myLocationReceiver = new MyLocationReceiver();
        handler = new MyUIHandler();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        myApp = (MyApp) getApplication();
        fabRevealLayout = (FABRevealLayout)findViewById(R.id.fab_reveal_layout);

        // 註冊接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction("location");
        filter.addAction("time");
        registerReceiver(myLocationReceiver, filter);

        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new getJS(), "sport");
        if (myApp.routeID.equals("-1"))
        {
            webView.loadUrl("file:///android_asset/mapPoly.php");
        }
        else
        {
            webView.loadUrl("file:///android_asset/directionService.php");
        }

        //取得螢幕寬度
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        widthPixels = metrics.widthPixels;

        initFloatingButton();
        initFABRevealLayout();

        if (myApp.bluetoothInput != null){ new AcceptMsgThread().start(); }

        //要求啟動GPS
        requestGPS();

    }


    //初始化 FloatingActionButton
    private void initFloatingButton()
    {
        pauseFab.hide();
        stopFab.hide();

        startFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFab.hide();
                stopFab.hide();
                pauseFab.show();
                startSport();
            }
        });
        pauseFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseFab.hide();
                startFab.show();
                stopFab.show();
                startFab.setTranslationX(-widthPixels / 2 / 2);
                stopFab.setTranslationX(widthPixels / 2 / 2);
                pauseSport();
            }
        });

        stopFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSport();
            }
        });

    }
    //初始化 FABRevealLayout
    private void initFABRevealLayout()
    {
        fabRevealLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                fabRevealLayout.revealMainView();
                return false;
            }
        });
    }

    //要求啟用GPS
    private void requestGPS()
    {
        boolean hasEnableGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if ( !hasEnableGPS )
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("警告")
                    .setMessage("您未啟動GPS定位")
                    .setPositiveButton("啟用", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent it = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(it);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onBackPressed();
                            unregisterReceiver(myLocationReceiver);
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    //位置監聽器
    private class MyLocationReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("location"))
            {
                //取得起始位置，並規劃路線
                if (isStartLatLng == false &&
                        webView.getUrl().equals("file:///android_asset/directionService.php"))
                {
                    //規劃路線
                    webView.loadUrl("javascript:directionRoute(" +
                            myApp.startLat +","+ myApp.startLng +","+ myApp.endLat + "," + myApp.endLng +")");
                    isStartLatLng = true;
                    Log.i("siang", myApp.startLat +"," + myApp.startLng);
                    Log.i("siang", myApp.endLat +"," + myApp.endLng);
                }
                webView.loadUrl("javascript:userLocation("+ myApp.nowLat +","+ myApp.nowLng +")");
                tvSpeed.setText( myApp.nowSpeed );
            }
            else if (intent.getAction().equals("time"))
            {
                tvTime.setText(myApp.totalDuration);
            }
        }
    }
    private class MyUIHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tvMileage.setText(myApp.totalMileage);
        }
    }

    // Javascript call function
    public class getJS
    {
        @JavascriptInterface
        public void setTotalDistance(float distance)
        {
            //米 -> 公里
            //取到小數點後第一位
            DecimalFormat df = new DecimalFormat("0.0");
            myApp.totalMileage = df.format(distance / 1000);
//            Log.i("siang", "d="+distance+","+"df="+myApp.totalMileage);
            handler.sendEmptyMessage(1);
        }
    }
    //開始運動
    private void startSport()
    {
        // this -> SportingService
        Intent it = new Intent(this, SportingService.class);
        it.putExtra("message", "startSport");
        startService(it);
    }
    //暫停運動
    private void pauseSport()
    {
        // this -> SportingService
        Intent it = new Intent(this, SportingService.class);
        it.putExtra("message", "pauseSport");
        startService(it);
    }
    //停止運動
    private void stopSport()
    {
        if ( myApp.totalMileage.equals("0.0") )
        {
            cancelDialog();
        }
        else
        {
            // this -> SportingService
            Intent it  = new Intent(this, SportingService.class);
            it.putExtra("message", "stopSport");
            it.putExtra("mileage", myApp.totalMileage);
            startService(it);

            // this -> SportCompleteActivity
            Intent intentSCActivity = new Intent(this, SportCompleteActivity.class);
            startActivity(intentSCActivity);
            unregisterReceiver(myLocationReceiver);

            finish();
        }
    }

    //取消運動訊息框
    private void cancelDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(SportingActivity.this);
        builder.setMessage("您確定取消騎乘")
                .setCancelable(false)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent it = new Intent(SportingActivity.this, SportingService.class);
                        it.putExtra("message", "cancelSport");
                        startService(it);
                        unregisterReceiver(myLocationReceiver);
                        onBackPressed();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //右燈
    public void bikingRightLight(View v)
    {
        new ControlLed(ledLight.rightLight).sendMsg();
    }
    //左燈
    public void bikingLeftLight(View v)
    {
        new ControlLed(ledLight.lefeLight).sendMsg();
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
                    if (rightStatus.equals("OFF"))
                        msg = leftStatus.equals("OFF") ? "leftOn" : "leftOff";
                    break;
                case rightLight:
                    if (leftStatus.equals("OFF"))
                        msg = rightStatus.equals("OFF") ? "rightOn" : "rightOff";
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
                        }
                    }
                } catch (Exception e) {
                    Log.i("siang", e.toString());
                    break;
                }
            }
        }
    }


    //返回鍵處理
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == event.KEYCODE_BACK)
        {
            cancelDialog();
        }
        return true;
    }

}
