package com.whitecloud.hm.whiteclouduser;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private AdapterViewPagerProfile Adaptador_ViewPagerProfile;
    private ViewPager ViewPager;

    private OnFragmentInteractionListener mListener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(view!=null) {
            TabLayout tabLayout = (TabLayout) getView().findViewById(R.id.tabLayout);
            tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
            tabLayout.addTab(tabLayout.newTab());
            tabLayout.addTab(tabLayout.newTab());

            ViewPager = (ViewPager) getView().findViewById(R.id.viewPager);
            Adaptador_ViewPagerProfile = new AdapterViewPagerProfile(getFragmentManager(),tabLayout.getTabCount());
            ViewPager.setAdapter(Adaptador_ViewPagerProfile);
            tabLayout.setupWithViewPager(ViewPager);

            TextView tv_conductor_nombre = (TextView) getView().findViewById(R.id.tvconductor_nombre);
            tv_conductor_nombre.setText(Inicio_sesion.v_nombres);

            // Button
            /*ImageView btnEdit = (ImageView) getView().findViewById(R.id.edit);
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final BottomSheetDialogFragment bsdFragment = BsModalEditProfile.newInstance();
                    bsdFragment.show(getActivity().getSupportFragmentManager(), "BSDialog");
                }
            });

            ImageView btnEditPic = (ImageView) getView().findViewById(R.id.edit_profile_pic);
            btnEditPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //takePicture();
                    try {
                        Class fragmentClass = EditProfilePicFragment.class;
                        FragmentManager fragmentManager = getFragmentManager();
                        Fragment fragment = (Fragment) fragmentClass.newInstance();
                        fragmentManager.beginTransaction().replace(R.id.cameraFrame, fragment).addToBackStack(null).commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });*/

        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}