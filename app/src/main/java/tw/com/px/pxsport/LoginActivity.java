package tw.com.px.pxsport;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.ConnectException;
import java.util.Calendar;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private EditText email,passwd;
    private MyHandler handler;
    private SharedPreferences setting;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        handler = new MyHandler();
        email = (EditText)findViewById(R.id.email);
        passwd = (EditText)findViewById(R.id.passwd);
        setting = getSharedPreferences("preference", Context.MODE_PRIVATE);
        calendar = Calendar.getInstance();

    }

    //註冊
    public void register(View v){
        Intent it = new Intent(this, RegisterActivity.class);
        startActivity(it);
    }
    //登入
    public void login(View v){
        new Thread(){
            @Override
            public void run() {
                doLogin();
            }
        }.start();
    }

    //查詢帳號密碼是否正確
    private void doLogin(){
        String strAccount = email.getText().toString();
        String strPasswd = passwd.getText().toString();
        try{
            MultipartUtility mu = new MultipartUtility(
                    "http://54.238.240.238/pxsport.php","UTF-8");
            mu.addFormField("func", "selectUserAccount");
            mu.addFormField("User_Account", strAccount);
            mu.addFormField("User_Password", strPasswd);

            List<String> ret = mu.finish();
            String result = ret.get(0);
            Log.i("siang", result);
            //跳轉至首頁
            if (!result.equals("NO DATA")){
                JSONArray jsonArray = new JSONArray(ret.get(0));
                JSONObject obj = jsonArray.getJSONObject(0);
                //使用者資訊寫入偏好設定
                int thisYear = calendar.get(Calendar.YEAR);
                String[] birthday = obj.getString("User_Birthday").split("-");
                int age = thisYear - Integer.parseInt(birthday[0]);
                setting.edit().putString("id", obj.getString("User_ID")).commit();
                setting.edit().putString("name", obj.getString("User_Name")).commit();
                setting.edit().putString("gender", obj.getString("User_Gender")).commit();
                setting.edit().putString("height", obj.getString("User_Height")).commit();
                setting.edit().putString("weight", obj.getString("User_Weight")).commit();
                setting.edit().putString("account", obj.getString("User_Account")).commit();
                setting.edit().putString("age", Integer.toString(age)).commit();

                Log.i("siang", "id="+setting.getString("id",null) + "name=" + setting.getString("name",null) + "gender=" + setting.getString("gender",null) +
                "height=" + setting.getString("height", null) + "weight=" + setting.getString("weight",null) + "account=" + setting.getString("account",null) + "age=" + setting.getString("age",null));

                Intent it = new Intent(this, HomeActivity.class);
                startActivity(it);
                finish();
            }else{
                handler.sendEmptyMessage(1);
            }

        } catch(Exception e){
            Log.i("siang", e.toString());
        }
    }

    private class MyHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast.makeText(LoginActivity.this, "帳號密碼錯誤！", Toast.LENGTH_SHORT).show();
        }
    }
}
