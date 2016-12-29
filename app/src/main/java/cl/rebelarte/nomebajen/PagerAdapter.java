package cl.rebelarte.nomebajen;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by rotten on 23-12-15.
 */
public class PagerAdapter extends FragmentPagerAdapter {

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public Fragment getItem(int arg0) {
        switch (arg0) {
            case 0:
                return new Tutorial1();
            case 1:
                return new Tutorial2();

            default:
                return null;
        }
    }

    public int getCount() {
        return 2;
    }

}