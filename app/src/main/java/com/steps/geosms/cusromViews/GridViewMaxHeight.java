package com.steps.geosms.cusromViews;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Interpolator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.GridView;

import com.ioane.sharvadze.geosms.R;

/**
 * GridView With Maximum size
 * Created by ioane on 6/19/15.
 */
public class GridViewMaxHeight extends GridView{
    private final int maxHeight;

    public GridViewMaxHeight(Context context) {
        this(context, null);
    }

    public GridViewMaxHeight(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GridViewMaxHeight(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.GridViewMaxHeight);
            maxHeight = a.getDimensionPixelSize(R.styleable.GridViewMaxHeight_maxHeight, Integer.MAX_VALUE);
            a.recycle();
        } else {
            maxHeight = 0;
        }
    }


//    @Override
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        super.onSizeChanged(w, h, oldw, oldh);
//        /**
//         *    ScaleAnimation scale = new ScaleAnimation((float)oldw, (float)w, (float)oldh, (float)h);
//         scale.setFillAfter(true);
//         scale.setDuration(500);
//         startAnimation(scale);
//         animate();
//         */
//
//        float min = Math.min(oldh, h);
//
//        Log.i("bja", " " +oldh + " " + h + " " + h / min);
//
//        ScaleAnimation animation = new ScaleAnimation(1, 1, 1, h/min);
//        animation.setFillAfter(false);
//        animation.setFillBefore(true);
//        animation.setBackgroundColor(getContext().getResources().getColor(R.color.themePrimary));
//       // Animation animation = AnimationUtils.loadAnimation(getContext(), android.R.anim.anticipate_interpolator);
//
//        animation.setDuration(5000);
//        setAnimation(animation);
//        animate();
//        animation.start();
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (maxHeight > 0 && maxHeight < measuredHeight) {
            int measureMode = MeasureSpec.getMode(heightMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, measureMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
