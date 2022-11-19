package com.wl.wlflatproject.MView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.worthcloud.avlib.widget.VideoPlayView;


public class WJAVideoView extends VideoPlayView {

    private int mRatioWidth;
    private int mRatioHeight;


    public WJAVideoView(Context context) {
        super(context);
    }

    public WJAVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WJAVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAspectRatio(int width, int height) {
        if (width >= 0 && height >= 0) {
            this.mRatioWidth = width;
            this.mRatioHeight = height;
        }
    }


    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);
        if (0 != this.mRatioWidth && 0 != this.mRatioHeight) {
            if (width > height * this.mRatioWidth / this.mRatioHeight) {
                this.setMeasuredDimension(width, width * this.mRatioHeight / this.mRatioWidth);
            } else {
                this.setMeasuredDimension(height * this.mRatioWidth / this.mRatioHeight, height);
            }
        } else {
            this.setMeasuredDimension(width, height);
        }

    }
}
