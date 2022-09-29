package com.senya.eatappserver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.senya.eatappserver.R;
import com.senya.eatappserver.callback.IRecyclerClickListener;
import com.senya.eatappserver.common.Common;
import com.senya.eatappserver.model.BestDealsModel;
import com.senya.eatappserver.model.EventBus.CategoryClick;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyBestDealsAdapter extends RecyclerView.Adapter<MyBestDealsAdapter.MyViewHolder> {

    Context context;
    List<BestDealsModel> bestDealsModelList;

    public MyBestDealsAdapter(Context context, List<BestDealsModel> bestDealsModelList) {
        this.context = context;
        this.bestDealsModelList = bestDealsModelList;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public List<BestDealsModel> getBestDealsModelList() {
        return bestDealsModelList;
    }

    public void setBestDealsModelList(List<BestDealsModel> bestDealsModelList) {
        this.bestDealsModelList = bestDealsModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_category_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Glide.with(context).load(bestDealsModelList.get(position).getImage())
                .into(holder.category_image);
        holder.category_name.setText(new StringBuilder(bestDealsModelList.get(position).getName()));

        //Event
        holder.setListener((view, pos) -> {
            });

    }

    @Override
    public int getItemCount() {
        return bestDealsModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Unbinder unbinder;
        @BindView(R.id.img_category)
        ImageView category_image;
        @BindView(R.id.txt_category)
        TextView category_name;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClickListener(v,getAdapterPosition());
        }
    }
}
