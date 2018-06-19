package com.conductor.aeroturex;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.conductor.aeroturex.old.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import static com.conductor.aeroturex.MainActivity.hour;
import static com.conductor.aeroturex.MainActivity.jServicio;
import static com.conductor.aeroturex.MainActivity.min;
import static com.conductor.aeroturex.MainActivity.seg;
import static com.conductor.aeroturex.MainActivity.v_latitud;
import static com.conductor.aeroturex.MainActivity.v_longitud;

public class MapaPrincipalFragment extends Fragment implements OnMapReadyCallback{

    On_MapaPrincipal_Listener mCallback;

    // The container Activity must implement this interface so the frag can deliver messages
     interface On_MapaPrincipal_Listener {
        void from_MapaPrincipalFragment(JSONObject position);
    }

    FloatingActionButton fab;
    ViewFlipper viewFlipper;
    Animation slide_in_left, slide_out_right;
    Switch switch_turno, switch_tiempo_espera;
    RelativeLayout rl_pista;
    TextView tv_pista ;
    UiSettings mUiSettings;
    static View rootView;
    Button btn_bmst_cerrar_2,btn_actions,btnCobrar;
    public static TextView tv_costo_aproximado_bsmst, tv_tiempo_transcurrido_bsmst, tv_recorrido_del_viaje_bsmst;
    public static RelativeLayout rl_tarificador = null;
    RatingBar rb_user;
    ConstraintLayout cl_rating;
    Button btn_enviar_calificacion;
    RelativeLayout btnDriver;
    Button btnGoToChat;
    Button btnCancelService;
    Button btnConfirmar;
    Button btnCancel;
    TextView tv_tiempo_espera;
    BottomSheetBehavior bottomSheetBehavior;

    public MapaPrincipalFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *

     * @return A new instance of fragment MapaPrincipalFragment.
     */
    // : Rename and change types and number of parameters
    public static MapaPrincipalFragment newInstance() {
        MapaPrincipalFragment fragment = new MapaPrincipalFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(rootView==null){
            rootView = inflater.inflate(R.layout.fragment_mapa_principal, container, false);
        }
        return rootView;
        //return inflater.inflate(R.layout.fragment_mapa_principal, container, false);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MainActivity.mMap = googleMap;
        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.mapstyle_grayscale);
        MainActivity.mMap.setMapStyle(style);
        //move the camera
        LatLng cali = new LatLng(v_latitud, v_longitud);
        MainActivity.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cali , 16f));
        mUiSettings = googleMap.getUiSettings();
        mUiSettings.setCompassEnabled(false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // FAB
        fab = view.findViewById(R.id.locate);

        MainActivity.mapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map);
        MainActivity.mapFragment.getMapAsync(this);

        // Bottom Sheets
        viewFlipper = view.findViewById(R.id.bsViewSwitch);
        slide_in_left = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), android.R.anim.slide_in_left);
        slide_out_right = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), android.R.anim.slide_out_right);
        viewFlipper.setInAnimation(slide_in_left);
        viewFlipper.setOutAnimation(slide_out_right);
        btn_actions = view.findViewById(R.id.btn_actions);
        btn_bmst_cerrar_2 = view.findViewById(R.id.btn_bmst_cerrar_2);
        rl_tarificador =  view.findViewById(R.id.rl_tarificador);
        btnCobrar =  view.findViewById(R.id.btn_bmst_cobrar);
        rb_user = view.findViewById(R.id.rb_user);
        cl_rating = view.findViewById(R.id.cl_rating_);
        btn_enviar_calificacion = view.findViewById(R.id.btn_enviar_calificacion1);
        switch_turno = view.findViewById(R.id.switch_turno);
        switch_tiempo_espera = view.findViewById(R.id.switch_tiempo_espera);
        btnDriver = view.findViewById(R.id.btn_driver);
        btnGoToChat = view.findViewById(R.id.bs_chat);
        btnCancelService = view.findViewById(R.id.btn_cancelService);
        btnConfirmar = view.findViewById(R.id.btn_confirmar);
        btnCancel = view.findViewById(R.id.btn_cancelar);
        tv_tiempo_espera = view.findViewById(R.id.tv_tiempo_espera);
        tv_pista = view.findViewById(R.id.tv_pista);
        rl_pista = view.findViewById(R.id.rl_pista);
        tv_tiempo_transcurrido_bsmst =  view.findViewById(R.id.tv_tiempo_transcurrido_bsmst);

        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.bottomSheetContainerLayout));
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
        MainActivity.btnPrincipal = view.findViewById(R.id.btn_solicitar);
        MainActivity.btnPrincipal.setText(MainActivity.btnPrincipal_text);
        if (!MainActivity.btnPrincipal.getText().toString().equals(Constants.btnPpal.PONERME_DISPONIBLE)) {
            MainActivity.btnPrincipal.setBackgroundColor(getResources().getColor(R.color.primary_dark));
        }
        MainActivity.btnPrincipal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //fab.animate().scaleX(0).scaleY(0).setDuration(100).start();
                //bottomSheetBehavior.setPeekHeight(120);
                //bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                // Notify the parent activity of selected item

                log("btnPrincipal.getText().toString() " + MainActivity.btnPrincipal.getText().toString());
                if (MainActivity.btnPrincipal.getText().toString().equals(Constants.btnPpal.PONERME_OCUPADO)) {
                    rl_pista.setVisibility(View.GONE);
                    switch_turno.setChecked(false);
                }

                try {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("cmd", "mpf_1");
                    jsonObj.put("detalle", MainActivity.btnPrincipal.getText().toString());
                    mCallback.from_MapaPrincipalFragment(jsonObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

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

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        btnCancelService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewFlipper.setDisplayedChild(0);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

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

        btnDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final BottomSheetDialogFragment bsdFragment = BsModalDriverProfile.newInstance();
                bsdFragment.show(getActivity().getSupportFragmentManager(), "BSDialog");
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (MainActivity.mMap != null) {
                    MainActivity.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(v_latitud, v_longitud), 16f));
                }
            }
        });

        rl_pista.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (switch_turno.isChecked()) {
                    String text = MainActivity.v_pista + "Consultando...";
                    tv_pista.setText(text);

                    try {//consulta enturnamiento
                        JSONObject jObj = new JSONObject();
                        jObj.put("cmd", "34");
                        jObj.put("lat", MainActivity.v_latitud + "");
                        jObj.put("lng", MainActivity.v_longitud + "");
                        jObj.put("date_gps", MainActivity.v_fechaGPS + "");
                        Inicio_sesion.datasource.createRegistro(jObj.toString(), "34", 0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        if (MainActivity.v_pista.equals("")) {
            rl_pista.setVisibility(View.GONE);
        } else {
            tv_pista.setText(MainActivity.v_pista);
            rl_pista.setVisibility(View.VISIBLE);
        }

        switch_tiempo_espera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if (isChecked) {
                    try {//envia inicio de tiempo de espera
                        MainActivity.tiempo_espera = true;
                        JSONObject jObj = new JSONObject();
                        jObj.put("cmd", "C4");
                        jObj.put("lat", MainActivity.v_latitud + "");
                        jObj.put("lng", MainActivity.v_longitud + "");
                        jObj.put("date_gps", MainActivity.v_fechaGPS + "");
                        Inicio_sesion.datasource.createRegistro(jObj.toString(), "C4", 0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    String text = "Tiempo de Espera: " + hour + ":" + min + ":" + seg;
                    tv_tiempo_espera.setText(text);
                    try {//envia terminacion de tiempo de espera
                        MainActivity.tiempo_espera = false;
                        JSONObject jObj = new JSONObject();
                        jObj.put("cmd", "C5");
                        jObj.put("lat", MainActivity.v_latitud + "");
                        jObj.put("lng", MainActivity.v_longitud + "");
                        jObj.put("date_gps", MainActivity.v_fechaGPS + "");
                        Inicio_sesion.datasource.createRegistro(jObj.toString(), "C5", 0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        btn_enviar_calificacion.setOnClickListener(new View.OnClickListener() {
                                                       @Override
                                                       public void onClick(View view) {
                                                           log("btn_enviar_calificacion.setOnClickListener ");
                                                           if(cl_rating!=null)
                                                               cl_rating.setVisibility(View.GONE);

                                                           try {
                                                               JSONObject jObj = new JSONObject();
                                                               jObj.put("cmd", "CU");
                                                               jObj.put("user", (int) rb_user.getRating() + "");
                                                               jObj.put("user_id", jServicio.getString("user_id"));
                                                               mCallback.from_MapaPrincipalFragment(jObj);
                                                           } catch (JSONException e) {
                                                               e.printStackTrace();
                                                           }
                                                           rb_user.setRating(0F);
                                                       }
                                                   }
        );

        btn_bmst_cerrar_2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                log("btn_bmst_cerrar_2");
                rl_tarificador.setVisibility(View.GONE);
                MainActivity.btnPrincipal.setVisibility(View.VISIBLE);
                MainActivity.btnPrincipal.setText(Constants.btnPpal.MOSTRAR_DIRECCION);

                try {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("cmd", "btn_bmst_cerrar_2");
                    mCallback.from_MapaPrincipalFragment(jsonObj);
                }catch (JSONException e){
                    e.printStackTrace();
                }

            }
        });

        btnCobrar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //rl_tarificador.setVisibility(View.GONE);
                try {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("cmd", "btn_bmst_cobrar");
                    mCallback.from_MapaPrincipalFragment(jsonObj);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });

        btn_actions.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                log("btn_actions setOnClickListener jServicio " + jServicio.toString());
                String[] accion1 = new String[]{};
                try {
                    if (jServicio.getString("t_serv").equals("18"))//si es base
                        accion1 = new String[]{/*"Chat con Usuario", */"En sitio", "Ingresar Clave", "Cancelar", "Rechazar"};
                    else
                        accion1 = new String[]{/*"Chat con Usuario", */"En sitio", "Escanear QR", "Cancelar", "Rechazar"};
                }catch (JSONException e){
                    e.printStackTrace();
                }

                final String[] accion = accion1;
                Integer[] image = new Integer[]{R.drawable.chevron_right, R.drawable.chevron_right, R.drawable.chevron_right, R.drawable.chevron_right, R.drawable.chevron_right, R.drawable.chevron_right};

                ListAdapter adapter = new ArrayAdapterWithIcon(getActivity(), accion, image);
                String text3 = "Acciones";
                MainActivity.alertDialogAcciones = new AlertDialog.Builder(getActivity()).setTitle(Html.fromHtml(text3))
                        .setAdapter(adapter, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, final int item) {
                                if (accion[item].equals("Cancelar")) {
                                    final String[] accion = new String[]{"No conoce la Clave", "No Atienden", "No Pidieron", "Se fueron", "Dirección Errada", "Pasajero Ebrio"};
                                    Integer[] image = new Integer[]{R.drawable.chevron_right, R.drawable.chevron_right, R.drawable.chevron_right, R.drawable.chevron_right, R.drawable.chevron_right, R.drawable.chevron_right};
                                    ListAdapter adapter = new ArrayAdapterWithIcon(getActivity(), accion, image);
                                    String text3 = "Cancelación";
                                    MainActivity.alertDialogCancelaciones = new AlertDialog.Builder(getActivity()).setTitle(Html.fromHtml(text3))
                                            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, final int item) {
                                                    try {
                                                        JSONObject jObj = new JSONObject();
                                                        jObj.put("cmd", "cancelacion");
                                                        jObj.put("action", accion[item]);
                                                        jObj.put("tipo_cancela", item + "");
                                                        mCallback.from_MapaPrincipalFragment(jObj);

                                                        rl_tarificador.setVisibility(View.GONE);
                                                        MainActivity.btnPrincipal.setVisibility(View.VISIBLE);

                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }).show();
                                } else {
                                    try {
                                        JSONObject jObj = new JSONObject();
                                        jObj.put("cmd", "btn_actions");
                                        jObj.put("action", accion[item]);
                                        mCallback.from_MapaPrincipalFragment(jObj);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).show();
            }
        });

        switch_turno.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if (isChecked) {
                    try {//envia enturnamiento
                        MainActivity.enturnado = true;
                        JSONObject jObj = new JSONObject();
                        jObj.put("cmd", "34");
                        jObj.put("lat", MainActivity.v_latitud + "");
                        jObj.put("lng", MainActivity.v_longitud + "");
                        jObj.put("date_gps", MainActivity.v_fechaGPS + "");
                        Inicio_sesion.datasource.createRegistro(jObj.toString(), "34", 0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {//envia desenturnamiento
                        MainActivity.enturnado = false;
                        JSONObject jObj = new JSONObject();
                        jObj.put("cmd", "A7");
                        jObj.put("lat", MainActivity.v_latitud + "");
                        jObj.put("lng", MainActivity.v_longitud + "");
                        jObj.put("date_gps", MainActivity.v_fechaGPS + "");
                        Inicio_sesion.datasource.createRegistro(jObj.toString(), "A7", 0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //Mapbox Functions
    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
    }

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
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
        mCallback = null;
    }

    private static void log(String s) {
        Log.d(MapaPrincipalFragment.class.getSimpleName(), "######" + s + "######");
    }
}