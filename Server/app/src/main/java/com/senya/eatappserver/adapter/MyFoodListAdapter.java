package com.senya.eatappserver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.senya.eatappserver.R;
import com.senya.eatappserver.callback.IRecyclerClickListener;
import com.senya.eatappserver.common.Common;
import com.senya.eatappserver.model.BestDealsModel;
import com.senya.eatappserver.model.FoodModel;
import com.senya.eatappserver.model.MostPopularModel;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MyFoodListAdapter extends RecyclerView.Adapter<MyFoodListAdapter.MyViewHolder> {

    private Context context;
    private List<FoodModel> foodModelList;

    private ExpandableLayout lastExpandable;

    public MyFoodListAdapter(Context context, List<FoodModel> foodModelList) {
        this.context = context;
        this.foodModelList = foodModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_food_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(foodModelList.get(position).getImage()).into(holder.img_food_image);
        holder.txt_food_price.setText(new StringBuilder("$")
                .append(foodModelList.get(position).getPrice()));
        holder.txt_food_name.setText(new StringBuilder("")
                .append(foodModelList.get(position).getName()));

        //Event
        holder.setListener((view, pos) -> {
            Common.selectedFood = foodModelList.get(pos);
            Common.selectedFood.setKey(String.valueOf(pos));

            if(lastExpandable != null && lastExpandable.isExpanded())lastExpandable.collapse();

            if(!holder.expandable_layout.isExpanded())
            {
                holder.expandable_layout.setSelected(true);
                holder.expandable_layout.expand();
            }
            else
            {
                holder.expandable_layout.collapse();
                holder.expandable_layout.setSelected(false);
            }
            lastExpandable = holder.expandable_layout;
        });

        holder.btn_best_deal.setOnClickListener(v -> {
            makeFoodToBestDealOfRestaurant(foodModelList.get(position));
        });

        holder.btn_most_popular.setOnClickListener(v -> {
            makeFoodToPopularOfRestaurant(foodModelList.get(position));
        });
    }

    private void makeFoodToPopularOfRestaurant(FoodModel foodModel) {

        MostPopularModel mostPopularModel = new MostPopularModel();
        mostPopularModel.setName(foodModel.getName());
        mostPopularModel.setMenu_id(Common.categorySelected.getMenu_id());
        mostPopularModel.setFood_id(foodModel.getId());
        mostPopularModel.setImage(foodModel.getImage());

        FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(Common.MOST_POPULAR)
                .child(new StringBuilder(mostPopularModel.getMenu_id()).append("_").append(mostPopularModel.getFood_id()).toString())
                .setValue(mostPopularModel)
                .addOnFailureListener(e -> {
                    Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnSuccessListener(unused -> {
            Toast.makeText(context, "Most Popular added", Toast.LENGTH_SHORT).show();
        });

    }

    private void makeFoodToBestDealOfRestaurant(FoodModel foodModel) {

        BestDealsModel bestDealsModel = new BestDealsModel();
        bestDealsModel.setName(foodModel.getName());
        bestDealsModel.setMenu_id(Common.categorySelected.getMenu_id());
        bestDealsModel.setFood_id(foodModel.getId());
        bestDealsModel.setImage(foodModel.getImage());

        FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(Common.BEST_DEALS)
                .child(new StringBuilder(bestDealsModel.getMenu_id()).append("_").append(bestDealsModel.getFood_id()).toString())
                .setValue(bestDealsModel)
                .addOnFailureListener(e -> {
                    Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnSuccessListener(unused -> {
                    Toast.makeText(context, "Best Deal added", Toast.LENGTH_SHORT).show();
                });

    }


    @Override
    public int getItemCount() {
        return foodModelList.size();
    }

    public FoodModel getItemAtPosition(int pos)
    {
        return foodModelList.get(pos);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Unbinder unbinder;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.txt_food_price)
        TextView txt_food_price;
        @BindView(R.id.img_food_image)
        ImageView img_food_image;
        @BindView(R.id.expandable_layout)
        ExpandableLayout expandable_layout;
        @BindView(R.id.btn_most_popular)
        Button btn_most_popular;
        @BindView(R.id.btn_best_deal)
        Button btn_best_deal;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClickListener(v, getAdapterPosition());
        }
    }
}