package com.refresh.customrefreshlistview.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.refresh.customrefreshlistview.AInterpolator;
import com.refresh.customrefreshlistview.AlphaTextView;
import com.refresh.customrefreshlistview.MyCustomAdapter;
import com.refresh.customrefreshlistview.R;
import com.refresh.customrefreshlistview.listview.BaseRefreshListView;
import com.refresh.customrefreshlistview.listview.DetailRefreshListView;
import com.refresh.customrefreshlistview.utils.DensityUtil;
import com.refresh.customrefreshlistview.utils.GradationUtil;
import com.refresh.customrefreshlistview.utils.StatusBarUtil;

public class BListActivity extends Activity implements BaseRefreshListView.OnListRefreshListener, DetailRefreshListView.OnScrollAlphaListener {

    private DetailRefreshListView mAlphaRefreshListView;
    private MyCustomAdapter mMyCustomAdapter;
    private AlphaTextView textView;
    private TextView tvBg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.b_list_activity);
        StatusBarUtil.setActivityTranslucent(this);
        StatusBarUtil.setStatusBarBg(this, getResources().getColor(R.color.transparent));

        mAlphaRefreshListView = (DetailRefreshListView) findViewById(R.id.listview);
        textView = (AlphaTextView) findViewById(R.id.text);
        tvBg = (TextView) findViewById(R.id.tv_banner_bg);

        View headImage = LayoutInflater.from(this).inflate(R.layout.item_image, null);
        mAlphaRefreshListView.addHeaderView(headImage);

        DensityUtil.measureView(textView);
        int textViewHeight = textView.getMeasuredHeight();
        mAlphaRefreshListView.setRefrestStart(textViewHeight);

        mMyCustomAdapter = new MyCustomAdapter(this);
        mAlphaRefreshListView.setAdapter(mMyCustomAdapter);
        mAlphaRefreshListView.setOnRefreshListener(this);
        mAlphaRefreshListView.setOnScrollAlphaListener(this);

        textView.getBackground().setAlpha(0);
        textView.setInterpolator(new AInterpolator());
    }

    @Override
    public void onRefresh() {
        delayRefreshFinish();
    }

    @Override
    public void onLoadingMore() {
        mAlphaRefreshListView.hideFootView();
    }

    /**
     * 延迟刷新完成
     */
    private void delayRefreshFinish() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (mAlphaRefreshListView != null) {
                    mAlphaRefreshListView.onRefreshFinish();
                    mMyCustomAdapter.notifyDataSetChanged();
                    //下拉刷新完成后，item会回弹到屏幕之外，需要重新定位回来。
                    mAlphaRefreshListView.setSelection(0);
                }
            }
        }, 500);
    }

    @Override
    public void onScrollAlpha(int alpha) {
        textView.setDetailAlpha(alpha, tvBg);
    }

}