package com.conductor.aeroturex;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.conductor.aeroturex.dummy.DummyContentDirecciones.DummyItem;

import java.util.List;
import java.util.Random;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link DireccionesFragment.OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class DireccionesItemRecyclerViewAdapter extends RecyclerView.Adapter<DireccionesItemRecyclerViewAdapter.ViewHolder> {

    private final List<DummyItem> mValues;
    private final DireccionesFragment.OnListFragmentInteractionListener mListener;
    private int[] icons = {R.drawable.ic_home, R.drawable.ic_business, R.drawable.ic_work};

    public DireccionesItemRecyclerViewAdapter(List<DummyItem> items, DireccionesFragment.OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_direcciones, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        //holder.mIdView.setText(mValues.get(position).id);
        Random r = new Random();
        int iconR = r.nextInt(3);
        holder.mIdView.setImageResource(icons[iconR]);
        holder.mContentView.setText(mValues.get(position).content);
        holder.mDetailsView.setText(mValues.get(position).details);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mIdView;
        public final TextView mContentView;
        public final TextView mDetailsView;
        public DummyItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (ImageView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
            mDetailsView = (TextView) view.findViewById(R.id.details);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
