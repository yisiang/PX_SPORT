package tw.com.px.pxsport;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import butterknife.Bind;
import butterknife.ButterKnife;
import tw.com.px.pxsport.decorators.EventDecorator;
import tw.com.px.pxsport.decorators.HighlightWeekendsDecorator;
import tw.com.px.pxsport.decorators.MySelectorDecorator;
import tw.com.px.pxsport.decorators.OneDayDecorator;

public class MySportRecordActivity extends AppCompatActivity implements OnDateSelectedListener {

    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    private AlertDialog.Builder builder;
    private Toolbar myToolbar;
    private Intent intentRecordService;
    private MyRecordReceiver myRecordReceiver;
    private MyApp myApp;
    private LinkedList<CalendarDay> dates;
    private GetImage getImage;

    @Bind(R.id.calendarView)
    MaterialCalendarView widget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_sport_record);
        ButterKnife.bind(this);

        myRecordReceiver = new MyRecordReceiver();
        myApp = (MyApp) getApplication();
        dates = new LinkedList<>();
        builder = new AlertDialog.Builder(this);
        getImage = new GetImage();

        // Toolbar
        myToolbar = (Toolbar)findViewById(R.id.mysr_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("我的紀錄");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //背景
        BitmapDrawable bitmapDrawable = getImage.getBimapDrawable(getResources(), R.drawable.record_background);
        LinearLayout layout = (LinearLayout) findViewById(R.id.mysr_layout);
        layout.setBackground(bitmapDrawable);

        // this -> SportingService
        Intent it = new Intent(this, SportingService.class);
        it.putExtra("message", "selectDateRecord");
        startService(it);

        //註冊監聽器
        IntentFilter filter = new IntentFilter();
        filter.addAction("dateRecord");
        filter.addAction("dayRecord");
        registerReceiver(myRecordReceiver, filter);

        //初始化月曆
        initCalendar();

    }

    //初始化月曆
    private void initCalendar()
    {
        widget.setOnDateChangedListener(this);
        widget.setShowOtherDates(MaterialCalendarView.SHOW_ALL);

        //取得今天的日期
        Calendar instance = Calendar.getInstance();
        widget.setSelectedDate(instance.getTime());

        //設定月曆起訖
        widget.state().edit()
                .setMinimumDate(CalendarDay.from(1900, 1, 1))
                .setMaximumDate(CalendarDay.from(2300, 12, 31))
                .commit();

        //增加月曆的裝飾
        widget.addDecorators(
                new MySelectorDecorator(this),
                new HighlightWeekendsDecorator(),
                oneDayDecorator
        );
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        //If you change a decorate, you need to invalidate decorators
        oneDayDecorator.setDate(date.getDate());
        widget.invalidateDecorators();

        // this -> SportingService (查詢某天運動資訊)
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String newDate = df.format(date.getDate());
        Intent it = new Intent(this, SportingService.class);
        it.putExtra("message", "selectDayRecord");
        it.putExtra("date", newDate);
        startService(it);

    }


    //查詢運動紀錄監聽器
    private class MyRecordReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("dateRecord"))
            {
                for (int i = 0; i < myApp.calendarRecord.length; i++)
                {
                    try {
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = df.parse(myApp.calendarRecord[i]);
                        CalendarDay calendarDay = CalendarDay.from(date);
                        dates.add(calendarDay);
                    } catch (Exception e) {

                    }
                }

                widget.addDecorator(new EventDecorator(Color.RED, dates));

            }
            else if (intent.getAction().equals("dayRecord"))
            {
                showRecordDialog( intent.getStringArrayListExtra("freeRecord") );
            }
        }
    }

    //顯示運動紀錄資訊 訊息框
    private void showRecordDialog(ArrayList<String> list)
    {

        StringBuffer msg = new StringBuffer();
        for (int i = 0; i < list.size(); i++)
        {
            msg.append( list.get(i) );
        }

        builder.setTitle("運動資訊")
                .setMessage(msg)
                .setCancelable(true)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    //回上一頁(首頁)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        //解除註冊接收器
        unregisterReceiver(myRecordReceiver);
        super.finish();
    }
}
