package org.gasmyr.travelmantics.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import org.gasmyr.travelmantics.DealActivity;
import org.gasmyr.travelmantics.ListActivity;
import org.gasmyr.travelmantics.R;
import org.gasmyr.travelmantics.core.TravelDeal;
import org.gasmyr.travelmantics.util.Constants;
import org.gasmyr.travelmantics.util.FirebaseUtil;
import org.gasmyr.travelmantics.util.Toaster;

import java.util.List;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.ViewHolder> {


    private List<TravelDeal> travelDeals;
    private DatabaseReference databaseReference;
    private ChildEventListener childEventListener;

    public DealAdapter(final ListActivity activity) {
        FirebaseUtil.openReference(Constants.TRAVEL_DEALS_PATH, activity);
        databaseReference = FirebaseUtil.databaseReference;
        travelDeals = FirebaseUtil.travelDeals;
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TravelDeal addedDeal = dataSnapshot.getValue(TravelDeal.class);
                addedDeal.setId(dataSnapshot.getKey());
                travelDeals.add(addedDeal);
                notifyItemInserted(travelDeals.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TravelDeal changedDeal = dataSnapshot.getValue(TravelDeal.class);
                for (TravelDeal deal:travelDeals){
                    if(deal.getId().equalsIgnoreCase(changedDeal.getId())){
                        travelDeals.remove(deal);
                        break;
                    }
                }
                travelDeals.add(changedDeal);
                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        databaseReference.addChildEventListener(childEventListener);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.deal_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TravelDeal deal = travelDeals.get(position);
        holder.priceTv.setText(deal.getPrice()+ "$ ");
        holder.titleTv.setText(deal.getTitle());
        holder.descriptionTv.setText(deal.getDescription());
        showImage(deal.getImageUrl(), holder.imageView);
    }

    @Override
    public int getItemCount() {
        return travelDeals.size();
    }

    private void showImage(String url, ImageView imageView) {
        if (url != null && !url.isEmpty()) {
            Picasso.get().load(url).resize(200, 200).centerCrop().into(imageView);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView titleTv, priceTv, descriptionTv;
        public ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTv = itemView.findViewById(R.id.title);
            priceTv = itemView.findViewById(R.id.price);
            descriptionTv = itemView.findViewById(R.id.description);
            imageView = itemView.findViewById(R.id.imageView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            TravelDeal selectedDeal = travelDeals.get(position);
            Intent intent = new Intent(view.getContext(), DealActivity.class);
            intent.putExtra(Constants.DEAL_EXTRA, selectedDeal);
            view.getContext().startActivity(intent);
        }
    }
}