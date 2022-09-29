package com.senya.androideatitv2client.Adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.asksira.loopingviewpager.LoopingPagerAdapter;
import com.asksira.loopingviewpager.LoopingViewPager;
import com.bumptech.glide.Glide;
import com.senya.androideatitv2client.EventBus.BestDealItemClick;
import com.senya.androideatitv2client.Model.BestDealModel;
import com.senya.androideatitv2client.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyBestDealsAdapter extends LoopingPagerAdapter<BestDealModel> {

    @BindView(R.id.img_best_deal)
    ImageView img_best_deal;
    @BindView(R.id.txt_best_deal)
    TextView txt_best_deal;

    Unbinder unbinder;

    public MyBestDealsAdapter(Context context, @NonNull List<BestDealModel> itemList, boolean isInfinite) {
        super(itemList, isInfinite);
    }

    @NonNull
    @Override
    protected View inflateView(int i, @NonNull ViewGroup viewGroup, int i1) {
        return LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_best_deal_item, viewGroup, false);
    }

    @Override
    protected void bindView(@NonNull View view, int listPosition, int viewType) {
        ImageView imageView = (ImageView)view.findViewById(R.id.img_best_deal);
        unbinder = ButterKnife.bind(this,view);
        //Get data
        Glide.with(view).load(getItem(listPosition).getImage()).into(img_best_deal);
        txt_best_deal.setText(getItem(listPosition).getName());

        view.setOnClickListener(v -> {
            EventBus.getDefault().postSticky(new BestDealItemClick(getItem(listPosition)));
        });
    }


}
