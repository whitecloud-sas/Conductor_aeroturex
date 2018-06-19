package com.conductor.aeroturex;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Fragment_conversaciones extends Fragment {

    On_Fragment_mensajes_Listener mCallback;
    RecyclerView recyclerView;
    Fragment_conversaciones_RecyclerAdapter adapter;
    List<String> OrigenList = new ArrayList<>();
    List<String> InfoList = new ArrayList<>();
    List<String> IdList = new ArrayList<>();
    List<String> BadgeList = new ArrayList<>();
    List<String> OrigenList_id = new ArrayList<>();

    // The container Activity must implement this interface so the frag can deliver messages
    public interface On_Fragment_mensajes_Listener {
        /**
         * Called by HeadlinesFragment when a list item is selected
         *
         * @param jObj
         */
        void from_Fragment_mensajes(JSONObject jObj);
    }

    public Fragment_conversaciones() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MapaPrincipalFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_conversaciones newInstance() {
        Fragment_conversaciones fragment = new Fragment_conversaciones();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        ((MainActivity)getActivity()).setFragmentRefreshListener_chats(new MainActivity.FragmentRefreshListener_chats() {
            @Override
            public void onRefresh(JSONObject jObj) {
                createList();
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.conversaciones_activity_recycler, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (view != null) {
            setupList();
        }
    }

    private void setupList() {
        recyclerView = (RecyclerView) getView().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        createList();
        adapter = new Fragment_conversaciones_RecyclerAdapter(getContext(), OrigenList, InfoList, IdList, BadgeList, mCallback, OrigenList_id);
        recyclerView.setAdapter(adapter);

    }

    private void createList() {

        log("createList");
        try {
            JSONArray jArr = Inicio_sesion.datasource.getAll_mensajes_grouped();
            //{"id":"286","cmd":"06","desc":"dummy msg","creacion":"2017-05-09 10:43:58","entrega":"2017-05-09 10:43:58","origen":"CENTRAL"}
            OrigenList.clear();
            InfoList.clear();
            IdList.clear();
            BadgeList.clear();
            OrigenList_id.clear();
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jObj = jArr.getJSONObject(i);
                log("conversaciones : " + jObj.toString());

                if(jObj.getString("origen").equals("0")){
                    OrigenList_id.add("0");
                    OrigenList.add("Central");
                }else{
                    OrigenList_id.add(jObj.getString("origen"));
                    OrigenList.add(jObj.getString("origen_name"));
                }

                InfoList.add(jObj.getString("desc"));
                IdList.add(jObj.getString("id"));
                //TODO cantidad de mensajes sin leer del origen
                int cant = Inicio_sesion.datasource.cant_mensajes_sin_leer_origen(jObj.getString("origen"));
                BadgeList.add(String.valueOf(cant));
            }
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
            mCallback = (On_Fragment_mensajes_Listener) context;
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
        Log.d(Fragment_conversaciones.class.getSimpleName(), "######" + s + "######");
    }
}