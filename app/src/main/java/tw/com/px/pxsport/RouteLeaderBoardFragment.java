package tw.com.px.pxsport;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.LinkagePager;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import me.crosswall.lib.coverflow.CoverFlow;
import me.crosswall.lib.coverflow.core.LinkagePagerContainer;

public class RouteLeaderBoardFragment extends Fragment {

	private LinkagePagerContainer pagerContainer;
	private LinkagePager upPager, bottomPager;
	private MyApp myApp;
	private View routeView;
	private MyUpPagerAdapter myUpPagerAdapter;
	private MyBottomAdapter myBottomAdapter;
	private String[] from = {"row", "userName", "speed"};
	private int[] to = {R.id.lv_route_row, R.id.lv_route_username, R.id.lv_route_speed};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		myApp = (MyApp) getActivity().getApplication();
		myUpPagerAdapter = new MyUpPagerAdapter();
		myBottomAdapter = new MyBottomAdapter();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		//upPager
		routeView = inflater.inflate(R.layout.route_fragment, container, false);
		pagerContainer = (LinkagePagerContainer)routeView.findViewById(R.id.lb_myToobar);
		upPager = (LinkagePager)routeView.findViewById(R.id.lb_route_up_pager);
		upPager.setAdapter(myUpPagerAdapter);
		upPager.setOffscreenPageLimit(5);
		new CoverFlow.Builder()
				.withLinkage(upPager)
				.pagerMargin(0f)
				.scale(0.3f)
				.spaceSize(0f)
				.rotationY(25f)
				.build();
		bottomPager = (LinkagePager)routeView.findViewById(R.id.lb_route_bottom_pager);
		bottomPager.setAdapter(myBottomAdapter);
		bottomPager.setOffscreenPageLimit(5);

		upPager.setLinkagePager(bottomPager);
		bottomPager.setLinkagePager(upPager);

		return routeView;
	}

	//路線圖片
	private class MyUpPagerAdapter extends PagerAdapter
	{
		@Override
		public int getCount() {
			return myApp.leaderBoardImg.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ImageView imageView = new ImageView(getContext());
			imageView.setImageBitmap( myApp.leaderBoardImg.get(position) );
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			container.addView( imageView );
			return imageView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
	}

	//排行榜
	private class MyBottomAdapter extends PagerAdapter
	{
		@Override
		public int getCount() {
			return myApp.leaderBoardRouteData.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ListView listView = new ListView(getContext());
			SimpleAdapter adapter = new SimpleAdapter(
					getContext(), myApp.leaderBoardRouteData.get(position), R.layout.listview_route_leaderboard, from, to);

			// listview標題
			LinearLayout linear = setListViewHeader(position);

			listView.addHeaderView(linear);
			listView.setAdapter(adapter);
			listView.setPadding(15, 0, 15 ,0);
			container.addView(listView);
			return listView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
	}

	//listview標題
	private LinearLayout setListViewHeader(int position)
	{
		LinearLayout rootLayout = new LinearLayout(getContext());
		rootLayout.setOrientation(LinearLayout.VERTICAL);

		//路線名稱
		TextView header = new TextView(getContext());
		header.setText( myApp.leaderBoardRoute.get(position).get("name") );
		header.setGravity(Gravity.CENTER);
		header.setTextSize(17);
		header.setTextColor(Color.parseColor("#FDFDFD"));
		header.setBackgroundColor(Color.parseColor("#424253"));

		RelativeLayout secondLayout = new RelativeLayout(getContext());

		//名次
		TextView row = new TextView(getContext());
		row.setText("名次");
		row.setId(R.id.list_header_row);
		row.setTextSize(15);
		RelativeLayout.LayoutParams rowParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		rowParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		row.setLayoutParams(rowParam);

		//姓名
		TextView name = new TextView(getContext());
		name.setText("姓名");
		name.setTextSize(15);
		RelativeLayout.LayoutParams nameParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		nameParam.addRule(RelativeLayout.RIGHT_OF, R.id.list_header_row);
		nameParam.leftMargin = 15;
		name.setLayoutParams(nameParam);

		//平均時速
		TextView speed = new TextView(getContext());
		speed.setText("平均時速");
		speed.setTextSize(15);
		RelativeLayout.LayoutParams speedParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		speedParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		speed.setLayoutParams(speedParam);

		secondLayout.addView(row, 0);
		secondLayout.addView(name, 1);
		secondLayout.addView(speed, 2);
		rootLayout.addView(header, 0);
		rootLayout.addView(secondLayout, 1);

		return rootLayout;
	}


}