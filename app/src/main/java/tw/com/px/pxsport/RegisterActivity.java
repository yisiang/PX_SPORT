package tw.com.px.pxsport;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    private String userGender = "";
    private NumberPicker year, month, day, tall ,heft;
    private EditText birthday, height, weight, name, account, passwd;
    private String yearNum, monthNum, dayNum, tallNum, heftNum, birthdayNum;
    private MyToastHandler myToastHandler;
    private Toolbar myToolbar;
    private SharedPreferences setting;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name = (EditText)findViewById(R.id.reg_name);
        account = (EditText)findViewById(R.id.reg_account);
        passwd = (EditText)findViewById(R.id.reg_pwd);
        birthday = (EditText) findViewById(R.id.reg_birthday);
        height = (EditText)findViewById(R.id.reg_height);
        weight = (EditText)findViewById(R.id.reg_weight);
        myToastHandler = new MyToastHandler();
        calendar = Calendar.getInstance();
        setting = getSharedPreferences("preference", Context.MODE_PRIVATE);

        myToolbar = (Toolbar) findViewById(R.id.reg_myToorbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("註冊");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker();
            }
        });

        height.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heightPicker();
            }
        });
        weight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weightPicker();
            }
        });
    }

    //日期選擇器
    private void datePicker()
    {
        int nowYear, nowMonth, nowDay;
        final Dialog dialog = new Dialog(RegisterActivity.this);

        //解決6.x版本無法顯示標題
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
        {
            dialog.setContentView(R.layout.dialog_date_6x);
        }
        else
        {
            dialog.setTitle("生日");
            dialog.setContentView(R.layout.dialog_date);
        }

        Button confirm = (Button) dialog.findViewById(R.id.date_confirm);
        Button cancel = (Button) dialog.findViewById(R.id.date_cancel);

        // 年
        year = (NumberPicker) dialog.findViewById(R.id.date_year);
        year.setMaxValue(2020);
        year.setMinValue(1940);
        //取得目前的值
        nowYear = yearNum == null ? 1990 : Integer.parseInt(yearNum);
        year.setValue(nowYear);
        yearNum = "" + year.getValue();
        year.setOnValueChangedListener(new NumberPicker.OnValueChangeListener (){
            public void onValueChange(NumberPicker view, int oldValue, int newValue) {
                yearNum = "" + year.getValue();
            }
        });

        //月
        month = (NumberPicker) dialog.findViewById(R.id.date_month);
        month.setMaxValue(12);
        month.setMinValue(1);
        //取得目前的值
        nowMonth = monthNum == null ? 6 : Integer.parseInt(monthNum);
        month.setValue(nowMonth);
        monthNum = "" + month.getValue();
        month.setOnValueChangedListener(new NumberPicker.OnValueChangeListener (){
            public void onValueChange(NumberPicker view, int oldValue, int newValue) {
                monthNum = "" + month.getValue();
            }
        });

        //日
        day = (NumberPicker) dialog.findViewById(R.id.date_days);
        day.setMaxValue(31);
        day.setMinValue(1);
        //取得目前的值
        nowDay = dayNum == null ? 15 : Integer.parseInt(dayNum);
        day.setValue(nowDay);
        dayNum = "" + day.getValue();
        day.setOnValueChangedListener(new NumberPicker.OnValueChangeListener (){
            public void onValueChange(NumberPicker view, int oldValue, int newValue) {
                dayNum = "" + day.getValue();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                birthdayNum = yearNum + "-" + monthNum + "-" + dayNum;
                birthday.setText(birthdayNum);
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    //身高選擇器
    private void heightPicker()
    {
        int nowTall;
        final Dialog dialog = new Dialog(RegisterActivity.this);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
        {
            dialog.setContentView(R.layout.dialog_height_6x);
        }
        else
        {
            dialog.setTitle("身高");
            dialog.setContentView(R.layout.dialog_height);
        }



        Button confirm = (Button) dialog.findViewById(R.id.height_confirm);
        Button cancel = (Button) dialog.findViewById(R.id.height_cancel);

        tall = (NumberPicker) dialog.findViewById(R.id.height_tall);
        tall.setMaxValue(200);
        tall.setMinValue(130);
        //取得目前的值
        nowTall = tallNum == null ? 150 : Integer.parseInt(tallNum);
        tall.setValue(nowTall);
        tallNum = "" + tall.getValue();
        tall.setOnValueChangedListener(new NumberPicker.OnValueChangeListener (){
            public void onValueChange(NumberPicker view, int oldValue, int newValue) {
                tallNum = "" + tall.getValue();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                height.setText(tallNum);
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();
    }

    //體重選擇器
    private void weightPicker()
    {
        int nowWeight;
        final Dialog dialog = new Dialog(RegisterActivity.this);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
        {
            dialog.setContentView(R.layout.dialog_weight_6x);
        }
        else
        {
            dialog.setTitle("體重");
            dialog.setContentView(R.layout.dialog_weight);
        }


        Button confirm = (Button)dialog.findViewById(R.id.weight_confirm);
        Button cancel = (Button)dialog.findViewById(R.id.weight_cancel);

        heft = (NumberPicker) dialog.findViewById(R.id.weight_heft);
        heft.setMaxValue(120);
        heft.setMinValue(30);
        //取得目前的值
        nowWeight = heftNum == null ? 50 : Integer.parseInt(heftNum);
        heft.setValue(nowWeight);
        heftNum = "" + heft.getValue();
        heft.setOnValueChangedListener(new NumberPicker.OnValueChangeListener (){
            public void onValueChange(NumberPicker view, int oldValue, int newValue) {
                heftNum = "" + heft.getValue();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weight.setText(heftNum);
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    // radio 性別
    public void radioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch(view.getId()) {
            case R.id.reg_woman:
                if (checked)
                    userGender = "female";
                break;
            case R.id.reg_man:
                if (checked)
                    userGender = "male";
                break;
        }
        Log.i("siang", userGender);
    }

    //註冊按鈕事件
    public void register(View v) {
        new Thread() {
            @Override
            public void run() {
                insertAccount();
            }
        }.start();
    }

    //新增帳號
    private void insertAccount() {

        String inputName = name.getText().toString();
        String inputAcc = account.getText().toString();
        String inputPwd = passwd.getText().toString();

        try{
            MultipartUtility mu = new MultipartUtility(
                    "http://54.238.240.238/pxsport.php","UTF-8");
            mu.addFormField("func", "insertUserAccount");
            mu.addFormField("User_Account", inputAcc);
            mu.addFormField("User_Password", inputPwd);
            mu.addFormField("User_Name", inputName);
            mu.addFormField("User_Gender", userGender);
            mu.addFormField("User_Height", tallNum);
            mu.addFormField("User_Weight", heftNum);
            mu.addFormField("User_Birthday", birthdayNum);
            List<String> ret = mu.finish();
            //接收資料庫回傳錯誤訊息
            String msg = ret.get(0);
            if (msg.equals("ActionError"))
            {
                myToastHandler.sendEmptyMessage(1);
            }
            else
            {
                Intent it = new Intent(this, HomeActivity.class);
                it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(it);
                //使用者資料寫入偏好設定
                int thisYear = calendar.get(Calendar.YEAR);
                int age = thisYear - Integer.parseInt(yearNum);
                setting.edit().putString("id", msg).commit();
                setting.edit().putString("name", inputName).commit();
                setting.edit().putString("gender", userGender).commit();
                setting.edit().putString("height", tallNum).commit();
                setting.edit().putString("weight", heftNum).commit();
                setting.edit().putString("account", inputAcc).commit();
                setting.edit().putString("age", Integer.toString(age)).commit();
                Log.i("siang", "id=" + msg + "gender=" + userGender + "tall=" + tallNum + "weight=" + heftNum
                 + "mail=" + inputAcc + "age=" + age);
                finish();
            }


        }catch (Exception e){
            Log.i("siang", e.toString());
        }
    }

    //吐出訊息
    private class MyToastHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast.makeText(RegisterActivity.this, "註冊失敗，請重新輸入!", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(getIntent());
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
