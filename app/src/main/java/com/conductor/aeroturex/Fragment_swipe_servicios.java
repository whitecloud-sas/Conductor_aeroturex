package com.conductor.aeroturex;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.conductor.aeroturex.old.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Fragment_swipe_servicios extends Fragment {

    On_Fragment_swipe_servicios_Listener mCallback;
    RecyclerView recyclerView;
    Fragment_swipe_servicios_RecyclerAdapter adapter;
    List<String> DireccionList  = new ArrayList<>();
    List<String> InfoList = new ArrayList<>();
    List<String> IdList = new ArrayList<>();

    // The container Activity must implement this interface so the frag can deliver messages
    public interface On_Fragment_swipe_servicios_Listener {
        /**
         * Called by HeadlinesFragment when a list item is selected
         *
         * @param position
         */
        void from_Fragment_swipe_servicios(JSONObject position);
    }

    public Fragment_swipe_servicios() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MapaPrincipalFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_swipe_servicios newInstance() {
        Fragment_swipe_servicios fragment = new Fragment_swipe_servicios();
        Bundle args = new Bundle();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.servicios_activity_recycler, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (view != null) {
            setupList(view);
        }
    }

    private void setupList(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);

        createList();
        adapter = new Fragment_swipe_servicios_RecyclerAdapter(getContext(), DireccionList, InfoList, IdList);
        recyclerView.setAdapter(adapter);
    }

    private void createList() {

        log("createList");
        try {
            JSONArray jArr = Inicio_sesion.datasource.getAllRegs(Constants.DB.SERVICIO);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jObj = jArr.getJSONObject(i);
                log("servicios createList: " + jObj.toString());
                DireccionList.add(jObj.getString("dir"));

                String infolist = "Usuario: " + jObj.getString("nombre");
                if (!jObj.getString("empresa").equals("")) {
                    infolist+="\nEmpresa:" + jObj.getString("empresa");
                }
                if(jObj.has("estado")) {
                    if (jObj.getString("estado").equals("Cumplido")) {
                        infolist += "\nValor: $ " + jObj.getString("valor")
                                + "\nRecorrido: " + jObj.getString("recorrido") + " mts";
                    }
                    infolist += "\nEstado: " + jObj.getString("estado");
                }
                InfoList.add(infolist);
                IdList.add(jObj.getString("servicio"));
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
            mCallback = (On_Fragment_swipe_servicios_Listener) context;
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
        Log.d(Fragment_swipe_servicios.class.getSimpleName(), "######" + s + "######");
    }
}