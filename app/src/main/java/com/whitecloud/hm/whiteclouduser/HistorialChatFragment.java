package com.whitecloud.hm.whiteclouduser;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.whitecloud.hm.whiteclouduser.dummy.DummyContentHistorialChat;
import com.whitecloud.hm.whiteclouduser.dummy.DummyContentHistorialChat.DummyItem;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class HistorialChatFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HistorialChatFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static HistorialChatFragment newInstance(int columnCount) {
        HistorialChatFragment fragment = new HistorialChatFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View viewContainer = inflater.inflate(R.layout.fragment_historial_chat_list, container, false);
        View view = viewContainer.findViewById(R.id.list);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new HistorialChatRecyclerViewAdapter(DummyContentHistorialChat.ITEMS, mListener));

            RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
            recyclerView.addItemDecoration(itemDecoration);
        }
        return viewContainer;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(view!=null) {
            RecyclerView historialChatView = (RecyclerView) getView().findViewById(R.id.list);
            historialChatView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = null;
                    Class fragmentClass = null;

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
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(DummyItem item);
    }
}
