package tw.com.px.pxsport;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SportingService extends Service {

    private LocationManager locationManager;
    private MyLocationListener myLocationListener;
    private LinkedList<HashMap<String, Double>> storeLatLng;
    private MyApp myApp;
    private Timer timer;
    private MyTimerTask myTimerTask;
    private int count = 0;
    private Calendar calendar;
    private SharedPreferences setting;
    private String userID;
    private GetImage getImage;

    public SportingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        myApp = (MyApp) getApplication();
        myApp.route = new LinkedList<>();
        myApp.img = new LinkedList<>();
        myApp.leaderBoardRoute = new LinkedList<>();
        myApp.leaderBoardImg = new LinkedList<>();
        myApp.leaderBoardRouteData = new LinkedList<>();
        myApp.leaderBoardCalorieData = new LinkedList<>();
        myApp.leaderCalorieHeader = new LinkedList<>();
        myApp.totalSpeed = new LinkedList<>();
        myLocationListener = new MyLocationListener();
        storeLatLng = new LinkedList<>();
        calendar = Calendar.getInstance();
        timer = new Timer();
        setting = getSharedPreferences("preference", Context.MODE_PRIVATE);
        userID = setting.getString("id", null);
        getImage = new GetImage();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null)
        {
            String msg = intent.getStringExtra("message");
            switch (msg)
            {
                //查詢首頁路線圖片、說明(HomeActivity)
                case "selectRouteData":
                    selectRouteData();
                    calorieLeaderBoard();
                    break;
                //查詢路線經緯度(HomeActivity)
                case "selectRoute":
                    selectRouteLatLng();
                    break;
                //開始運動(SportingActivity)
                case "startSport":
                    try {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, myLocationListener);
                    } catch (SecurityException e) {}
                    myTimerTask = new MyTimerTask();
                    timer.schedule(myTimerTask, 0, 1000);
                    break;
                //暫停運動(SportingActivity)
                case "pauseSport":
                    if (myTimerTask != null){ myTimerTask.cancel(); }
                    break;
                //結束運動(SportingActivity)
                case "stopSport":
                //取消運動(SportingActivity)
                case "cancelSport":
                    if (myTimerTask != null){ myTimerTask.cancel(); }
                    myApp.startLat = 0;
                    myApp.startLng = 0;
                    count = 0;
                    try {
                        locationManager.removeUpdates(myLocationListener);
                    } catch (SecurityException e) {}
                    break;
                //新增運動紀錄資料庫(SportCompleteActivity)
                case "insertSportData":
                    insertSportData();
                    break;
                //查詢使用者運動紀錄日期(MySportRecordActivity)
                case "selectDateRecord":
                    selectDateRecord();
                    break;
                //查詢使用者"某天"運動紀錄(MySportRecordActivity)
                case "selectDayRecord":
                    selectDayRecord( intent.getStringExtra("date") );
                    break;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i("siang", "serviceOndestory");
        if (timer != null)
        {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        try {
            locationManager.removeUpdates(myLocationListener);
        } catch (SecurityException e) {}

        super.onDestroy();
    }


    //位置監聽器
    private class MyLocationListener implements LocationListener
    {
        @Override
        public void onLocationChanged(Location location) {

            //起始位置
            if (location.getLatitude() != 0 &&  location.getLongitude() != 0)
            {
                if (myApp.startLat == 0 || myApp.startLng == 0)
                {
                    myApp.startLat = location.getLatitude();
                    myApp.startLng = location.getLongitude();
                }
            }

            //目前經位度
            myApp.nowLat = location.getLatitude();
            myApp.nowLng = location.getLongitude();
            // 目前時速  1 meters / second = 3.6 km / hour
            DecimalFormat df = new DecimalFormat("0");
            myApp.nowSpeed = df.format( location.getSpeed() * 3.6F );
            //儲存所有速度值(計算平均時速)
            myApp.totalSpeed.add(myApp.nowSpeed);

            // this -> SportAcitvity
            Intent it = new Intent("location");
            sendBroadcast(it);
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }

    //時間任務
    private class MyTimerTask extends TimerTask
    {
        @Override
        public void run() {
            count++;
            String hour = String.format("%02d", (count / 3600) );
            String min = String.format("%02d", (count / 60) );
            String sec = String.format("%02d", (count % 60) );

            //持續時間
            myApp.totalDuration =  hour + ":" + min + ":" + sec;

            Intent it = new Intent("time");
            sendBroadcast(it);
        }
    }

    //查詢挑戰路線經緯度
    private void selectRouteLatLng()
    {
        new Thread()
        {
            @Override
            public void run() {
                super.run();
                try {
                    MultipartUtility mp = new MultipartUtility("http://54.238.240.238/pxsport.php", "UTF-8");
                    mp.addFormField("func", "selectPlaneRouteByID");
                    mp.addFormField("id", myApp.routeID);
                    List<String> data = mp.finish();

                    JSONArray jsonArray = new JSONArray(data.get(0));
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    String endLat = jsonObject.getString("Route_Latitude");
                    String endLng = jsonObject.getString("Route_Longitude");
                    myApp.endLat = Double.parseDouble(endLat);
                    myApp.endLng = Double.parseDouble(endLng);

                } catch (Exception e) {

                }
            }
        }.start();
    }

    //查詢所有路線圖片、說明
    private void selectRouteData()
    {

        new Thread()
        {
            @Override
            public void run() {
                try {
                    MultipartUtility mp = new MultipartUtility("http://54.238.240.238/pxsport.php", "UTF-8");
                    mp.addFormField("func", "selectPlaneRoute");
                    List<String> data = mp.finish();

                    //自由路線 資料
                    HashMap<String, String> freeRoute = new HashMap<>();
                    freeRoute.put("id", "-1");
                    freeRoute.put("name", "自由路線");
                    freeRoute.put("explanation", "騎單車遊台灣，可以有很多選擇。或許是放鬆心情的慢騎漫遊、悠閒環湖之旅，或是突破極限、超越自我的挑戰之旅。");
                    freeRoute.put("image", "https://dl.dropboxusercontent.com/s/jppsjq2igp443o8/freeRoute.png");
                    myApp.route.add(freeRoute);

                    if (!data.get(0).equals("NO DATA"))
                    {
                        JSONArray jsonArray = new JSONArray(data.get(0));
                        for (int i = 0; i < jsonArray.length(); i++)
                        {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", obj.getString("Route_ID"));
                            hashMap.put("name", obj.getString("Route_Name"));
                            hashMap.put("explanation", obj.getString("Route_Explanation"));
                            hashMap.put("image", obj.getString("Route_Image"));
                            myApp.route.add(hashMap);
                            myApp.leaderBoardRoute.add(hashMap);
                        }
                    }

                    //解碼圖片
                    decodeImage();

                } catch (Exception e) {
                    Log.i("siang", e.toString());
                }
            }
        }.start();
    }

    //解碼圖片
    private void decodeImage()
    {
        new Thread()
        {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < myApp.route.size(); i++)
                    {
                        String path = myApp.route.get(i).get("image");
                        URL url = new URL( path );

                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setDoInput(true);
                        conn.connect();

                        InputStream in = conn.getInputStream();
                        Bitmap ImgBitmap = getImage.getBitmap(in);
                        myApp.img.add( ImgBitmap );
                        if (i > 0)
                        {
                            myApp.leaderBoardImg.add( myApp.img.get(i) );
                        }
                        in.close();
                    }

                    // this -> HomeActivity
                    Intent it = new Intent("routeData");
                    sendBroadcast(it);

                    //排行榜
                    routeLeaderBoard();
                } catch (Exception e) {
                    Log.i("brad", e.toString());
                }
            }
        }.start();
    }

    //路線排行榜
    private void routeLeaderBoard() {
        new Thread()
        {
            @Override
            public void run() {
                super.run();
                try {
                    Log.i("siang", myApp.leaderBoardRoute.size() + "");
                    Log.i("siang", myApp.leaderBoardImg.size() + "");
                    if (myApp.leaderBoardRoute.size() > 0)
                    {
                        for (int i = 0; i < myApp.leaderBoardRoute.size(); i++)
                        {
                            MultipartUtility mp = new MultipartUtility("http://54.238.240.238/pxsport.php", "UTF-8");
                            mp.addFormField("func", "routeLeaderBoard");
                            mp.addFormField("routeID", myApp.leaderBoardRoute.get(i).get("id"));
                            List<String> list = mp.finish();

                            if (!list.get(0).equals("NO DATA"))
                            {
                                JSONArray array = new JSONArray(list.get(0));
                                LinkedList<HashMap<String, String>> leaderBoardList = new LinkedList<>();
                                for (int j = 0; j < array.length(); j++)
                                {
                                    JSONObject obj = array.getJSONObject(j);
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("row", obj.getString("Row"));
                                    hashMap.put("userName", obj.getString("User_Name"));
                                    hashMap.put("routeName", obj.getString("Route_Name"));
                                    hashMap.put("speed", obj.getString("UserRecord_AvgSpeed"));
                                    leaderBoardList.add(hashMap);
                                }
                                myApp.leaderBoardRouteData.add(leaderBoardList);
                            }
                            else
                            {
                                LinkedList<HashMap<String, String>> leaderBoardList = new LinkedList<>();
                                myApp.leaderBoardRouteData.add( leaderBoardList );
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.i("saing", e.toString());
                }
            }
        }.start();
    }

    //卡路里排行榜
    private void calorieLeaderBoard()
    {Log.i("siang", "OK");
        new Thread()
        {
            @Override
            public void run() {
                try {
                    int year = calendar.get(Calendar.YEAR);
                    for (int i = 1; i < 13; i++)
                    {
                        LinkedList<HashMap<String, String>> row = new LinkedList<>();
                        MultipartUtility mp = new MultipartUtility("http://54.238.240.238/pxsport.php", "UTF-8");
                        mp.addFormField("func", "calorieLeaderBoard");
                        mp.addFormField("year", Integer.toString(year));
                        mp.addFormField("month", String.format("%02d", i));
                        List<String> data = mp.finish();
                        Log.i("siang", data.get(0));

                        if (!data.get(0).equals("NO DATA"))
                        {
                            JSONArray array = new JSONArray(data.get(0));
                            for (int j = 0; j < array.length(); j++)
                            {
                                JSONObject obj = array.getJSONObject(j);
                                HashMap<String, String> hashMap = new HashMap<>();
                                hashMap.put("row", obj.getString("Row"));
                                hashMap.put("userName", obj.getString("User_Name"));
                                hashMap.put("calorie", obj.getString("UserRecord_Calorie"));
                                row.add(hashMap);
                            }
                            myApp.leaderBoardCalorieData.add(row);
                        }
                        else
                        {
                            LinkedList<HashMap<String, String>> linkedList = new LinkedList<>();
                            myApp.leaderBoardCalorieData.add(linkedList);
                        }

                        //儲存年月
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("year", Integer.toString(year));
                        hashMap.put("month", Integer.toString(i));
                        myApp.leaderCalorieHeader.add(hashMap);

                    }

                } catch (Exception e) {
                    Log.i("siang", e.toString());
                }
            }
        }.start();
    }

    //運動記錄寫入資料庫
    private void insertSportData()
    {
        new Thread()
        {
            @Override
            public void run() {
                super.run();

                try {
                    String func = myApp.routeID == "-1" ? "insertFreeRouteRecord" : "insertPlaneRouteRecord";

                    MultipartUtility mp = new MultipartUtility("http://54.238.240.238/pxsport.php", "UTF-8");
                    mp.addFormField("func", func);
                    mp.addFormField("userID", userID);
                    mp.addFormField("routeID", myApp.routeID);
                    mp.addFormField("speed", myApp.avgSpeed);
                    mp.addFormField("mile", myApp.totalMileage);
                    mp.addFormField("time", myApp.totalDuration);
                    mp.addFormField("calorie", myApp.calorie);
                    mp.finish();

                } catch (Exception e) {
                    Log.i("siang", e.toString());
                }
            }
        }.start();


    }

    //查詢使用者運動紀錄日期
    private void selectDateRecord()
    {
        new Thread()
        {
            @Override
            public void run() {
                super.run();
                try {

                    //自由路線
                    MultipartUtility mp = new MultipartUtility("http://54.238.240.238/pxsport.php", "UTF-8");
                    mp.addFormField("func", "selectDateRecord");
                    mp.addFormField("userID", userID);
                    List<String> data  = mp.finish();

                    JSONArray jsonArray = new JSONArray(data.get(0));
                    myApp.calendarRecord = new String[jsonArray.length()];
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        myApp.calendarRecord[i] = obj.getString("Date");
                    }

                    Intent it = new Intent("dateRecord");
                    sendBroadcast(it);

                } catch (Exception e) {

                }
            }
        }.start();
    }

    //查詢使用者"某天"運動紀錄
    private void selectDayRecord(final String date)
    {
        new Thread()
        {
            @Override
            public void run() {
                super.run();
                try {
                    ArrayList<String> record = new ArrayList<String>();
                    //取得自由路線運動資訊
                    MultipartUtility mpFree = new MultipartUtility("http://54.238.240.238/pxsport.php", "UTF-8");
                    mpFree.addFormField("func", "selectFreeDayRecord");
                    mpFree.addFormField("userID", userID);
                    mpFree.addFormField("date", date);
                    List<String> dataFree = mpFree.finish();

                    if (!dataFree.get(0).equals("NO DATA"))
                    {
                        JSONArray arrayFree = new JSONArray(dataFree.get(0));
                        for (int i = 0; i < arrayFree.length(); i++)
                        {
                            JSONObject obj = arrayFree.getJSONObject(i);
                            record.add( "路線 : 自由路線\n" );
                            record.add( "持續時間 : " + obj.getString("FreeRouteRecord_Duration") + "\n" );
                            record.add( "平均時速 : " + obj.getString("FreeRouteRecord_AvgSpeed") + "  KM/H\n" );
                            record.add( "總里程 : " + obj.getString("FreeRouteRecord_Mileage") + "  KM\n" );
                            record.add( "消耗卡路里 : " + obj.getString("FreeRouteRecord_Calorie") + "  kcal\n" );
                            record.add("\n");
                        }
                    }

                    //取得規劃路線運動資訊
                    MultipartUtility mpRoute = new MultipartUtility("http://54.238.240.238/pxsport.php", "UTF-8");
                    mpRoute.addFormField("func", "selectRouteDayRecord");
                    mpRoute.addFormField("date", date);
                    mpRoute.addFormField("userID", userID);
                    List<String> dataRoute = mpRoute.finish();

                    if (!dataRoute.get(0).equals("NO DATA"))
                    {
                        JSONArray arrayRoute = new JSONArray(dataRoute.get(0));
                        for (int i = 0; i < arrayRoute.length(); i++)
                        {
                            JSONObject obj = arrayRoute.getJSONObject(i);
                            record.add( "路線 : " + obj.getString("Route_Name") + "\n" );
                            record.add( "持續時間 : " + obj.getString("UserRecord_Duration") + "\n" );
                            record.add( "平均時速 : " + obj.getString("UserRecord_AvgSpeed") + "  KM/H\n" );
                            record.add( "總里程 : " + obj.getString("UserRecord_Mileage") + "  KM\n" );
                            record.add( "消耗卡路里 : " + obj.getString("UserRecord_Calorie") + "  kcal\n" );
                            record.add("\n");
                        }
                    }

                    if (record.size() != 0)
                    {
                        Intent it = new Intent("dayRecord");
                        it.putStringArrayListExtra("freeRecord", record);
                        sendBroadcast(it);
                    }

                } catch (Exception e) {

                }
            }
        }.start();
    }


}
