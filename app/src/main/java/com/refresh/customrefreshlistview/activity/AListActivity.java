package com.refresh.customrefreshlistview.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.refresh.customrefreshlistview.MyCustomAdapter;
import com.refresh.customrefreshlistview.R;
import com.refresh.customrefreshlistview.listview.AlphaRefreshListView;
import com.refresh.customrefreshlistview.listview.BaseRefreshListView;
import com.refresh.customrefreshlistview.utils.DensityUtil;
import com.refresh.customrefreshlistview.utils.GradationUtil;
import com.refresh.customrefreshlistview.utils.StatusBarUtil;

public class AListActivity extends Activity implements BaseRefreshListView.OnListRefreshListener, AlphaRefreshListView.onScrollYListener {

    private AlphaRefreshListView mAlphaRefreshListView;
    private MyCustomAdapter mMyCustomAdapter;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_list_activity);
        StatusBarUtil.setActivityTranslucent(this);
        mAlphaRefreshListView = (AlphaRefreshListView) findViewById(R.id.listview);
        textView = (TextView) findViewById(R.id.text);

        View headImage = LayoutInflater.from(this).inflate(R.layout.item_image, null);
        mAlphaRefreshListView.addHeaderView(headImage);

        DensityUtil.measureView(textView);
        int textViewHeight = textView.getMeasuredHeight();
        mAlphaRefreshListView.setRefreshStart(textViewHeight);

        mMyCustomAdapter = new MyCustomAdapter(this);
        mAlphaRefreshListView.setAdapter(mMyCustomAdapter);
        mAlphaRefreshListView.setOnRefreshListener(this);
        mAlphaRefreshListView.setOnScrollYListener(this);

        textView.getBackground().setAlpha(0);
        gradationUtil = new GradationUtil(textView);
    }
    GradationUtil gradationUtil;
    @Override
    public void onRefresh() {
        delayRefreshFinish(true);
    }

    @Override
    public void onLoadingMore() {
        delayRefreshFinish(false);
    }

    /**
     * 延迟刷新完成
     */
    private void delayRefreshFinish(final boolean isRefresh) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (mAlphaRefreshListView != null) {
                    mAlphaRefreshListView.onRefreshFinish();
                    mMyCustomAdapter.notifyDataSetChanged();
                    if (isRefresh) {
                        //下拉刷新完成后，item会回弹到屏幕之外，需要重新定位回来。
                        mAlphaRefreshListView.setSelection(0);
                    }
                }
            }
        }, 500);
    }

    @Override
    public void onScrollY(int y) {
        gradationUtil.setTitleAlpha(y, mAlphaRefreshListView.isDropDown());
    }
}
