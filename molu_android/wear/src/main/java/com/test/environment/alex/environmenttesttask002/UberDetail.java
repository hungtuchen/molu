package com.test.environment.alex.environmenttesttask002;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;

public class UberDetail extends Activity {

    private GridViewPager mGridViewPager;
    private Fragment[] mFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.round_activity_uber_detail);

        mGridViewPager = (GridViewPager)findViewById(R.id.gridview);


        mFragments = new Fragment[] {
                ActionFragment.newInstance(DataInfo.MoLuFriends.GetMoLuFriends().name, R.drawable.friend1,
                        makeConfirmationActivityIntent(ConfirmationActivity.OPEN_ON_PHONE_ANIMATION,
                                "success animation")),
                ActionFragment.newInstance(DataInfo.MoLuFriends.GetMoLuFriends().name, R.drawable.friend2,
                        makeConfirmationActivityIntent(ConfirmationActivity.OPEN_ON_PHONE_ANIMATION,
                                "failure animation")),
                ActionFragment.newInstance(DataInfo.MoLuFriends.GetMoLuFriends().name, R.drawable.friend3,
                        makeConfirmationActivityIntent(ConfirmationActivity.OPEN_ON_PHONE_ANIMATION,
                                "open on phone animation")),
                ActionFragment.newInstance(DataInfo.MoLuFriends.GetMoLuFriends().name, R.drawable.friend4,
                        makeConfirmationActivityIntent(ConfirmationActivity.OPEN_ON_PHONE_ANIMATION,
                                "open on phone animation")),
                ActionFragment.newInstance("给Uber司机打电话", R.drawable.ic_full_reply,
                        makeConfirmationActivityIntent(ConfirmationActivity.OPEN_ON_PHONE_ANIMATION,
                                "open on phone animation"))
        };

        mGridViewPager.setAdapter(new ConfirmationDemoGridPagerAdapter(this,
                getFragmentManager(), mFragments));

    }

    private Intent makeConfirmationActivityIntent(int animationType, String message) {
        Intent confirmationActivity = new Intent(this, ConfirmationActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION)
                .putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, animationType)
                .putExtra(ConfirmationActivity.EXTRA_MESSAGE, message);
        return confirmationActivity;
    }

    public static class ConfirmationDemoGridPagerAdapter extends FragmentGridPagerAdapter {
        private Drawable mBackground;
        private Fragment[] mDemoFragments;

        public ConfirmationDemoGridPagerAdapter(Context context, FragmentManager fm, Fragment[] fragments) {
            super(fm);
            mBackground = context.getDrawable(R.drawable.definition_bg);
            mDemoFragments = fragments;
        }

        @Override
        public Fragment getFragment(int row, int column) {
            return mDemoFragments[column];
        }

        @Override
        public int getRowCount() {
            return 1;
        }

        @Override
        public int getColumnCount(int row) {
            return mDemoFragments.length;
        }

        @Override
        public Drawable getBackgroundForPage(int row, int column) {
            return mBackground;
        }
    }
}
