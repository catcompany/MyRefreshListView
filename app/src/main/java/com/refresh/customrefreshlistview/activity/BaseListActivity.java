package com.refresh.customrefreshlistview.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.refresh.customrefreshlistview.MyCustomAdapter;
import com.refresh.customrefreshlistview.R;
import com.refresh.customrefreshlistview.listview.BaseRefreshListView;
import com.refresh.customrefreshlistview.utils.StatusBarUtil;

public class BaseListActivity extends Activity implements BaseRefreshListView.OnListRefreshListener {

    private BaseRefreshListView mBaseRefreshListView;
    private MyCustomAdapter mMyCustomAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_list_activity);
        StatusBarUtil.setActivityTranslucent(this);
        mBaseRefreshListView = (BaseRefreshListView) findViewById(R.id.listview);
        mMyCustomAdapter = new MyCustomAdapter(this);
        mMyCustomAdapter.updateFirstList("比较传统的ListView,基于它去改造");
        mBaseRefreshListView.setAdapter(mMyCustomAdapter);
        mBaseRefreshListView.setOnRefreshListener(this);
    }

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
                if (mBaseRefreshListView != null) {
                    mBaseRefreshListView.onRefreshFinish();
                    mMyCustomAdapter.notifyDataSetChanged();
                    if (isRefresh) {
                        //刷新完成后，item会回弹到屏幕之外，需要重新定位回来。
                        mBaseRefreshListView.setSelection(0);
                    }
                }
            }
        }, 3500);
    }

}
