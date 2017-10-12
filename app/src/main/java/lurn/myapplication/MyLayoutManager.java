package lurn.myapplication;

import android.animation.ValueAnimator;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

/**
 * Created by Administrator 可爱的路人 on 2017/10/9.
 *
 */

public class MyLayoutManager extends RecyclerView.LayoutManager {
    private String TAG = "MyLayoutManager";
    private int DEFAULT_SHOW_ITEM = 8;//层数
    private float DEFAULT_SCALE = 0.02f;//每层缩放量
    private float DEFAULT_TRANSLATE_Y = 0.02f;//每层向下偏移量
    private float PER_CARD_THINNER = 1;//每层的高度
    private View firstView;
    private float afterX, afterY, afterRotation;
    private ValueAnimator valueAnimator = null;
    private float rotateRate;
    private int totalDx, totalDy;

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onLayoutChildren(final RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        // 先移除所有view
        removeAllViews();
        // 在布局之前，将所有的子 View 先 Detach 掉，放入到 Scrap 缓存中
        detachAndScrapAttachedViews(recycler);
        int itemCount = getItemCount();
        // 在这里，我们默认配置 CardConfig.DEFAULT_SHOW_ITEM = 3。即在屏幕上显示的卡片数为3
        // 当数据源个数大于最大显示数时
        if (itemCount > DEFAULT_SHOW_ITEM) {
            // 把数据源倒着循环，这样，第0个数据就在屏幕最上面了
            for (int position = DEFAULT_SHOW_ITEM; position >= 0; position--) {
                final View view = recycler.getViewForPosition(position);
                // 将 Item View 加入到 RecyclerView 中
                addView(view);
                // 测量 Item View
                measureChildWithMargins(view, 0, 0);
                // getDecoratedMeasuredWidth(view) 可以得到 Item View 的宽度
                // 所以 widthSpace 就是除了 Item View 剩余的值
                int widthSpace = getWidth() - getDecoratedMeasuredWidth(view);
                // 同理
                int heightSpace = getHeight() - getDecoratedMeasuredHeight(view);
                // 将 Item View 放入 RecyclerView 中布局
                // 在这里默认布局是放在 RecyclerView 中心
                layoutDecoratedWithMargins(view, widthSpace / 2, heightSpace / 2,
                        widthSpace / 2 + getDecoratedMeasuredWidth(view),
                        heightSpace / 2 + getDecoratedMeasuredHeight(view));
                // 其实屏幕上有四张卡片，但是我们把第三张和第四张卡片重叠在一起，这样看上去就只有三张
                // 第四张卡片主要是为了保持动画的连贯性
                view.setElevation(position == DEFAULT_SHOW_ITEM ? PER_CARD_THINNER : (DEFAULT_SHOW_ITEM - position) * PER_CARD_THINNER);
                if (position == DEFAULT_SHOW_ITEM) {
                    // 按照一定的规则缩放，并且偏移Y轴。
                    // CardConfig.DEFAULT_SCALE 默认为0.1f，CardConfig.DEFAULT_TRANSLATE_Y 默认为14
                    view.setScaleX(1 - (position - 1) * DEFAULT_SCALE);
                    view.setScaleY(1 - (position - 1) * DEFAULT_SCALE);
                    view.setTranslationY((position - 1) * view.getMeasuredHeight() * DEFAULT_TRANSLATE_Y);
                } else if (position > 0) {
                    view.setScaleX(1 - position * DEFAULT_SCALE);
                    view.setScaleY(1 - position * DEFAULT_SCALE);
                    view.setTranslationY(position * view.getMeasuredHeight() * DEFAULT_TRANSLATE_Y);
                } else {
                    // 设置 mTouchListener 的意义就在于我们想让处于顶层的卡片是可以随意滑动的
                    // 而第二层、第三层等等的卡片是禁止滑动的
                    if (firstView == null) {
                        firstView = view;
                    }
                    view.setOnTouchListener(mOnTouchListener);
                }
            }
        } else {
            // 当数据源个数小于或等于最大显示数时，和上面的代码差不多
            for (int position = itemCount - 1; position >= 0; position--) {
                final View view = recycler.getViewForPosition(position);
                addView(view);
                measureChildWithMargins(view, 0, 0);
                int widthSpace = getWidth() - getDecoratedMeasuredWidth(view);
                int heightSpace = getHeight() - getDecoratedMeasuredHeight(view);

                layoutDecoratedWithMargins(view, widthSpace / 2, heightSpace / 2,
                        widthSpace / 2 + getDecoratedMeasuredWidth(view),
                        heightSpace / 2 + getDecoratedMeasuredHeight(view));
                view.setElevation((DEFAULT_SHOW_ITEM - position) * PER_CARD_THINNER);
                if (position > 0) {
                    view.setScaleX(1 - position * DEFAULT_SCALE);
                    view.setScaleY(1 - position * DEFAULT_SCALE);
                    view.setTranslationY(position * view.getMeasuredHeight() * DEFAULT_TRANSLATE_Y);
                } else {
                    if (firstView == null) {
                        firstView = view;
                    }
                    view.setOnTouchListener(mOnTouchListener);
                }
            }
        }
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        totalDy -= dy;
        firstView.setTranslationY(firstView.getTranslationY() - dy);
        moveOtherView(totalDx, totalDy);
        return 0;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        totalDx -= dx;
        float rotationAngel = firstView.getRotation() + dx * rotateRate;
        firstView.setTranslationX((float) (totalDx));
        firstView.setRotation(rotationAngel);
        moveOtherView(totalDx, totalDy);
        return 0;
    }

    private void moveOtherView(float totalDx, float totalDy) {
        if (getItemCount() <= 1) {
            return;
        }
        int showall = 1000;
        int z = (int) Math.sqrt(Math.pow(totalDx, 2) + Math.pow(totalDy, 2));
        int zz = Math.min(showall, z);
        Log.i(TAG, "moveOtherView: zz:" + zz);

        for (int i = 1; i < (getItemCount() > DEFAULT_SHOW_ITEM ? DEFAULT_SHOW_ITEM : getItemCount()); i++) {
            CardView viewForPosition = (CardView) findViewByPosition(i);
            Log.i(TAG, "moveOtherView: viewForPosition:" + viewForPosition.getClass().hashCode());
            viewForPosition.setScaleX(1 - (i - (float) zz / showall) * DEFAULT_SCALE);
            viewForPosition.setScaleY(1 - (i - (float) zz / showall) * DEFAULT_SCALE);
            viewForPosition.setTranslationY((i - (float) zz / showall) * viewForPosition.getMeasuredHeight() * DEFAULT_TRANSLATE_Y);
            viewForPosition.setElevation((DEFAULT_SHOW_ITEM - i + (float) zz / showall) * PER_CARD_THINNER);
        }
    }

    @Override
    public void onScrollStateChanged(int state) {
        Log.i(TAG, "onScrollStateChanged: state" + state);

        if (state == 0) {
            //停止
            afterX = firstView.getTranslationX();
            afterY = firstView.getTranslationY();
            afterRotation = firstView.getRotation();
            if (valueAnimator == null) {
                valueAnimator = ValueAnimator.ofFloat(1f, 0f);
                valueAnimator.setDuration(300);
                valueAnimator.setInterpolator(new OvershootInterpolator());
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float animatedValue = (float) animation.getAnimatedValue();
                        firstView.setTranslationX(afterX * animatedValue);
                        firstView.setTranslationY(afterY * animatedValue);
                        firstView.setRotation(afterRotation * animatedValue);
                        moveOtherView(afterX * animatedValue, afterY * animatedValue);
                    }
                });
            }
            valueAnimator.start();
        }
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Log.i(TAG, "touch:down");
                int height = v.getMeasuredHeight();
                rotateRate = (event.getY() / height * 2 - 1) * 0.05f;
                totalDx = 0;
                totalDy = 0;
                if (valueAnimator != null && valueAnimator.isRunning()) {
                    valueAnimator.cancel();
                }
            } else if (MotionEvent.ACTION_UP == event.getAction()) {
                Log.i(TAG, "touch:up");
            }
            return true;
        }
    };
}
