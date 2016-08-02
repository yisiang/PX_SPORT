package tw.com.px.pxsport;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.LinkagePager;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import me.crosswall.lib.coverflow.CoverFlow;
import me.crosswall.lib.coverflow.core.LinkagePagerContainer;

public class HomeActivity extends AppCompatActivity {

    private Drawer drawer;
    private Toolbar myToolbar;
    private MyApp myApp;
    private MyRouteReceiver myRouteReceiver;
    private LinkagePagerContainer pagerContainer;
    private LinkagePager upPager, bottomPager;
    private UpPagerAdapter upPagerAdapter;
    private BottomPagerAdapter bottomPagerAdapter;
    private SharedPreferences setting;
    private ProgressDialog progressDialog;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothAdapter adapter;
    private MyScanReceiver receiver;
    private LinkedList<BluetoothDevice> allDevices;
    private BluetoothSocket socket;
    private ProgressDialog dialog;
    private Timer timer;
    private ToastHanlder hanlder;
    private AccountHeader header;
    private GetImage getImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        myApp = (MyApp) getApplication();
        myRouteReceiver = new MyRouteReceiver();
        pagerContainer = (LinkagePagerContainer) findViewById(R.id.ha_pager_container);
        upPagerAdapter = new UpPagerAdapter();
        upPager = pagerContainer.getViewPager();
        bottomPager = (LinkagePager) findViewById(R.id.ha_bottom_pager);
        bottomPagerAdapter = new BottomPagerAdapter();
        adapter = BluetoothAdapter.getDefaultAdapter();
        receiver = new MyScanReceiver();
        allDevices = new LinkedList<>();
        dialog = new ProgressDialog(this);
        dialog.setTitle("藍芽連線中...");
        hanlder = new ToastHanlder();
        setting = getSharedPreferences("preference", Context.MODE_PRIVATE);
        getImage = new GetImage();

        //取得權限
        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
                    ,Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!hasPermission(this, permission))
        {
            ActivityCompat.requestPermissions(this, permission, 1);
        }
        else
        {
            requestBluetooth();
        }

        //背景
        BitmapDrawable bitmapDrawable = getImage.getBimapDrawable(getResources(), R.drawable.home_background);
        LinearLayout layout = (LinearLayout) findViewById(R.id.ha_layout);
        layout.setBackground(bitmapDrawable);

        //dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("資料讀取中...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();


        // this -> HomeService
        Intent it = new Intent(this, SportingService.class);
        it.putExtra("message", "selectRouteData");
        startService(it);

        //註冊接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction("routeData");
        filter.addAction("img");
        registerReceiver(myRouteReceiver, filter);

        //初始化抽屜
        initNavicationDrawer();

        //upPager監聽器
        upPager.addOnPageChangeListener(new LinkagePager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                myApp.routeID = myApp.route.get(i).get("id");
                Log.i("siang", myApp.routeID);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    //權限回應
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                requestBluetooth();
            }
            else
            {
                finish();
            }
        }
    }

    //取得權限
    private boolean hasPermission(Context context, String[] permission)
    {
        for (String str : permission)
        {
            if (ContextCompat.checkSelfPermission(context, str) != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
        }
        return true;
    }

    //要求藍芽功能開啟
    private void requestBluetooth()
    {
        if (adapter != null)
        {
            if(!adapter.isEnabled())
            {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }
            else
            {
                discoverDevice();
            }
        }
        else
        {
            Toast.makeText(this, "您無藍芽裝置，將無法控制車燈", Toast.LENGTH_LONG).show();
        }

    }

    //要求藍芽功能開啟，回應處理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK)
        {
            discoverDevice();
        }
        else if (requestCode == 0 && resultCode == RESULT_CANCELED)
        {
            Toast.makeText(this, "你將無法控制車燈", Toast.LENGTH_SHORT).show();
        }
    }

    //搜尋附近的藍芽裝置
    private void discoverDevice()
    {
        dialog.show();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        adapter.startDiscovery();
        timer = new Timer();

        //藍芽12秒後則不搜尋
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (allDevices.size() != 0)
                {
                    for (BluetoothDevice device : allDevices)
                    {
                        if (device.getName().equals("Sport_BT"))
                        {
                            new ConnectDevice(device).start();
                            unregisterReceiver(receiver);
                            return;
                        }
                    }
                }
                dialog.dismiss();
                hanlder.sendEmptyMessage(1);
                unregisterReceiver(receiver);
            }
        }, 12 * 1000);

    }

    //接收器
    private class MyScanReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.i("siang", device+"");
            allDevices.add(device);
        }
    }

    private class ToastHanlder extends Handler
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast.makeText(HomeActivity.this, "搜尋不到其他藍芽裝置", Toast.LENGTH_SHORT).show();
        }
    }

    //建立連線
    private class ConnectDevice extends Thread
    {
        BluetoothDevice bluetoothDevice;

        public ConnectDevice(BluetoothDevice device)
        {
            bluetoothDevice = device;
            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (Exception e) {
                Log.i("siang", e.toString()+"uuidERROR");
            }
        }
        @Override
        public void run() {
            try {
                adapter.cancelDiscovery();
                socket.connect();
                if (socket.isConnected()) {
                    myApp.bluetoothOutput = socket.getOutputStream();
                    myApp.bluetoothInput = socket.getInputStream();
                }
                dialog.dismiss();
                Log.i("siang", "bluetooth connet");
            } catch (Exception e) {
                Log.i("siang", e.toString() );
            }
        }
    }

    //Navication drawer(抽屜)
    private void initNavicationDrawer()
    {

        //toolbar
        myToolbar = (Toolbar) findViewById(R.id.ha_myToorBar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("首頁");

        PrimaryDrawerItem personal = new PrimaryDrawerItem().withName(R.string.drawer_item_personal)
                .withIcon(GoogleMaterial.Icon.gmd_person);
        PrimaryDrawerItem record = new PrimaryDrawerItem().withName(R.string.drawer_item_record)
                .withIcon(GoogleMaterial.Icon.gmd_receipt);
        PrimaryDrawerItem leaderboard = new PrimaryDrawerItem().withName(R.string.drawer_item_leaderboard)
                .withIcon(GoogleMaterial.Icon.gmd_assessment);
        PrimaryDrawerItem bikecheck = new PrimaryDrawerItem().withName(R.string.drawer_item_bikecheck)
                .withIcon(GoogleMaterial.Icon.gmd_directions_bike);

//        SecondaryDrawerItem item2 = (SecondaryDrawerItem)new SecondaryDrawerItem().withName(R.string.drawer_item_home);

        header = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.home_bike)
                .withProfileImagesVisible(false)
                .addProfiles(new ProfileDrawerItem()
                    .withName( setting.getString("name", "User") )
                    .withEmail( setting.getString("account", null) )
                )
                .withSelectionListEnabledForSingleProfile(false)
                .build();

        drawer = new DrawerBuilder()
                .withAccountHeader(header)
                .withActivity(this)
                .withRootView(R.id.drawer_layout)
                .withToolbar(myToolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        personal,
                        record,
                        leaderboard
                )
                .withSelectedItem(-1)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch (position)
                        {
                            case 1:
                                // to -> 個人資料
                                Intent itPersonal = new Intent(HomeActivity.this, PersonalInfoActivity.class);
                                startActivity(itPersonal);
                                break;
                            case 2:
                                // to -> 我的紀錄
                                Intent itMySportRecord = new Intent(HomeActivity.this, MySportRecordActivity.class);
                                startActivity(itMySportRecord);
                                break;
                            case 3:
                                // to -> 排行榜
                                Intent itLeaderBoard = new Intent(HomeActivity.this, LeaderBoardActivity.class);
                                startActivity(itLeaderBoard);
                                break;
                        }
                        return false;
                    }
                })
                .build();

        //Show the hamburger icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);


    }




    //開始運動
    public void startSport(View v)
    {
        //判斷是否為自由路線
        String msg = myApp.routeID.equals("-1") ? "freeRoute" : "selectRoute";
        Intent intentService = new Intent(this, SportingService.class);
        intentService.putExtra("message", msg);
        startService(intentService);

        Intent it = new Intent(this, SportingActivity.class);
        startActivity(it);
    }

    //路線監聽器
    private class MyRouteReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("routeData"))
            {
                upPager.setAdapter(upPagerAdapter);
                upPager.setOffscreenPageLimit(5);
                new CoverFlow.Builder()
                        .withLinkage(upPager)
                        .pagerMargin(0f)
                        .scale(0.3f)
                        .spaceSize(0f)
                        .rotationY(25f)
                        .build();
                bottomPager.setAdapter(bottomPagerAdapter);
                bottomPager.setOffscreenPageLimit(5);

                upPager.setLinkagePager(bottomPager);
                bottomPager.setLinkagePager(upPager);
                if (progressDialog.isShowing()){ progressDialog.dismiss(); }
            }
        }
    }

    //上面的pager(路線圖片)
    private class UpPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return myApp.route.size();
        }
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            ImageView imgView = new ImageView(HomeActivity.this);
            imgView.setImageBitmap(myApp.img.get(position));
            imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            container.addView(imgView);

            return imgView;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }

    //下面的pager(路線說明)
    private class BottomPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return myApp.route.size();
        }
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            TextView textView = new TextView(HomeActivity.this);
            String msg = myApp.route.get(position).get("name") + "\n\n" + myApp.route.get(position).get("explanation");
            textView.setText( msg );
            textView.setTextSize(17);
            container.addView(textView);

            return textView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }

    @Override
    public void finish() {
        unregisterReceiver(myRouteReceiver);
        stopService(new Intent(this, SportingService.class));
        try {
            if (socket != null){ socket.close(); }
            if (myApp.bluetoothOutput != null){ myApp.bluetoothOutput.close(); }
            if (myApp.bluetoothInput != null){ myApp.bluetoothInput.close(); }
        } catch (Exception e) {
            Log.i("siang", e.toString());
        }
        super.finish();
    }
}
