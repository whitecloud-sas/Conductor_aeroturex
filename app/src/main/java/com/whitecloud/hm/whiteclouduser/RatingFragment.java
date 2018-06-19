package com.whitecloud.hm.whiteclouduser;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.MapboxAccountManager;;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.Constants;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.directions.v5.DirectionsCriteria;
import com.mapbox.services.directions.v5.MapboxDirections;
import com.mapbox.services.directions.v5.models.DirectionsResponse;
import com.mapbox.services.directions.v5.models.DirectionsRoute;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RatingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RatingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RatingFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private MapView mapView;
    private MapboxMap map;
    private DirectionsRoute currentRoute;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public RatingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RatingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RatingFragment newInstance(String param1, String param2) {
        RatingFragment fragment = new RatingFragment();
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rating, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (view != null) {
            MapboxAccountManager start = MapboxAccountManager.start(getActivity().getApplicationContext(), getActivity().getString(R.string.access_token));
            try {

                final Position origin = Position.fromCoordinates(Double.parseDouble(MainActivity.jServicio.getString("lng")), Double.parseDouble(MainActivity.jServicio.getString("lat")));
                final Position destination = Position.fromCoordinates(Double.parseDouble(MainActivity.jServicio.getString("lng_d")), Double.parseDouble(MainActivity.jServicio.getString("lat_d")));





                // Setup the MapView
                mapView = (MapView) getView().findViewById(R.id.mapview);
                mapView.onCreate(savedInstanceState);
                mapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(MapboxMap mapboxMap) {
                        map = mapboxMap;

                        IconFactory iconFactory = IconFactory.getInstance(getActivity().getApplicationContext());
                        Drawable iconDrawable = ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_marker32);
                        Icon iconOrigen = iconFactory.fromDrawable(iconDrawable);
    //                    iconDrawable.setTint(getResources().getColor(R.color.primary_dark));
                        Icon iconDestino = iconFactory.fromDrawable(iconDrawable);

                        // Add origin and destination to the map
                        mapboxMap.addMarker(new MarkerOptions()
                                .position(new LatLng(origin.getLatitude(), origin.getLongitude()))
                                .title("Origen")
                                .icon(iconOrigen));
                        mapboxMap.addMarker(new MarkerOptions()
                                .position(new LatLng(destination.getLatitude(), destination.getLongitude()))
                                .title("Destino")
                                .icon(iconDestino));


                        final LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                .include(new LatLng(origin.getLatitude(),origin.getLongitude())) // Northeast
                                .include(new LatLng(destination.getLatitude(),destination.getLongitude())) // Southwest
                                .build();

                        map.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 10), 1000);


                        // Get route from API
                        try {
                            getRoute(origin, destination);
                        } catch (ServicesException servicesException) {
                            servicesException.printStackTrace();
                        }
                    }
                });

                mapView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });


                CircleImageView rat_cim_user_pic = (CircleImageView) getView().findViewById(R.id.rat_cim_user_pic);
                TextView rat_tv_user = (TextView) getView().findViewById(R.id.rat_tv_user);

                String imageUrl = getResources().getString(R.string.img_url) + "imgUsuarios/" + MainActivity.jServicio.getString("user_pic");

                Picasso.with(getContext())
                        .load(imageUrl)
                        .resize(96, 96)
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .into(rat_cim_user_pic);
                log("user " + imageUrl);

                rat_tv_user.setText("con " +  MainActivity.jServicio.getString("nombre"));


            }catch (JSONException e){
                e.printStackTrace();
            }

            final RatingBar rb_user = (RatingBar) getView().findViewById(R.id.rb_user);

            Button btn_enviar_calificacion = (Button) getView().findViewById(R.id.btn_enviar_calificacion);
            btn_enviar_calificacion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        try {
                            JSONObject jObj = new JSONObject();
                            jObj.put("cmd", "CU");
                            jObj.put("user",(int) rb_user.getRating()+"");
                            jObj.put("user_id", MainActivity.jServicio.getString("user_id"));
                            mListener.onFragmentInteraction(jObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            );


        }
    }

    private void getRoute(Position origin, Position destination) throws ServicesException {

        MapboxDirections client = new MapboxDirections.Builder()
                .setOrigin(origin)
                .setDestination(destination)
                .setProfile(DirectionsCriteria.PROFILE_CYCLING)
                .setAccessToken(MapboxAccountManager.getInstance().getAccessToken())
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                // You can get the generic HTTP info about the response
                Log.d("Ruta", "Response code: " + response.code());
                if (response.body() == null) {
                    Log.e("Ruta", "No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().getRoutes().size() < 1) {
                    Log.e("Ruta", "No routes found");
                    return;
                }
                currentRoute = response.body().getRoutes().get(0);
                // Draw the route on the map
                drawRoute(currentRoute);
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Log.e("Ruta", "Error: " + throwable.getMessage());
            }
        });
    }

    private void drawRoute(DirectionsRoute route) {
        // Convert LineString coordinates into LatLng[]
        LineString lineString = LineString.fromPolyline(route.getGeometry(), Constants.OSRM_PRECISION_V5);
        List<Position> coordinates = lineString.getCoordinates();
        LatLng[] points = new LatLng[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = new LatLng(
                    coordinates.get(i).getLatitude(),
                    coordinates.get(i).getLongitude());
        }

        // Draw Points on MapView
        map.addPolyline(new PolylineOptions()
                .add(points)
                .color(Color.parseColor("#f56f6c"))
                .width(5));
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
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            //mListener.onFragmentInteraction(uri);
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
        void onFragmentInteraction(JSONObject jObj);
    }

    private static void log(String s) {
        Log.d(RatingFragment.class.getSimpleName(), "######" + s + "######");
    }
}
