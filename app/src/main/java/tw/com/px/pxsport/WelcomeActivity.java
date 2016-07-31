package tw.com.px.pxsport;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends AppCompatActivity {
    private Timer timer;
    private SharedPreferences setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        timer = new Timer();
        setting = getSharedPreferences("preference", Context.MODE_PRIVATE);

        //判斷是否有登入
        if ( setting.getString("id", null) == null )
        {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Intent it = new Intent(WelcomeActivity.this, LoginActivity.class);
                    startActivity(it);
                    if (timer != null)
                    {
                        timer.purge();
                        timer.cancel();
                        timer = null;
                    }
                    finish();
                }
            }, 2*1000);
        }
        else
        {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Intent it = new Intent(WelcomeActivity.this, HomeActivity.class);
                    startActivity(it);
                    if (timer != null)
                    {
                        timer.purge();
                        timer.cancel();
                        timer = null;
                    }
                    finish();
                }
            }, 2*1000);
        }
        // userID, userGender,userBirthday,userHeight,userWeight,userMail, userImage
    }

}
