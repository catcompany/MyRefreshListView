package com.refresh.customrefreshlistview.listview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * 一、定义下拉位置setRefreshStart
 * 二、下拉控制Title背景色透明度
 * 1、AlphaRefreshListView设置setOnScrollYListener监听
 * 2、得到scrollY的值改变背景色透明度
 * 注： 该类继承BaseRefreshListView（最基本的下拉刷新、控件的初始化）
 * Created by HuangZc on 2018/04/24.
 */
public class AlphaRefreshListView extends BaseRefreshListView implements OnScrollListener {

    boolean isDropDown = false;

    private onScrollYListener onScrollYListener;

    public AlphaRefreshListView(Context context) {
        super(context);
        init();
    }

    public AlphaRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AlphaRefreshListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        headerView.setVisibility(VISIBLE);
        initValueAnimatorListener();
        mRefreshStart = 0;
    }

    protected void initValueAnimatorListener() {
        setmOnUpdateListener(new OnListViewAnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int curValue = (int) animation.getAnimatedValue();
                headerView.setPadding(0, curValue, 0, 0);

                if (curValue < mRefreshStart) {
                    if (isChange) {
                        tvState.setText(pullDownText());
                        headerAnimation();
                        headerView.setVisibility(INVISIBLE);
                        isChange = false;
                    }
                }
            }
        });

        setmOnAnimatorListener(new OnAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                changeHeaderByState(REFRESHING);
                handler.sendEmptyMessageDelayed(1, DELAYMILLIS);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

        View firstView = view.getChildAt(0);
        if (null != firstView) {
            ItemRecod itemRecord = (ItemRecod) recordSp.get("" + mFirstVisibleItem);
            if (null == itemRecord) {
                itemRecord = new ItemRecod();
            }
            itemRecord.height = firstView.getHeight();
            itemRecord.top = firstView.getTop();
            itemRecord.paddingTop = firstView.getPaddingTop();
            recordSp.put("" + mFirstVisibleItem, itemRecord);
        }

        int listScrollY = getListScrollY();

        if (firstView != null) {
            int y = listScrollY + mHeaderViewHeight;
            if (firstVisibleItem == 0 && mMoveY > 0) {
                isDropDown = true;
            } else {
                isDropDown = false;
            }
            if (onScrollYListener != null) {
                onScrollYListener.onScrollY(y);
            }
        }
    }

    public boolean isDropDown() {
        return isDropDown;
    }

    public void setOnScrollYListener(onScrollYListener onScrollYListener) {
        this.onScrollYListener = onScrollYListener;
    }

    private int getListScrollY() {
        int height = 0;
        for (int i = 0; i < mFirstVisibleItem; i++) {
            ItemRecod itemRecod = (ItemRecod) recordSp.get("" + i);
            if (itemRecod != null) {
                height += itemRecod.height;
            }
        }
        ItemRecod itemRecod = (ItemRecod) recordSp.get("" + mFirstVisibleItem);
        if (null == itemRecod) {
            itemRecod = new ItemRecod();
        }
        Log.i("yiguanalpah", " top:" + itemRecod.paddingTop + " mFirstVisibleItem:" + mFirstVisibleItem);
        return height - itemRecod.top + itemRecod.paddingTop;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isEnd) {//如果现在时结束的状态，即刷新完毕了，可以再次刷新了，在onRefreshComplete中设置
            if (isRefreable) {//如果现在是可刷新状态   在setOnRefreshListener中设置为true
                switch (ev.getAction()) {
                    //用户按下
                    case MotionEvent.ACTION_DOWN:
                        //如果当前是在listview顶部并且没有记录y坐标
                        if (mFirstVisibleItem == 0 && !isRecord) {
                            //将isRecord置为true，说明现在已记录y坐标
                            isRecord = true;
                            //将当前y坐标赋值给startY起始y坐标
                            startY = ev.getY();
                        }
                        break;
                    //用户滑动
                    case MotionEvent.ACTION_MOVE:
                        //再次得到y坐标，用来和startY相减来计算offsetY位移值
                        float tempY = ev.getY();
                        //再起判断一下是否为listview顶部并且没有记录y坐标
                        if (mFirstVisibleItem == 0 && !isRecord) {
                            isRecord = true;
                            startY = tempY;
                        }
                        //如果当前状态不是正在刷新的状态，并且已经记录了y坐标
                        if (state != REFRESHING && isRecord) {
                            //计算y的偏移量
                            offsetY = tempY - startY;
                            //计算当前滑动的高度
                            if (-mHeaderViewHeight + offsetY / RATIO > mRefreshStart - mHeaderViewHeight) {
                                headerView.setVisibility(VISIBLE);
                            } else {
                                headerView.setVisibility(INVISIBLE);
                            }
                            //如果当前的状态是放开刷新，并且已经记录y坐标
                            if (state == RELEASE_TO_REFRESH && isRecord) {
                                setSelection(0);
                                //如果当前滑动的距离小于headerView的总高度
                                if (-mHeaderViewHeight + offsetY / RATIO < mRefreshStart) {
                                    //将状态置为下拉刷新状态
                                    state = PULL_TO_REFRESH;
                                    //根据状态改变headerView，主要是更新动画和文字等信息
                                    changeHeaderByState(state);
                                    //如果当前y的位移值小于0，即为headerView隐藏了
                                } else if (offsetY <= 0) {
                                    //将状态变为done
                                    state = DONE;
                                    //根据状态改变headerView，主要是更新动画和文字等信息
                                    changeHeaderByState(state);
                                }
                            }
                            //如果当前状态为下拉刷新并且已经记录y坐标
                            if (state == PULL_TO_REFRESH && isRecord) {
                                setSelection(0);
                                //如果下拉距离大于等于headerView的总高度
                                if (-mHeaderViewHeight + offsetY / RATIO >= mRefreshStart) {
                                    //将状态变为放开刷新
                                    state = RELEASE_TO_REFRESH;
                                    //根据状态改变headerView，主要是更新动画和文字等信息
                                    changeHeaderByState(state);
                                    //如果当前y的位移值小于0，即为headerView隐藏了
                                } else if (offsetY <= 0) {
                                    //将状态变为done
                                    state = DONE;
                                    //根据状态改变headerView，主要是更新动画和文字等信息
                                    changeHeaderByState(state);
                                }
                            }
                            //如果当前状态为done并且已经记录y坐标
                            if (state == DONE && isRecord) {
                                //如果位移值大于0
                                if (offsetY >= 0) {
                                    //将状态改为下拉刷新状态
                                    state = PULL_TO_REFRESH;
                                }
                            }
                            //如果为下拉刷新状态
                            if (state == PULL_TO_REFRESH) {
                                //则改变headerView的padding来实现下拉的效果
                                headerView.setPadding(0, (int) (-mHeaderViewHeight + offsetY / RATIO), 0, 0);
                            }
                            //如果为放开刷新状态
                            if (state == RELEASE_TO_REFRESH) {
                                //改变headerView的padding值
                                headerView.setPadding(0, (int) (-mHeaderViewHeight + offsetY / RATIO), 0, 0);
                            }
                        }
                        break;
                    //当用户手指抬起时
                    case MotionEvent.ACTION_UP:
                        int paddingtop = (int) (-mHeaderViewHeight + offsetY / RATIO);
                        //如果当前状态为下拉刷新状态
                        if (state == PULL_TO_REFRESH) {
                            //平滑的隐藏headerView
                            setValueAnimator(paddingtop, -mHeaderViewHeight);
                            //根据状态改变headerView
                            changeHeaderByState(state);
                        }
                        //如果当前状态为放开刷新
                        if (state == RELEASE_TO_REFRESH) {
                            //平滑的滑到正好显示headerView
                            setValueAnimator(paddingtop, mRefreshStart, true);
                            //将当前状态设置为正在刷新
                            state = REFRESHING;
                            //回调接口的onRefresh方法
                            if (mOnRefreshListener != null) {
                                mOnRefreshListener.onRefresh();
                            }
                            //根据状态改变headerView
                            changeHeaderByState(state);
                        }
                        //这一套手势执行完，一定别忘了将记录y坐标的isRecord改为false，以便于下一次手势的执行
                        isRecord = false;
                        break;
                }
            }
        }
        return super.onTouchEvent(ev, true);
    }

    public interface onScrollYListener {
        void onScrollY(int y);
    }

}
