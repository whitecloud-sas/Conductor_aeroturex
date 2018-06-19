package com.whitecloud.hm.whiteclouduser;
/**
 * Created by hm on 2017-03-22.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class AdapterViewPagerProfile extends FragmentPagerAdapter {
    int numberTabs;

    public AdapterViewPagerProfile(FragmentManager fm, int numberTabs) {
        super(fm);
        this.numberTabs=numberTabs;
    }

    @Override
    public Fragment getItem(int position) {
        // recibimos la posición por parámetro y en función de ella devolvemos el Fragment correspondiente a esa sección.
        switch (position) {
            case 0:
                return new ProfileFragment_seccion_info();
            case 1:
                return new ProfileFragment_seccion_settings();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numberTabs;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Información";
            case 1:
                return "Preferencias";
            default:
                return null;
        }
    }
}
