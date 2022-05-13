package com.urjalusa.presec;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
    private final OnItemClickListener userClickListener;

    public interface OnItemClickListener {
        void OnItemClick(int position);
    }

    private final GestureDetector userGestureDetector;

    RecyclerItemClickListener(Context context, OnItemClickListener listener) {
        userClickListener = listener;
        userGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView view, @NonNull MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && userClickListener != null && userGestureDetector.onTouchEvent(e)) {
            userClickListener.OnItemClick(view.getChildAdapterPosition(childView));
        }
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView view, @NonNull MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }
}