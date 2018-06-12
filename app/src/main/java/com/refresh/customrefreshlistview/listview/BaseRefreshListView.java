package com.refresh.customrefreshlistview.listview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.refresh.customrefreshlistview.R;
import com.refresh.customrefreshlistview.utils.DensityUtil;

import java.util.HashMap;

/**
 * 下拉刷新基类
 * 具有下拉刷新、加载更多功能
 * Created by HuangZc on 2018/05/30.
 */
public class BaseRefreshListView extends ListView implements OnScrollListener {

    public static final int DONE = 0;
    public static final int PULL_TO_REFRESH = 1;
    public static final int RELEASE_TO_REFRESH = 2;
    protected static final int REFRESHING = 3;
    protected static final float RATIO = 1.5f;
    protected final static int DELAYMILLIS = 300;
    protected View endLine;                   // 头布局刷新下方的线条
    protected RelativeLayout rlHead;          // 头布局下第一个layout
    protected View headerView;                // 头布局
    protected View footerView;                // 脚布局
    protected TextView tvState;               // 头布局刷新状态
    protected ImageView mProgressBar;         // 头布局的进度条
    protected TextView tvLastUpdateTime;      // 头布局的最后刷新时间
    protected ImageView mFooterImage;
    protected int mHeaderViewHeight;           // 头布局的高度
    protected int mFooterViewHeight;           // 脚布局的高度
    protected int mFirstVisibleItem;           // 滚动时界面显示在顶部的item的position
    protected boolean isScroll2Bottom = false; //是否滚动到底部
    protected boolean canLoadMore = true;      //可以加载更多
    protected boolean isLoadMoving = false;    //是否正在加载更多中
    protected String lastUpdateTimeText;       //获得最后刷新时间文案
    protected String refreshingText;
    protected Animation headOperatingAnim;
    protected int state;
    protected float startY;
    protected float offsetY;
    protected boolean isEnd;
    protected boolean isRecord;
    protected boolean isRefreable;
    protected ValueAnimator mAnimator;
    protected OnListRefreshListener mOnRefreshListener;
    protected OnAnimatorListener mOnAnimatorListener;
    protected OnListViewAnimatorUpdateListener mOnUpdateListener;
    //直接用于手势判断：上拉还是下拉
    protected float mDownY;
    protected float mMoveY;
    protected HashMap recordSp;
    protected boolean isChange;//控制代码在执行时，只执行一次
    protected int mRefreshStart = 0;

    protected Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (state == DONE) {
                setValueAnimator(mRefreshStart, -mHeaderViewHeight);
            } else {
                handler.sendEmptyMessageDelayed(1, DELAYMILLIS);
            }
        }
    };

    public BaseRefreshListView(Context context) {
        super(context);
        init();
    }

    public BaseRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BaseRefreshListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (isInEditMode()) {
            return;
        }
        recordSp = new HashMap();
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        setOnScrollListener(this);
        initHeader();
        initFooter();
        state = DONE;
        isEnd = true;
        isRefreable = false;
        initValueAnimatorListener();
    }

    /**
     * 定义下拉刷新起始位置
     *
     * @param sRefreshStart
     */
    public void setRefreshStart(int sRefreshStart) {
        this.mRefreshStart = sRefreshStart;
    }

    /**
     * 初始化脚布局
     */
    private void initFooter() {
        footerView = LayoutInflater.from(getContext()).inflate(R.layout.common_listview_refresh_footer, null);
        mFooterImage = (ImageView) footerView.findViewById(R.id.common_refresh_loading);

        DensityUtil.measureView(footerView); // 测量一下脚布局的高度
        mFooterViewHeight = footerView.getMeasuredHeight();

        footerView.setPadding(0, -mFooterViewHeight, 0, 0);// 隐藏脚布局
        this.addFooterView(footerView, null, true);
        footerAnimation();
    }

    /**
     * 初始化头布局
     */
    private void initHeader() {
        headerView = LayoutInflater.from(getContext()).inflate(R.layout.common_listview_refresh_header, null);
        rlHead = (RelativeLayout) headerView.findViewById(R.id.rl_head);
        mProgressBar = (ImageView) headerView.findViewById(R.id.pb_listview_header_progress);
        tvState = (TextView) headerView.findViewById(R.id.tv_listview_header_state);
        endLine = headerView.findViewById(R.id.tv_listview_header_last_end_line);
        tvLastUpdateTime = (TextView) headerView.findViewById(R.id.tv_listview_header_last_update_time);
        tvLastUpdateTime.setText(getLastUpdateTimeText());

        DensityUtil.measureView(headerView);
        mHeaderViewHeight = headerView.getMeasuredHeight();

        initHeadViewPaddingTop();
        addHeaderView(headerView);
        initAnim();
        setEndLineVisibility(View.GONE);
    }

    protected void initHeadViewPaddingTop() {
        headerView.setPadding(0, -mHeaderViewHeight, 0, 0);
    }

    protected void initAnim() {
        headOperatingAnim = AnimationUtils.loadAnimation(getContext(), R.anim.refresh_loading);
        headOperatingAnim.setInterpolator(new LinearInterpolator());
    }

    public void setCanLoadMore(boolean isCanLoadMore) {
        canLoadMore = isCanLoadMore;
    }

    public void setEndLineVisibility(int visibility) {
        endLine.setVisibility(visibility);
    }

    /**
     * 头部动画
     */
    protected void headerAnimation() {
        mProgressBar.startAnimation(headOperatingAnim);
    }

    /**
     * 尾部动画
     */
    protected void footerAnimation() {
        Animation operatingAnim = AnimationUtils.loadAnimation(getContext(), R.anim.loading);
        if (operatingAnim != null) {
            LinearInterpolator lin = new LinearInterpolator();
            operatingAnim.setInterpolator(lin);
            mFooterImage.startAnimation(operatingAnim);
        }
    }

    public void hideFootView() {
        isLoadMoving = false;
        footerView.setPadding(0, -mFooterViewHeight, 0, 0);
    }

    protected String getLastUpdateTimeText() {
        return lastUpdateTimeText == null ? getResources().getString(R.string.lastUpdateTimeText) : lastUpdateTimeText;
    }

    public void setlastUpdateTimeText(String lastUpdateTimeText) {
        this.lastUpdateTimeText = lastUpdateTimeText;
    }

    protected String pullDownText() {
        return "下拉刷新";
    }

    protected String releaseRefreshText() {
        return "释放刷新...";
    }

    protected String refreshingText() {
        return refreshingText == null ? "努力刷新中..." : refreshingText;
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


    public void onRefreshFinish() {
        if (isLoadMoving) {
            // 隐藏脚布局
            isLoadMoving = false;
            footerView.setPadding(0, -mFooterViewHeight, 0, 0);
        } else {
            isEnd = true;
            state = DONE;
            changeHeaderByState(state);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
                || scrollState == OnScrollListener.SCROLL_STATE_FLING) {
            if (isScroll2Bottom && !isLoadMoving) {                // 滚动到底部
                // 加载更多
                if (!canLoadMore) {
                    return;
                }
                footerView.setPadding(0, 0, 0, 0);
                this.setSelection(this.getCount());                // 滚动到ListView的底部
                isLoadMoving = true;

                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onLoadingMore();
                }
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mFirstVisibleItem = firstVisibleItem;
        isScroll2Bottom = (firstVisibleItem + visibleItemCount) >= totalItemCount
                && totalItemCount > 0;
    }

    /**
     * 设置刷新的监听事件
     */
    public void setOnRefreshListener(OnListRefreshListener listener) {
        this.mOnRefreshListener = listener;
        isRefreable = true;
    }

    public void setmOnAnimatorListener(OnAnimatorListener mOnAnimatorListener) {
        this.mOnAnimatorListener = mOnAnimatorListener;
    }

    public void setmOnUpdateListener(OnListViewAnimatorUpdateListener mOnUpdateListener) {
        this.mOnUpdateListener = mOnUpdateListener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                mMoveY = ev.getY() - mDownY;
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent ev, boolean isdeal) {
        if (isdeal) {
            return super.onTouchEvent(ev);
        }
        return isdeal;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isEnd) {//如果现在时结束的状态，即刷新完毕了，可以再次刷新了，在onRefreshComplete中设置
            if (isRefreable) {//如果现在是可刷新状态   在setOnMeiTuanListener中设置为true
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
                                    //changeHeaderByState(state);
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
                                    //changeHeaderByState(state);
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
                            //this.smoothScrollBy((int) (-mHeaderViewHeight + offsetY / RATIO) + mHeaderViewHeight, 500);
                            setValueAnimator(paddingtop, -mHeaderViewHeight);
                            //根据状态改变headerView
                            changeHeaderByState(state);
                        }
                        //如果当前状态为放开刷新
                        if (state == RELEASE_TO_REFRESH) {
                            //平滑的滑到正好显示headerView
                            //this.smoothScrollBy((int) (-mHeaderViewHeight + offsetY / RATIO), 500);
                            setValueAnimator(paddingtop, mRefreshStart, true);
                            //将当前状态设置为正在刷新
                            state = REFRESHING;
                            //回调接口的onRefresh方法
                            if (mOnRefreshListener != null) {
                                mOnRefreshListener.onRefresh();
                            }
                            //根据状态改变headerView
                            //changeHeaderByState(state);
                        }
                        //这一套手势执行完，一定别忘了将记录y坐标的isRecord改为false，以便于下一次手势的执行
                        isRecord = false;
                        break;
                }
            }
        }
        return super.onTouchEvent(ev);
    }

    protected void changeHeaderByState(int state) {
        switch (state) {
            case DONE://隐藏状态
                //设置headerView的padding为隐藏
                //此处不做处理
                //自动刷新 isAutoRefreshing为 true，在Up时自动恢复状态，
                //下拉刷新 在回弹到显示headerView时，等待为DONE即可
                break;
            case RELEASE_TO_REFRESH://当前状态为放开刷新
                //文字显示为放开刷新
                tvState.setText(releaseRefreshText());
                headerAnimation();
                break;
            case PULL_TO_REFRESH://当前状态为下拉刷新
                //设置文字为下拉刷新
                tvState.setText(pullDownText());
                headerAnimation();
                break;
            case REFRESHING://当前状态为正在刷新
                //文字设置为正在刷新
                tvState.setText(refreshingText());
                mProgressBar.setVisibility(VISIBLE);
                headerAnimation();
                break;
            default:
                break;
        }
    }

    //缓慢回弹动画设置
    protected void setValueAnimator(int fromValue, int toValue) {
        setValueAnimator(fromValue, toValue, false);
    }

    public class myInter implements Interpolator {

        @Override
        public float getInterpolation(float v) {
            return 0;
        }
    }
    protected void setValueAnimator(int fromValue, int toValue, boolean isRefreshing) {
        isChange = true;
        mAnimator = ValueAnimator.ofInt(fromValue, toValue);
        mAnimator.setInterpolator(new DecelerateInterpolator(2.0f));
        mAnimator.setDuration(getDuration(fromValue, toValue));

        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mOnUpdateListener != null) {
                    mOnUpdateListener.onAnimationUpdate(animation);
                }
            }
        });
        if (isRefreshing) {
            mAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    if (mOnAnimatorListener != null) {
                        mOnAnimatorListener.onAnimationStart(animator);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (mOnAnimatorListener != null) {
                        mOnAnimatorListener.onAnimationEnd(animator);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    if (mOnAnimatorListener != null) {
                        mOnAnimatorListener.onAnimationCancel(animator);
                    }
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                    if (mOnAnimatorListener != null) {
                        mOnAnimatorListener.onAnimationRepeat(animator);
                    }
                }
            });
        }
        mAnimator.start();
    }

    private int getDuration(int fromValue, int toValue) {
        int duration = fromValue - toValue;
        if (state == REFRESHING) {
            duration = duration / 2;
        } else {
            if (duration < 200) {
                duration = 200;
            }
        }
        return duration;
    }

    public interface OnAnimatorListener {
        void onAnimationStart(Animator animator);

        void onAnimationEnd(Animator animator);

        void onAnimationCancel(Animator animator);

        void onAnimationRepeat(Animator animator);
    }

    public interface OnListViewAnimatorUpdateListener {
        void onAnimationUpdate(ValueAnimator animation);
    }

    public interface OnListRefreshListener {

        /**
         * 下拉刷新执行的刷新任务, 使用时,
         * 当刷新完毕之后, 需要手动的调用onRefreshFinish(), 去隐藏头布局
         */
        void onRefresh();

        /**
         * 当加载更多时回调
         * 当加载更多完毕之后, 需要手动的调用onRefreshFinish(), 去隐藏脚布局
         */
        void onLoadingMore();
    }

    class ItemRecod {
        int height = 0;
        int top = 0;
        int paddingTop = 0;
    }
}
