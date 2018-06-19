package com.whitecloud.hm.whiteclouduser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.MyBearingTracking;
import com.mapbox.mapboxsdk.constants.MyLocationTracking;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.geocoder.ui.GeocoderAutoCompleteView;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.geocoding.v5.models.CarmenFeature;
import com.whitecloud.hm.whiteclouduser.old.Constants;
import com.whitecloud.hm.whiteclouduser.old.callAlertBox;

import org.json.JSONException;
import org.json.JSONObject;


import static com.whitecloud.hm.whiteclouduser.MainActivity.*;
/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapaPrincipalFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapaPrincipalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapaPrincipalFragment extends Fragment {

    On_MapaPrincipal_Listener mCallback;

    // The container Activity must implement this interface so the frag can deliver messages
    public interface On_MapaPrincipal_Listener {
        /** Called by HeadlinesFragment when a list item is selected
         * @param position*/
        void from_MapaPrincipalFragment(JSONObject position);
    }

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private MapView mapView;

    private FloatingActionButton fab;
    //private ViewSwitcher viewSwitcher;
    private ViewFlipper viewFlipper;
    private ProgressBar bar;
    Animation slide_in_left, slide_out_right;

    private OnFragmentInteractionListener mListener;
    private LocationServices locationServices;

    public MapaPrincipalFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapaPrincipalFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapaPrincipalFragment newInstance(String param1, String param2) {
        MapaPrincipalFragment fragment = new MapaPrincipalFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //mParam1 = getArguments().getString(ARG_PARAM1);
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mapa_principal, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(view!=null) {
            // FAB
            fab = (FloatingActionButton) getView().findViewById(R.id.locate);
            locationServices = LocationServices.getLocationServices(MainActivity.mContext);
            // Mapbox
            MapboxAccountManager start = MapboxAccountManager.start(getActivity().getApplicationContext(), getActivity().getString(R.string.access_token));
            // Create a mapView
            mapView = (MapView) getView().findViewById(R.id.mapview);
            mapView.onCreate(savedInstanceState);
            // Add a MapboxMap
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {
                    map = mapboxMap;
                    // Create an Custom Icon object for the marker to use
                    /*IconFactory iconFactory = IconFactory.getInstance(getActivity().getApplicationContext());
                    Drawable iconDrawable = ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_marker32);
                    Icon icon = iconFactory.fromDrawable(iconDrawable);
                    // Customize map
                    markerViewOptions = new MarkerViewOptions()
                            .position(new LatLng(3.430226, -76.540501))
                            .icon(icon)
                            //.title(getString(R.string.hint_marker))
                    ;
                    map.addMarker(markerViewOptions);*/

                    Location lastLocation = locationServices.getLastLocation();
                    if (lastLocation != null) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 16));
                        MainActivity.v_latitud = lastLocation.getLatitude();
                        MainActivity.v_longitud = lastLocation.getLongitude();
                    }

                    map.getTrackingSettings().setMyLocationTrackingMode(MyLocationTracking.TRACKING_FOLLOW);
                    //map.getTrackingSettings().setMyBearingTrackingMode(MyBearingTracking.COMPASS);

                    map.setMyLocationEnabled(true);

                }
            });

            // Set up autocomplete widget
            /*GeocoderAutoCompleteView autocomplete = (GeocoderAutoCompleteView) getView().findViewById(R.id.query);
            autocomplete.setAccessToken(MapboxAccountManager.getInstance().getAccessToken());
            autocomplete.setType(GeocodingCriteria.TYPE_POI);
            autocomplete.setOnFeatureListener(new GeocoderAutoCompleteView.OnFeatureListener() {
                @Override
                public void OnFeatureClick(CarmenFeature feature) {
                    Position position = feature.asPosition();
                    //updateMap(position.getLatitude(), position.getLongitude());
                }
            });*/
            /*EditText edit_text = (EditText) getView().findViewById(R.id.txtAddress);
            edit_text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    Fragment fragment = null;
                    Class fragmentClass = AutocompleteAddressFragment.class;
                    FragmentManager fragmentManager = getFragmentManager();

                    if(hasFocus){
                        try {
                            fragment = (Fragment) fragmentClass.newInstance();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        fragmentManager.beginTransaction().replace(R.id.addres_container, fragment).addToBackStack(null).commit();
                    }
                }
            });*/

            // Bottom Sheets
            viewFlipper = (ViewFlipper) getView().findViewById(R.id.bsViewSwitch);
            slide_in_left = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), android.R.anim.slide_in_left);
            slide_out_right = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), android.R.anim.slide_out_right);
            viewFlipper.setInAnimation(slide_in_left);
            viewFlipper.setOutAnimation(slide_out_right);

            final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(getView().findViewById(R.id.bottomSheetContainerLayout));
            // Capturing the callbacks for bottom sheet
            bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    switch (newState) {
                        case BottomSheetBehavior.STATE_COLLAPSED:
                            log("Bottom Sheet Behaviour STATE_COLLAPSED");
                            fab.animate().scaleX(1).scaleY(1).setDuration(300).start();
                            break;
                        case BottomSheetBehavior.STATE_DRAGGING:
                            log("Bottom Sheet Behaviour STATE_DRAGGING");
                            fab.animate().scaleX(0).scaleY(0).setDuration(100).start();
                            break;
                        case BottomSheetBehavior.STATE_EXPANDED:
                            log("Bottom Sheet Behaviour STATE_EXPANDED");
                            fab.animate().scaleX(0).scaleY(0).setDuration(100).start();
                            break;
                        case BottomSheetBehavior.STATE_HIDDEN:
                            log("Bottom Sheet Behaviour STATE_HIDDEN");
                            fab.animate().scaleX(1).scaleY(1).setDuration(300).start();
                            break;
                        case BottomSheetBehavior.STATE_SETTLING:
                            log("Bottom Sheet Behaviour STATE_SETTLING");
                            break;
                    }
                }
                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    log("BottomSheetCallback slideOffset: " + slideOffset);
                }
            });

            // Buttons
            btnPrincipal = (Button) getView().findViewById(R.id.btn_solicitar);
            btnPrincipal.setText(btnPrincipal_text);
            if(!btnPrincipal_text.equals(Constants.btnPpal.PONERME_DISPONIBLE)){
                btnPrincipal.setBackgroundColor(getResources().getColor(R.color.primary_dark));
            }
            btnPrincipal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //fab.animate().scaleX(0).scaleY(0).setDuration(100).start();
                    //bottomSheetBehavior.setPeekHeight(120);
                    //bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                    // Notify the parent activity of selected item

                    try {
                        JSONObject jsonObj = new JSONObject();
                        jsonObj.put("cmd", "mpf_1");
                        jsonObj.put("detalle", btnPrincipal.getText().toString());
                        mCallback.from_MapaPrincipalFragment(jsonObj);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                }
            });
            Button btnConfirmar = (Button) getView().findViewById(R.id.btn_confirmar);
            btnConfirmar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //viewSwitcher.showNext();
                    viewFlipper.showNext();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            viewFlipper.showNext();
                        }
                    }, 3000);
                }
            });
            Button btnCancel = (Button) getView().findViewById(R.id.btn_cancelar);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            });
            Button btnCancelService = (Button) getView().findViewById(R.id.btn_cancelService);
            btnCancelService.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewFlipper.setDisplayedChild(0);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            });
            Button btnGoToChat = (Button) getView().findViewById(R.id.bs_chat);
            btnGoToChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = null;
                    Class fragmentClass;

                    fragmentClass = ChatFragment.class;
                    try {
                        fragment = (Fragment) fragmentClass.newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
                }
            });
            RelativeLayout btnDriver = (RelativeLayout) getView().findViewById(R.id.btn_driver);
            btnDriver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final BottomSheetDialogFragment bsdFragment = BsModalDriverProfile.newInstance();
                    bsdFragment.show(getActivity().getSupportFragmentManager(), "BSDialog");
                }
            });
            fab.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    if (map != null) {

                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(v_latitud, v_longitud))
                               // .bearing(location.getBearing())
                                .build();

                        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 3000, null);
                    }
                }
            });
        }
    }

    //Mapbox Functions
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
    /*@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }*/

    // Check screen orientation or screen rotate event here
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                log("Cancelled from fragment");
            } else {
                log("Scanned from fragment: " + result.getContents());
            }
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    private void updateMap(double latitude, double longitude) {
        // Build marker
        IconFactory iconFactory = IconFactory.getInstance(getActivity().getApplicationContext());
        Drawable iconDrawable = ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_marker32);
        //iconDrawable.setTint(getResources().getColor(R.color.primary_dark));
        Icon icon = iconFactory.fromDrawable(iconDrawable);

        map.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .icon(icon)
                .title("Geocoder result"));

        // Animate camera to geocoder result location
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))
                .zoom(15)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 5000, null);
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
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (On_MapaPrincipal_Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()  + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mCallback = null;
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

    private static void log(String s) {
        Log.d(MapaPrincipalFragment.class.getSimpleName(), "######" + s + "######");
    }
}