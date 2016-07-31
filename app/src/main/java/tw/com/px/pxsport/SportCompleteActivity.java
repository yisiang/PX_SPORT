package tw.com.px.pxsport;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;

import java.text.DecimalFormat;

public class SportCompleteActivity extends AppCompatActivity {

    private Toolbar myToolbar;
    private TextView tvTime, tvSpeed, tvMileage, tvCalorie;
    private MyApp myApp;
    private RatingBar ratingBar;
    private SharedPreferences setting;
    private int height, weight, age;
    private String gender;
    private CallbackManager callbackManager;
    private GetImage getImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //FB SDK初始化
        FacebookSdk.sdkInitialize(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport_complete);

        myApp = (MyApp)getApplication();
        setting = getSharedPreferences("preference", Context.MODE_PRIVATE);
        height = Integer.parseInt( setting.getString("height", null) );
        weight = Integer.parseInt( setting.getString("weight", null) );
        age = Integer.parseInt( setting.getString("age", null) );
        gender = setting.getString("gender", null);
        getImage = new GetImage();

        //Toolbar
        myToolbar = (Toolbar)findViewById(R.id.sc_myToorbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("運動完成");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_done_white_16dp_2x);

        //背景
        BitmapDrawable bitmapDrawable = getImage.getBimapDrawable(getResources(), R.drawable.complete_background);
        LinearLayout layout = (LinearLayout) findViewById(R.id.sc_layout);
        layout.setBackground(bitmapDrawable);

        tvTime = (TextView)findViewById(R.id.sc_time);
        tvMileage = (TextView)findViewById(R.id.sc_mileage);
        tvSpeed = (TextView)findViewById(R.id.sc_speed);
        tvCalorie = (TextView)findViewById(R.id.sc_calorie);
        ratingBar = (RatingBar)findViewById(R.id.sc_rating);

        //計算運動紀錄，並顯示
        caculateRecord();

        //新增運動紀錄至資料庫
        insertSportData();

        // FB callback
        callbackManager = CallbackManager.Factory.create();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sc_toolbar_fb, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //點擊回首頁
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.sc_menu_fb:
                shareFB();
                break;
        }
        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    //分享至FB
    private void shareFB()
    {
        //轉換成秒
        String[] str = myApp.totalDuration.split(":");
        int totalDuration = Integer.parseInt(str[0])*3600 + Integer.parseInt(str[1])*60 + Integer.parseInt(str[2]);
        ShareOpenGraphObject object = new ShareOpenGraphObject.Builder()
                .putString("og:type", "fitness.course")
                .putString("og:title", "運動資訊")
                .putString("og:description", "您將把此運動資訊分享給朋友")
                .putInt("fitness:duration:value", totalDuration )
                .putString("fitness:duration:units", "s")
                .putDouble("fitness:distance:value", Double.parseDouble(myApp.totalMileage))
                .putString("fitness:distance:units", "km")
                .putInt("fitness:calories", Integer.parseInt(myApp.calorie))
                .putDouble("fitness:speed:value", new Double(myApp.avgSpeed) / 3.6)
                .putString("fitness:speed:units", "m/s")
                .build();
        ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
                .setActionType("fitness.bikes")
                .putObject("fitness:course", object)
                .build();
        ShareOpenGraphContent content = new ShareOpenGraphContent.Builder()
                .setPreviewPropertyName("fitness:course")
                .setAction(action)
                .build();

        ShareDialog.show(this, content);

//        shareButton.setShareContent(content);
    }

    //計算運動紀錄
    private void caculateRecord()
    {
        float tempSpeed = 0;
        float avgspeed = 0;
        //計算平均時速
        for (int i = 0; i < myApp.totalSpeed.size(); i++)
        {
            tempSpeed += Float.parseFloat( myApp.totalSpeed.get(i) );
        }
        if ( tempSpeed != 0 )
        {
            //計算評分用
            avgspeed = tempSpeed / myApp.totalSpeed.size();
            DecimalFormat df = new DecimalFormat("0");
            myApp.avgSpeed = df.format( tempSpeed / myApp.totalSpeed.size() );
        }
        else
        {
            myApp.avgSpeed = "0";
        }


        //計算卡路里
        double men = 66.5+(13.75 * weight)+(5.003 * height)-(6.775 * age);
        double women = 655.1+(9.563 * weight)+(1.85 * height)-(4.676 * age);
        String[] str = myApp.totalDuration.split(":");
        int time = Integer.parseInt(str[0]) * 3600 + Integer.parseInt(str[1]) * 60 + Integer.parseInt(str[2]);

        DecimalFormat df = new DecimalFormat("0");

        if (gender.equals("female"))
        {
            myApp.calorie = df.format( ((women * 4)/24) * time/60/60 );
        }
        else if (gender.equals("male"))
        {
            myApp.calorie = df.format( ((men * 4)/24) * time/60/60 );
        }

        tvTime.setText(myApp.totalDuration);
        tvMileage.setText(myApp.totalMileage + "   KM");
        tvSpeed.setText(myApp.avgSpeed + "   KM/H");
        tvCalorie.setText(myApp.calorie + "   Kcal");
        Log.i("siang", "duration="+myApp.totalDuration+","+"milleage="+myApp.totalMileage+","+"speed="+myApp.avgSpeed+","+"calorie="+myApp.calorie);
        //計算評分

        if (avgspeed < 10) { showRating(1.0f); }
        else if ( avgspeed < 20) { showRating(2.0f); }
        else if ( avgspeed < 30) { showRating(3.0f); }
        else if ( avgspeed < 40) { showRating(4.0f); }
        else if ( avgspeed >= 40) { showRating(5.0f); }
    }

    //顯示評分
    private void showRating(float num)
    {
        float current = ratingBar.getRating();

        ObjectAnimator anim = ObjectAnimator.ofFloat(ratingBar , "rating", current, num);
        anim.setDuration(1000);
        anim.start();
    }

    //新增運動紀錄至資料庫
    private void insertSportData()
    {
        Intent it = new Intent(this, SportingService.class);
        it.putExtra("message", "insertSportData");
        startService(it);
    }
}
