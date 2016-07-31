package tw.com.px.pxsport;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

public class CalroieLeaderBoardFragment extends Fragment {

	private View calorieView;
	private ExpandableListView listView;
	private String[] groupFrom = {"year", "month"}, childFrom = {"row", "userName", "calorie"};
	private int[] groupTo = {R.id.lv_calorie_group_year, R.id.lv_calorie_group_month},
					childTo = {R.id.lv_calorie_item_row, R.id.lv_calorie_item_name, R.id.lv_calorie_item_cal};
	private MyApp myApp;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		myApp = (MyApp) getActivity().getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		calorieView = inflater.inflate(R.layout.calorie_fragment, container, false);
		listView = (ExpandableListView) calorieView.findViewById(R.id.lb_calorie_lv);
		SimpleExpandableListAdapter adapter =
				new SimpleExpandableListAdapter(
						getContext(),
						myApp.leaderCalorieHeader,
						R.layout.listview_calorie_group,
						groupFrom,
						groupTo,
						myApp.leaderBoardCalorieData,
						R.layout.listview_calorie_item,
						childFrom,
						childTo);

		listView.setAdapter(adapter);
		RelativeLayout layout = setListViewHeader();

		return calorieView;
	}

	//listview標題
	private RelativeLayout setListViewHeader()
	{
		RelativeLayout rootLayout = new RelativeLayout(getContext());

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
		speed.setText("消耗卡路里");
		speed.setTextSize(15);
		RelativeLayout.LayoutParams speedParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		speedParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		speed.setLayoutParams(speedParam);

		rootLayout.addView(row, 0);
		rootLayout.addView(name, 1);
		rootLayout.addView(speed, 2);

		return rootLayout;
	}



}
