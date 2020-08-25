package me.ictlinkbd.com.projectalpha.util;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewParent;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;

public class CustomMapView extends MapView {

    private String TAG="CustomMapView";
    private ViewParent mViewParent;

    public CustomMapView(Context context) {
        super(context);
    }

    public CustomMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setViewParent(@Nullable final ViewParent viewParent) { //any ViewGroup
        mViewParent = viewParent;
    }

    public CustomMapView(Context context, GoogleMapOptions options) {
        super(context, options);
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                mViewParent.requestDisallowInterceptTouchEvent(true);
                Log.d(TAG, "Inside if of action down ");
            case MotionEvent.ACTION_UP:

                mViewParent.requestDisallowInterceptTouchEvent(false);
                Log.d(TAG, "Inside if of action up");
                break;

            case MotionEvent.ACTION_MOVE:
                mViewParent.requestDisallowInterceptTouchEvent(true);
                return false;
            default:
                break;
        }

        return super.onInterceptTouchEvent(event);
    }
}