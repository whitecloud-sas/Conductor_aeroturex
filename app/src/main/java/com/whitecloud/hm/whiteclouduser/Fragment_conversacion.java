package com.whitecloud.hm.whiteclouduser;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.whitecloud.hm.whiteclouduser.old.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Fragment_conversacion extends Fragment {

    On_Fragment_mensaje_Listener mCallback;
    RecyclerView recyclerView;
    TextView tv_origen_titulo;
    Fragment_conversacion_RecyclerAdapter adapter;
    List<String> OrigenList = new ArrayList<>();
    List<String> InfoList = new ArrayList<>();
    List<String> IdList = new ArrayList<>();
    List<Integer> MeList = new ArrayList<>();
    JSONObject jBundle;
    RelativeLayout layout_recycler;
    String user_id="";

    // The container Activity must implement this interface so the frag can deliver messages
    public interface On_Fragment_mensaje_Listener {
        /**
         * Called by HeadlinesFragment when a list item is selected
         *
         * @param jObj
         */
        void from_Fragment_mensaje(JSONObject jObj);
    }

    public Fragment_conversacion() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MapaPrincipalFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_conversacion newInstance() {
        Fragment_conversacion fragment = new Fragment_conversacion();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        //acomoda el edittext arriba del teclado
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        ((MainActivity)getActivity()).setFragmentRefreshListener(new MainActivity.FragmentRefreshListener() {
            @Override
            public void onRefresh(JSONObject jObj) {
                try {

                    log("onRefresh " + tv_origen_titulo.getText().toString());
                    log("onRefresh origen " + jObj.getString("origen"));
                    log("onRefresh origen_name " + jObj.getString("origen_name"));
                    if (tv_origen_titulo.getText().toString().equals(jObj.getString("origen_name"))) {
                        MainActivity.in_conversacion = true;
                        log("2");
                        createList(jObj);
                        adapter.notifyDataSetChanged();
                        Inicio_sesion.datasource.update_conversacion_readed(jObj.getString("origen"));
                    }else{
                        MainActivity.in_conversacion = false;
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        String json = bundle.getString("json");
        try {
            jBundle = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.conversacion_activity_recycler, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (view != null) {
            setupList();
            // Fix para el bug del botón FAB. Es un problema reportado que aún no se ha arreglado.
            getView().findViewById(R.id.fab_send).post(new Runnable() {
                @Override
                public void run() {
                    getView().findViewById(R.id.fab_send).requestLayout();
                }
            });

            layout_recycler = (RelativeLayout) getView().findViewById(R.id.layout_recycler);

            try {

                log("jBundle " + jBundle);

                if(!jBundle.getString("user_name").equals("Central") && MainActivity.jServicio!=null) {

                    CircleImageView ci_chat = (CircleImageView) getView().findViewById(R.id.ci_chat);
                    String imageUrl = getResources().getString(R.string.img_url) + "imgUsuarios/" + MainActivity.jServicio.getString("user_pic");

                    Picasso.with(getContext())
                            .load(imageUrl)
                            .resize(32, 32)
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.mipmap.ic_launcher)
                            .into(ci_chat);
                    log("usuario " + imageUrl);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void setupList() {
        recyclerView = (RecyclerView) getView().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tv_origen_titulo = (TextView) getView().findViewById(R.id.tv_origen_titulo);
        log("3");
        createList(jBundle);
        adapter = new Fragment_conversacion_RecyclerAdapter(getContext(), OrigenList, InfoList, IdList, MeList, mCallback);

        //LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        //layoutManager.setStackFromEnd(true);
        //recyclerView.setLayoutManager(layoutManager);

        CircleImageView ci = (CircleImageView) getView().findViewById(R.id.ci_chat);

        if(tv_origen_titulo.getText().toString().equals("Central")){
            ci.setImageResource(R.mipmap.ic_launcher);
        }else{
            //TODO poner las imagenes correspondientes
            ci.setImageResource(R.mipmap.ic_launcher);
        }

        recyclerView.setAdapter(adapter);

        final EditText et_text =  (EditText) getView().findViewById(R.id.et_text);
        final TextView tv_origen_titulo = (TextView) getView().findViewById(R.id.tv_origen_titulo);



        FloatingActionButton myFab = (FloatingActionButton) getView().findViewById(R.id.fab_send);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!et_text.getText().toString().trim().equals("")) {
                    try {
                        JSONObject jObj = new JSONObject();
                        jObj.put("desc", et_text.getText().toString());
                        jObj.put("origen", user_id);
                        jObj.put("origen_name", tv_origen_titulo.getText().toString());
                        jObj.put("id", "0");
                        jObj.put("cmd", "06");
                        jObj.put("creacion", "0");
                        jObj.put("entrega", "0");
                        mCallback.from_Fragment_mensaje(jObj);
                        //{"id":"363","cmd":"06","creacion":"2017-05-10 21:53:25","desc":"dummy msg 2","entrega":"2017-05-10 22:00:22","origen":"central"}
                        long m_id = Inicio_sesion.datasource.create_mensaje(jObj.toString(), 1);

                        jObj = new JSONObject();
                        jObj.put("desc", et_text.getText().toString());
                        jObj.put("cmd", "29");
                        jObj.put("lat", MainActivity.v_latitud + "");
                        jObj.put("lng", MainActivity.v_longitud + "");

                        if(tv_origen_titulo.getText().toString().equals("Central")) {
                            jObj.put("tipo", "1");
                            jObj.put("destino", "0");
                        }else {
                            jObj.put("tipo", "6");
                            jObj.put("destino", MainActivity.jServicio.getString("user_id"));
                        }

                        jObj.put("m_id", m_id + "");

                        Inicio_sesion.datasource.createRegistro(jObj.toString(), Constants.ACTION.TRAMA_X_ENVIAR, 0);

                        et_text.setText("");

                        //actualiza el adapter
                        log("1");
                        createList(jObj);
                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void createList(final JSONObject jObj_t) {

        log("createList jObj_t " + jObj_t.toString());
        String origen;

        //si id = 0, es porque aun no se ha creado conversacion,
        //normalmente es cuando el conductor crea una conversacion con el usuario
        //de lo contrario el chat es con la central

        try {

            if(!jObj_t.has("id")) {
                log("jServicio " + MainActivity.jServicio.toString());
                jObj_t.put("id", MainActivity.jServicio.getString("user_id"));
            }

            JSONArray jArr = Inicio_sesion.datasource.getAll_mensajes(jObj_t);

            if(jObj_t.has("user_id")) {
                user_id = jObj_t.getString("user_id");
            }else if(jObj_t.has("origen_id")) {
                user_id = jObj_t.getString("origen_id");
            } else if(jObj_t.has("destino")) {
                user_id = jObj_t.getString("destino");
            }

            JSONObject jObj;
            OrigenList.clear();
            InfoList.clear();
            IdList.clear();
            MeList.clear();
            for (int i = 0; i < jArr.length(); i++) {
                jObj = jArr.getJSONObject(i);
                log("createList mensaje: " + jObj.toString());
                OrigenList.add(jObj.getString("origen"));
                InfoList.add(jObj.getString("desc"));
                IdList.add(jObj.getString("id"));
                MeList.add(jObj.getInt("me"));
            }

            if(jObj_t.has("origen_")) {
                origen = jObj_t.getString("origen_");
            }else if(jObj_t.has("user_name")){
                origen = jObj_t.getString("user_name");
            }else if(jObj_t.has("origen_name")){
                origen = jObj_t.getString("origen_name");
            }else{
                origen = MainActivity.jServicio.getString("nombre");
            }

            log("origen " + origen);


            tv_origen_titulo.setText(origen);

            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            //muestra los ultimos registros
            layoutManager.setStackFromEnd(true);
            recyclerView.setLayoutManager(layoutManager);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    // Check screen orientation or screen rotate event here
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

       LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        //muestra los ultimos registros
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter.notifyItemRangeChanged(0, adapter.getItemCount());

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        MainActivity.in_conversacion = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (On_Fragment_mensaje_Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    private static void log(String s) {
        Log.d(Fragment_conversacion.class.getSimpleName(), "######" + s + "######");
    }
}