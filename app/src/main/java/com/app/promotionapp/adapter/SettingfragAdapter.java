package com.app.promotionapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.promotionapp.R;
import com.app.promotionapp.model.Store;
import com.app.promotionapp.model.SubscribedStore;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class SettingfragAdapter extends RecyclerView.Adapter<SettingfragAdapter.UserSubscribedStoresViewholder> {
    List<SubscribedStore> storeList;

    public SettingfragAdapter(List<SubscribedStore> storeList) {
        this.storeList = storeList;
    }

    @NonNull
    @Override
    public UserSubscribedStoresViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_subscribed_layout, null, false);
        return new UserSubscribedStoresViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserSubscribedStoresViewholder holder, int position) {
        SubscribedStore store = storeList.get(position);
        holder.shopTitle.setText(store.getStore_title());
        FirebaseFirestore.getInstance().collection("Stores")
                .whereEqualTo("store_id",store.getStore_id())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                for(DocumentSnapshot documentSnapshot: documentSnapshots){
                    holder.shopLocation.setText(documentSnapshot.toObject(Store.class).getLocation_subtitle());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }


    public class UserSubscribedStoresViewholder extends RecyclerView.ViewHolder{
        TextView shopTitle;
        TextView shopLocation;

        public UserSubscribedStoresViewholder(@NonNull View itemView) {
            super(itemView);

            shopTitle = itemView.findViewById(R.id.userSubscribed_shopTitle);
            shopLocation = itemView.findViewById(R.id.userSubscribed_location);
        }
    }
}
