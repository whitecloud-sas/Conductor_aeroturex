package com.conductor.aeroturex;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AutocompleteAddressFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AutocompleteAddressFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AutocompleteAddressFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ViewSwitcher viewSwitcher;
    Animation slide_in_left, slide_out_right;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public AutocompleteAddressFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AutocompleteAddressFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AutocompleteAddressFragment newInstance(String param1, String param2) {
        AutocompleteAddressFragment fragment = new AutocompleteAddressFragment();
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
        return inflater.inflate(R.layout.fragment_autocomplete_address, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (view != null) {
            EditText addressDestino = (EditText) getView().findViewById(R.id.edit_query);
            addressDestino.requestFocus();

            LinearLayout back_address = (LinearLayout) getView().findViewById(R.id.set_pin_location);
            back_address.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    getFragmentManager().popBackStack();
                }
            });

            viewSwitcher = (ViewSwitcher) getView().findViewById(R.id.viewSwitcher);
            slide_in_left = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), android.R.anim.slide_in_left);
            slide_out_right = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), android.R.anim.slide_out_right);
            viewSwitcher.setInAnimation(slide_in_left);
            viewSwitcher.setOutAnimation(slide_out_right);

            EditText edit_text = (EditText) getView().findViewById(R.id.edit_query);
            edit_text.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 0) {
                        if (viewSwitcher.getDisplayedChild() != 0) { viewSwitcher.showPrevious(); }
                    } else {
                        if (viewSwitcher.getDisplayedChild() != 1) { viewSwitcher.showNext(); }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
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
