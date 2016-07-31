package tw.com.px.pxsport;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.InputStream;

/**
 * Created by HONG SIANG on 2016/7/22.
 */
public class GetImage {

    public BitmapDrawable getBimapDrawable(Resources res, int id)
    {
        Bitmap background = BitmapFactory.decodeResource(res, id, getBitmapOption());
        BitmapDrawable bitmapDrawable = new BitmapDrawable(res, background);
        bitmapDrawable.setAlpha(50);
        return bitmapDrawable;
    }
    public Bitmap getBitmap(InputStream in)
    {
        Bitmap bitmap = BitmapFactory.decodeStream(in, null, getBitmapOption());
        return bitmap;
    }
    //把圖片設定為小圖片
    public BitmapFactory.Options getBitmapOption()
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inInputShareable = true;
        //值越大，解析度越小
        options.inSampleSize = 2;
        return options;
    }

}
