package com.urjalusa.presec;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

class ViewPagerAdapter extends PagerAdapter {

    private final Context context;
    private final List<ScreenFragment> fragmentList;

    ViewPagerAdapter(Context context, List<ScreenFragment> fragmentList) {
        this.context = context;
        this.fragmentList = fragmentList;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layoutScreen = inflater.inflate((R.layout.layout_screen_fragment), null);

        ImageView imgOnFragment = layoutScreen.findViewById(R.id.imageViewFragment);
        TextView textOnFragment = layoutScreen.findViewById(R.id.textViewTextFragment);

        textOnFragment.setText(fragmentList.get(position).getTextToDisplay());
        imgOnFragment.setImageResource((fragmentList.get(position).getImageToShow()));

        container.addView(layoutScreen);
        return layoutScreen;
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}