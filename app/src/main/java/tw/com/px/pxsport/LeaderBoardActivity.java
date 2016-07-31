package tw.com.px.pxsport;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.LinkagePager;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.crosswall.lib.coverflow.CoverFlow;
import me.crosswall.lib.coverflow.core.LinkagePagerContainer;

public class LeaderBoardActivity extends ActionBarActivity {

    private Toolbar toolbar;
    private FragmentTabHost tabHost;
    private GetImage getImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);

        getImage = new GetImage();

        // ToolBar
        toolbar = (Toolbar)findViewById(R.id.lb_myToobar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("排行榜");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //背景
        BitmapDrawable bitmapDrawable = getImage.getBimapDrawable(getResources(), R.drawable.leaderboard_bacground);
        LinearLayout layout = (LinearLayout) findViewById(R.id.lb_layout);
        layout.setBackground(bitmapDrawable);

        tabHost = (FragmentTabHost)findViewById(R.id.tabhost);
        tabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        //新增頁籤
        tabHost.addTab(tabHost.newTabSpec("route").setIndicator("路線"), RouteLeaderBoardFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("calorie").setIndicator("卡路里"), CalroieLeaderBoardFragment.class, null);
//        tabHost.getTabWidget().setBackgroundColor(Color.parseColor("#DADFE3"));
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++)
        {
            TextView widget = (TextView) tabHost.getTabWidget().getChildTabViewAt(i).findViewById(android.R.id.title);
            widget.setTextSize(18);
            widget.setGravity(Gravity.CENTER);
            widget.setTextColor(Color.parseColor("#FDFDFD"));
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
