package com.senya.androideatitv2client.ui.view_orders;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.senya.androideatitv2client.Adapter.MyOrdersAdapter;
import com.senya.androideatitv2client.Callback.ILoadOrderCallbackListener;
import com.senya.androideatitv2client.Common.Common;
import com.senya.androideatitv2client.Common.MySwipeHelper;
import com.senya.androideatitv2client.Database.CartDataSource;
import com.senya.androideatitv2client.Database.CartDatabase;
import com.senya.androideatitv2client.Database.CartItem;
import com.senya.androideatitv2client.Database.LocalCartDataSource;
import com.senya.androideatitv2client.EventBus.CounterCartEvent;
import com.senya.androideatitv2client.EventBus.MenuItemBack;
import com.senya.androideatitv2client.Model.OrderModel;
import com.senya.androideatitv2client.Model.ShippingOrderModel;
import com.senya.androideatitv2client.R;
import com.senya.androideatitv2client.TrackingOrderActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ViewOrdersFragment extends Fragment implements ILoadOrderCallbackListener {

    CartDataSource cartDataSource;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @BindView(R.id.recycler_orders)
    RecyclerView recycler_orders;

    AlertDialog dialog;

    private Unbinder unbinder;

    private ViewOrdersViewModel viewOrdersViewModel;

    private ILoadOrderCallbackListener listener;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewOrdersViewModel =
                new ViewModelProvider(this).get(ViewOrdersViewModel.class);
        View root = inflater.inflate(R.layout.fragment_view_order,container,false);
        unbinder = ButterKnife.bind(this,root);

        initViews(root);
        loadOrdersFromFirebase();

        viewOrdersViewModel.getMutableLiveDataOrderList().observe(getViewLifecycleOwner(),orderList -> {
            MyOrdersAdapter adapter = new MyOrdersAdapter(getContext(),orderList);
            recycler_orders.setAdapter(adapter);
        });

        return root;
    }

    private void loadOrdersFromFirebase() {
        List<OrderModel> orderModelList = new ArrayList<>();
        FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_REF)
                .child(Common.currentRestaurant.getUid())
                .child(Common.ORDER_REF)
                .orderByChild("userId")
                .equalTo(Common.currentUser.getUid())
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot orderSnapShot:snapshot.getChildren())
                        {
                            OrderModel orderModel = orderSnapShot.getValue(OrderModel.class);
                            orderModel.setOrderNumber(orderSnapShot.getKey());
                            orderModelList.add(orderModel);
                        }
                        listener.onLoadOrderSuccess(orderModelList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onLoadOrderFailed(error.getMessage());
                    }
                });
    }

    private void initViews(View root) {
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        listener = this;

        dialog = new AlertDialog.Builder(getContext()).setCancelable(false).create();
        dialog.create();

        recycler_orders.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_orders.setLayoutManager(layoutManager);
        recycler_orders.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(), recycler_orders, 250) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Отменить заказ", 30, 0, Color.parseColor("#FF3C30"),
                        pos -> {
                            OrderModel orderModel = ((MyOrdersAdapter)recycler_orders.getAdapter()).getItemAtPosition(pos);
                            if(orderModel.getOrderStatus() == 0)
                            {
                                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                                builder.setTitle("Отмена заказа")
                                        .setMessage("Вы действительно хотите отменить заказ?")
                                        .setNegativeButton("НЕТ", (dialogInterface, i) -> dialogInterface.dismiss())
                                        .setPositiveButton("ДА", (dialogInterface, i) -> {

                                            Map<String,Object> update_data = new HashMap<>();
                                            update_data.put("orderStatus", -1);
                                            FirebaseDatabase.getInstance()
                                                    .getReference(Common.RESTAURANT_REF)
                                                    .child(Common.currentRestaurant.getUid())
                                                    .child(Common.ORDER_REF)
                                                    .child(orderModel.getOrderNumber())
                                                    .updateChildren(update_data)
                                                    .addOnFailureListener(e -> Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                                                    .addOnSuccessListener(unused -> {
                                                        orderModel.setOrderStatus(-1);
                                                        ((MyOrdersAdapter)recycler_orders.getAdapter()).setItemAtPosition(pos,orderModel);
                                                        recycler_orders.getAdapter().notifyItemChanged(pos);
                                                        Toast.makeText(getContext(), "Заказ отменен", Toast.LENGTH_SHORT).show();
                                                    });
                                        });
                                androidx.appcompat.app.AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                            else
                            {
                                Toast.makeText(getContext(), new StringBuilder("Статус заказа изменен на ")
                                        .append(Common.convertStatusToText(orderModel.getOrderStatus()))
                                        .append(", так что отмена невозможна"), Toast.LENGTH_SHORT).show();
                            }
                        }));

                buf.add(new MyButton(getContext(), "Отслеживание", 30, 0, Color.parseColor("#001970"),
                        pos -> {
                            OrderModel orderModel = ((MyOrdersAdapter)recycler_orders.getAdapter()).getItemAtPosition(pos);

                            FirebaseDatabase.getInstance()
                                    .getReference(Common.RESTAURANT_REF)
                                    .child(Common.currentRestaurant.getUid())
                                    .child(Common.SHIPPING_ORDER_REF)
                                    .child(orderModel.getOrderNumber())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists())
                                            {
                                                Common.currentShippingOrder = snapshot.getValue(ShippingOrderModel.class);
                                                Common.currentShippingOrder.setKey(snapshot.getKey());
                                                if(Common.currentShippingOrder.getCurrentLat() != -1 &&
                                                Common.currentShippingOrder.getCurrentLng() != -1)
                                                {

                                                    startActivity(new Intent(getContext(), TrackingOrderActivity.class));

                                                }
                                                else
                                                {
                                                    Toast.makeText(getContext(), "Курьер еще не начал доставку", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            else 
                                            {
                                                Toast.makeText(getContext(), "Передаем курьеру", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(getContext(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }));

                buf.add(new MyButton(getContext(), "Повторить заказ", 30, 0, Color.parseColor("#5d4037"),
                        pos -> {

                            OrderModel orderModel = ((MyOrdersAdapter)recycler_orders.getAdapter()).getItemAtPosition(pos);

                            dialog.show();
                            cartDataSource.cleanCart(Common.currentUser.getUid(),Common.currentRestaurant.getUid())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(Integer integer) {

                                            CartItem[] cartItems = orderModel
                                                    .getCartItemList().toArray(new CartItem[orderModel.getCartItemList().size()]);

                                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItems)
                                            .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(() -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(getContext(), "Добавлено в корзину", Toast.LENGTH_SHORT).show();
                                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));

                                                    },throwable -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    })
                                            );
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            dialog.dismiss();
                                            Toast.makeText(getContext(), "[Error]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }));
            }
        };

    }

    @Override
    public void onLoadOrderSuccess(List<OrderModel> orderModelList) {
        dialog.dismiss();
        viewOrdersViewModel.setMutableLiveDataOrderList(orderModelList);
    }

    @Override
    public void onLoadOrderFailed(String message) {
        dialog.dismiss();
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}