package tw.com.px.pxsport;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PersonalInfoActivity extends AppCompatActivity {

    private SharedPreferences setting;
    private EditText setHeight, setWeight;
    private TextView setName, setAccount;
    private ImageView img;
    private NumberPicker tall, heft;
    private String tallNum, heftNum;
    private Toolbar myToolbar;
    private Bitmap bitmap;
    private File appRoot;
    private MyToastHandler myToastHandler;
    private GetImage getImage;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        // Toolbar
        myToolbar = (Toolbar)findViewById(R.id.per_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("個人資料");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setHeight = (EditText) findViewById(R.id.setHeight);
        setWeight = (EditText) findViewById(R.id.setWeight);
        setName = (TextView) findViewById(R.id.setName);
        setAccount = (TextView) findViewById(R.id.setAccount);
        img = (ImageView) findViewById(R.id.img);
        myToastHandler = new MyToastHandler();
        getImage = new GetImage();

        setting = getSharedPreferences("preference", Context.MODE_PRIVATE);

        setHeight.setText( setting.getString("height", null) );
        setWeight.setText( setting.getString("weight", null) );
        setName.setText( setting.getString("name", null) );
        setAccount.setText( setting.getString("account", null) );

        //背景
        BitmapDrawable bitmapDrawable = getImage.getBimapDrawable(getResources(), R.drawable.personal_background);
        LinearLayout layout = (LinearLayout) findViewById(R.id.per_layout);
        layout.setBackground(bitmapDrawable);


        if ( setting.getString("img", null) == null  )
        {
            img.setImageResource(R.drawable.header);
        }
        else
        {
            Log.i("siang", setting.getString("img", null));
            Bitmap bitmap = BitmapFactory.decodeFile( setting.getString("img", null) );
            img.setImageBitmap(bitmap);
        }


        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent it = new Intent(Intent.ACTION_GET_CONTENT);
                it.setType("image/*");
                startActivityForResult(it, 1);
            }
        });

        setHeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heightPicker();
            }
        });

        setWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weightPicker();
            }
        });

    }

    //右邊更新按鈕
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //身高選擇器
    private void heightPicker()
    {

        final Dialog dialog = new Dialog(PersonalInfoActivity.this);
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

        tall = (NumberPicker)dialog.findViewById(R.id.height_tall);
        tall.setMaxValue(200);
        tall.setMinValue(130);
        tall.setValue( Integer.parseInt(setHeight.getText().toString()) );
        tall.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                tallNum = "" + tall.getValue();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setHeight.setText(tallNum);
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

    private void weightPicker()
    {

        final Dialog dialog = new Dialog(PersonalInfoActivity.this);
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
        heft.setValue( Integer.parseInt(setWeight.getText().toString()) );
        heft.setOnValueChangedListener(new NumberPicker.OnValueChangeListener (){
            public void onValueChange(NumberPicker view, int oldValue, int newValue) {

                heftNum = "" + heft.getValue();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWeight.setText(heftNum);
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

    //回上一頁、更新資料
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() )
        {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_update:
                updateUserData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //照片回應結果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 1) {
            //取得圖檔的路徑位置
            Uri uri = data.getData();
            //抽象資料的接口
            ContentResolver cr = this.getContentResolver();
            try {
                //由抽象資料接口轉換圖檔路徑為Bitmap
                bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                img.setImageBitmap(bitmap);

                //寫入SD卡
                File sdRoot = Environment.getExternalStorageDirectory();
                appRoot = new File(sdRoot, "Android/data/" +  getPackageName() + "/");
                Log.i("siang", appRoot + "");
                if (!appRoot.exists())
                {
                    appRoot.mkdirs();
                    saveImage();
                }
                else
                {
                    saveImage();
                }


            } catch (Exception e) {
                Log.i("siang", e.toString());
            }
        }
    }

    //照片儲存至SD卡
    private void saveImage()
    {
        try {
            File imgPath = new File(appRoot, "header.png");
            FileOutputStream fout = new FileOutputStream(imgPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout);
            fout.flush();
            fout.close();

            setting.edit().putString("img", imgPath + "").commit();

            Intent it = new Intent("img");
            sendBroadcast(it);
        } catch (Exception e) {
            Log.i("siang", e.toString());
        }

    }

    //修改資料
    private void updateUserData()
    {
        new Thread()
        {
            @Override
            public void run() {
                try {
                    String img = setting.getString("img", null);
                    MultipartUtility mp = new MultipartUtility("http://54.238.240.238/pxsport.php", "UTF-8");
                    mp.addFormField("func", "updateUserData");
                    mp.addFormField("id", setting.getString("id", null));
                    mp.addFormField("height", tallNum);
                    mp.addFormField("weight", heftNum);
                    if (img != null) { mp.addFormField("img", img.toString() ); };
                    List<String> ret = mp.finish();
                    if (!ret.get(0).equals("ActionError"))
                    {
                        setting.edit().putString("height", tallNum).commit();
                        setting.edit().putString("weight", heftNum).commit();
                    }
                    myToastHandler.sendEmptyMessage(1);
                } catch (Exception e) {
                    Log.i("siang", e.toString());
                }
            }
        }.start();
    }

    //吐司
    private class MyToastHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast.makeText(PersonalInfoActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
        }
    }

}
