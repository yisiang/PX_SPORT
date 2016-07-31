package tw.com.px.pxsport;

import android.app.Application;
import android.bluetooth.BluetoothServerSocket;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by HONG SIANG on 2016/6/20.
 */
public class MyApp extends Application {
    //總里程
    public String totalMileage = "0.0";
    //持續時間
    public String totalDuration;
    //加總時速
    public LinkedList<String> totalSpeed;
    //平均時速,目前時速
    public String avgSpeed, nowSpeed;
    //卡路里
    public String calorie;
    //路線ID
    public String routeID = "-1";
    //起始經緯度,目的地經緯度,目前經緯度
    public double startLat = 0, startLng = 0, endLat, endLng, nowLat, nowLng;
    //取得運動紀錄的日期
    public String[] calendarRecord;
    //取得路線資料
    public LinkedList<HashMap<String, String>> route, leaderBoardRoute, leaderCalorieHeader;
    //解碼後的圖片
    public LinkedList<Bitmap> img, leaderBoardImg;
    //路線排行榜資料、卡路里排行榜
    public LinkedList<LinkedList<HashMap<String, String>>> leaderBoardRouteData, leaderBoardCalorieData;
    //藍芽
    public OutputStream bluetoothOutput;
    public InputStream bluetoothInput;


}
