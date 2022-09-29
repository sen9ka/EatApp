package com.senya.eatappserver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.metrics.Event;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseField;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.senya.eatappserver.adapter.PdfDocumentAdapter;
import com.senya.eatappserver.common.Common;
import com.senya.eatappserver.common.PDFUtils;
import com.senya.eatappserver.databinding.ActivityHomeBinding;
import com.senya.eatappserver.model.EventBus.CategoryClick;
import com.senya.eatappserver.model.EventBus.ChangeMenuClick;
import com.senya.eatappserver.model.EventBus.PrintOrderEvent;
import com.senya.eatappserver.model.EventBus.ToastEvent;
import com.senya.eatappserver.model.FCMResponse;
import com.senya.eatappserver.model.FCMSendData;
import com.senya.eatappserver.model.OrderModel;
import com.senya.eatappserver.model.RestaurantLocationModel;
import com.senya.eatappserver.remote.IFCMService;
import com.senya.eatappserver.remote.RetrofitFCMClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int PICK_IMAGE_REQUEST = 7171;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private NavController navController;
    private int menuClick = -1;

    private ImageView img_upload;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IFCMService ifcmService;
    private Uri imgUri = null;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private AlertDialog dialog;

    @OnClick(R.id.fab_chat)
    void onOpenChatList(){
        startActivity(new Intent(this,ChatListActivity.class));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarHome.toolbar);

        ButterKnife.bind(this);

        init();

        drawer = binding.drawerLayout;
        navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_category, R.id.nav_food_list, R.id.nav_order, R.id.nav_shipper)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        View headerView = navigationView.getHeaderView(0);
        TextView txt_user = (TextView) headerView.findViewById(R.id.txt_user);
        Common.setSpanString("Hello, ", Common.currentServerUser.getName(),txt_user);

        menuClick = R.id.nav_category; //Дефолт

        checkIsOpenFromActivity();
    }

    private void init(){
        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        subscribeToTopic(Common.createTopicOrder());
        updateToken();

        dialog = new AlertDialog.Builder(this).setCancelable(false)
                .setMessage("Please wait...")
                .create();
    }

    private void checkIsOpenFromActivity() {
        boolean isOpenFromNewOrder = getIntent().getBooleanExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER,false);
        if(isOpenFromNewOrder)
        {
            navController.popBackStack();
            navController.navigate(R.id.nav_order);
            menuClick = R.id.nav_order;
        }
    }

    private void updateToken() {
        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnFailureListener(e -> Toast.makeText(HomeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(instanceIdResult -> {
                    Common.updateToken(HomeActivity.this,instanceIdResult.getToken(),
                            true,false);


                });
    }

    private void subscribeToTopic(String topicOrder) {
        FirebaseMessaging.getInstance()
                .subscribeToTopic(topicOrder)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    if(!task.isSuccessful())
                        Toast.makeText(this, "Failed "+task.isSuccessful(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().unregister(this);
        compositeDisposable.clear();
        super.onStop();
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onCategoryClick(CategoryClick event)
    {
        if(event.isSuccess())
        {
            if(menuClick != R.id.nav_food_list)
            {
                navController.navigate(R.id.nav_food_list);
                menuClick = R.id.nav_food_list;
            }
        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onToastEvent(ToastEvent event)
    {
        if(event.getAction() == Common.ACTION.CREATE)
        {
            Toast.makeText(this, "Successfully Created!", Toast.LENGTH_SHORT).show();
        }
        else if(event.getAction() == Common.ACTION.UPDATE)
        {
            Toast.makeText(this, "Successfully Updated!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "Successfully Deleted!", Toast.LENGTH_SHORT).show();
        }

            EventBus.getDefault().postSticky(new ChangeMenuClick(event.isFromFoodList()));
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onChangeMenuClick(ChangeMenuClick event)
    {
        if(event.isFromFoodList())
        {
            //Очистить
            navController.popBackStack(R.id.nav_category,true);
            navController.navigate(R.id.nav_category);
        }
        else
        {
            navController.popBackStack(R.id.nav_food_list,true);
            navController.navigate(R.id.nav_food_list);
        }
        menuClick = -1;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        menuItem.setChecked(true);
        drawer.closeDrawers();
        switch (menuItem.getItemId())
        {
            case R.id.nav_category:
                if(menuItem.getItemId() != menuClick)
                {
                    navController.popBackStack(); //удалить все до этого открытые окна
                    navController.navigate(R.id.nav_category);
                }
                break;
            case R.id.nav_order:
                if(menuItem.getItemId() != menuClick)
                {
                    navController.popBackStack();
                    navController.navigate(R.id.nav_order);
                }
                break;
            case R.id.nav_shipper:
                if(menuItem.getItemId() != menuClick)
                {
                    navController.popBackStack();
                    navController.navigate(R.id.nav_shipper);
                }
                break;
            case R.id.nav_best_deals:
                if(menuItem.getItemId() != menuClick)
                {
                    navController.popBackStack();
                    navController.navigate(R.id.nav_best_deals);
                }
                break;
            case R.id.nav_most_popular:
                if(menuItem.getItemId() != menuClick)
                {
                    navController.popBackStack();
                    navController.navigate(R.id.nav_most_popular);
                }
                break;
            case R.id.nav_location:
                showUpdateLocationDialog();
                break;
            case R.id.nav_send_news:
                showNewsDialog();
                break;
            case R.id.nav_sign_out:
                signOut();
                break;
            default:
                menuClick = -1;
                break;
        }

        menuClick = menuItem.getItemId();
        return true;
    }

    private void showUpdateLocationDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Update location");
        builder.setMessage("Do you want to update this restaurant location?");

        builder.setNegativeButton("NO",(dialogInterface, i) -> {
           dialogInterface.dismiss();
        });
        builder.setPositiveButton("YES", (dialogInterface, i) -> {

            Dexter.withContext(HomeActivity.this)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new PermissionListener() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                            FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(HomeActivity.this);


                            fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
                                @NonNull
                                @Override
                                public boolean isCancellationRequested() {
                                    return true;
                                }
                                @Override
                                public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                                    return null;
                                }
                            }).addOnSuccessListener(location -> {

                                FirebaseDatabase.getInstance()
                                        .getReference(Common.RESTAURANT_REF)
                                        .child(Common.currentServerUser.getRestaurant())
                                        .child(Common.LOCATION_REF)
                                        .setValue(new RestaurantLocationModel(location.getLatitude(),location.getLongitude()))
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(HomeActivity.this, "Location updated successfully", Toast.LENGTH_SHORT).show();
                                        }).addOnFailureListener(e -> {
                                    Toast.makeText(HomeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });

                            }).addOnFailureListener(e -> Toast.makeText(HomeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show());

                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                            Toast.makeText(HomeActivity.this, "Accept this permission", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                        }
                    }).check();

        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showNewsDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("News System");
        builder.setMessage("Send notification to all clients");
        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_news_system,null);

        //View
        EditText edt_title = (EditText) itemView.findViewById(R.id.edt_title);
        EditText edt_content = (EditText) itemView.findViewById(R.id.edt_content);
        EditText edt_link = (EditText) itemView.findViewById(R.id.edt_link);
        img_upload = (ImageView) itemView.findViewById(R.id.img_upload);
        RadioButton rdi_none = (RadioButton) itemView.findViewById(R.id.rdi_none);
        RadioButton rdi_link = (RadioButton) itemView.findViewById(R.id.rdi_link);
        RadioButton rdi_upload = (RadioButton) itemView.findViewById(R.id.rdi_image);

        //Event
        rdi_none.setOnClickListener(view -> {
            edt_link.setVisibility(View.GONE);
            img_upload.setVisibility(View.GONE);
        });
        rdi_link.setOnClickListener(view -> {
            edt_link.setVisibility(View.VISIBLE);
            img_upload.setVisibility(View.GONE);
        });
        rdi_upload.setOnClickListener(view -> {
            edt_link.setVisibility(View.GONE);
            img_upload.setVisibility(View.VISIBLE);
        });
        img_upload.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
        });

        builder.setView(itemView);
        builder.setNegativeButton("CANCEL", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });
        builder.setPositiveButton("SEND", (dialogInterface, i) -> {
            if(rdi_none.isChecked())
            {
                sendNews(edt_title.getText().toString(),edt_content.getText().toString());
            }
            else if(rdi_link.isChecked())
            {
                sendNews(edt_title.getText().toString(),edt_content.getText().toString(),edt_link.getText().toString());
            }
            else if(rdi_upload.isChecked())
            {
                if(imgUri != null)
                {
                    AlertDialog dialog = new AlertDialog.Builder(this).setMessage("Uploading...").create();
                    dialog.show();

                    String file_name = UUID.randomUUID().toString();
                    StorageReference newsImages = storageReference.child("news/"+file_name);
                    newsImages.putFile(imgUri)
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }).addOnSuccessListener(taskSnapshot -> {
                                dialog.dismiss();
                                newsImages.getDownloadUrl().addOnSuccessListener(uri -> sendNews(edt_title.getText().toString(),edt_content.getText().toString(),uri.toString()));
                            }).addOnProgressListener(taskSnapshot -> {
                                double progress = Math.round((100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()));
                                dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                            });
                }
            }


        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void sendNews(String title, String content, String url) {
        Map<String,String> notificationData = new HashMap<String,String>();
        notificationData.put(Common.NOTI_TITLE,title);
        notificationData.put(Common.NOTI_CONTENT,content);
        notificationData.put(Common.IS_SEND_IMAGE,"true");
        notificationData.put(Common.IMAGE_URL,url);

        FCMSendData fcmSendData = new FCMSendData(Common.getNewsTopic(),notificationData);

        AlertDialog dialog = new AlertDialog.Builder(this).setMessage("Waiting...").create();
        dialog.show();

        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fcmResponse -> {
                    dialog.dismiss();
                    if(fcmResponse.getMessage_id() != 0)
                        Toast.makeText(this, "Notifications has been sent", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(this, "Failed to send notifications", Toast.LENGTH_SHORT).show();
                },throwable -> {
                    dialog.dismiss();
                    Toast.makeText(this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    private void sendNews(String title, String content) {
        Map<String,String> notificationData = new HashMap<String,String>();
        notificationData.put(Common.NOTI_TITLE,title);
        notificationData.put(Common.NOTI_CONTENT,content);
        notificationData.put(Common.IS_SEND_IMAGE,"false");

        FCMSendData fcmSendData = new FCMSendData(Common.getNewsTopic(),notificationData);

        AlertDialog dialog = new AlertDialog.Builder(this).setMessage("Waiting...").create();
        dialog.show();

        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(fcmResponse -> {
            dialog.dismiss();
            if(fcmResponse.getMessage_id() != 0)
                Toast.makeText(this, "Notifications has been sent", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Failed to send notifications", Toast.LENGTH_SHORT).show();
        },throwable -> {
            dialog.dismiss();
            Toast.makeText(this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }));
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign Out")
                .setMessage("Do you really want to sign out?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Common.selectedFood = null;
                Common.categorySelected = null;
                Common.currentServerUser = null;
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(HomeActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if(data != null && data.getData() != null)
            {
                imgUri = data.getData();
                img_upload.setImageURI(imgUri);
            }
        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onPrintEventListener(PrintOrderEvent event)
    {
        createPDFFile(event.getPath(),event.getOrderModel());
    }

    private void createPDFFile(String path, OrderModel orderModel) {
        dialog.show();

        if(new File(path).exists())
            new File(path).delete();
        try {
            Document document = new Document();
            //Save
            PdfWriter.getInstance(document,new FileOutputStream(path));
            //Open
            document.open();

            //Settings
            document.setPageSize(PageSize.A4);
            document.addCreationDate();
            document.addAuthor("My restaurant");
            document.addCreator(Common.currentServerUser.getName());

            //Font settings
            BaseColor colorAccent = new BaseColor(0,153,204,255);
            float fontSize = 20.0f;

            //Custom font
            BaseFont fontName = BaseFont.createFont("assets/fonts/brandon_medium.otf","UTF-8",BaseFont.EMBEDDED);

            //Title of document
            Font titleFont = new Font(fontName,36.0f,Font.NORMAL,BaseColor.BLACK);
            PDFUtils.addNewItem(document,"Order Details", Element.ALIGN_CENTER,titleFont);

            Font orderNumberfont = new Font(fontName,fontSize,Font.NORMAL,colorAccent);
            PDFUtils.addNewItem(document,"Order No:",Element.ALIGN_LEFT,orderNumberfont);
            Font orderNumberValueFont = new Font(fontName,20,Font.NORMAL,BaseColor.BLACK);
            PDFUtils.addNewItem(document,orderModel.getKey(),Element.ALIGN_LEFT,orderNumberValueFont);

            PDFUtils.addLineSeparator(document);

            PDFUtils.addNewItem(document,"Order Date",Element.ALIGN_LEFT,orderNumberfont);
            PDFUtils.addNewItem(document,new SimpleDateFormat("dd/MM/yyyy").format(orderModel.getCreateDate()),Element.ALIGN_LEFT,orderNumberValueFont);

            PDFUtils.addLineSeparator(document);

            //Account name
            PDFUtils.addNewItem(document,"Account Name:",Element.ALIGN_LEFT,orderNumberfont);
            PDFUtils.addNewItem(document,orderModel.getUserName(),Element.ALIGN_LEFT,orderNumberValueFont);

            PDFUtils.addLineSeparator(document);

            //Add product and details
            PDFUtils.addLineSpace(document);
            PDFUtils.addNewItem(document,"Product Details",Element.ALIGN_CENTER,titleFont);
            PDFUtils.addLineSeparator(document);

            Observable.fromIterable(orderModel.getCartItemList())
                    .flatMap(cartItem -> Common.getBitmapFromUrl(HomeActivity.this,cartItem,document))
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe( cartItem -> {

                        PDFUtils.addNewItemWithLeftAndRight(document,cartItem.getFoodName(),
                                ("(0.0%"),
                                titleFont,
                                orderNumberValueFont);

                        PDFUtils.addNewItemWithLeftAndRight(document,
                                "Size",
                                Common.formatSizeJsonToString(cartItem.getFoodSize()),
                                titleFont,
                                orderNumberValueFont);
                        PDFUtils.addNewItemWithLeftAndRight(document,
                                "Addons",
                                Common.formatAddonJsonToString(cartItem.getFoodAddon()),
                                titleFont,
                                orderNumberValueFont);

                        //Food Price
                        PDFUtils.addNewItemWithLeftAndRight(document,
                                new StringBuilder()
                                        .append(cartItem.getFoodQuantity())
                                .append("*")
                                .append(cartItem.getFoodPrice() + cartItem.getFoodExtraPrice())
                                .toString(),
                                new StringBuilder()
                                .append(cartItem.getFoodQuantity()*(cartItem.getFoodExtraPrice()+cartItem.getFoodPrice()))
                                .toString(),
                                titleFont,
                                orderNumberValueFont);

                        PDFUtils.addLineSeparator(document);

                    }, throwable -> {
                        dialog.dismiss();
                        Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_SHORT).show();

                    },()->{

                        PDFUtils.addLineSpace(document);
                        PDFUtils.addLineSpace(document);

                        PDFUtils.addNewItemWithLeftAndRight(document,"Total",
                                new StringBuilder()
                        .append(orderModel.getTotalPayment()).toString(),
                                titleFont,
                                titleFont);

                        document.close();
                        dialog.dismiss();
                        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();

                        printPdf();

                    });

        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printPdf() {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        try {
            PrintDocumentAdapter printDocumentAdapter = new PdfDocumentAdapter(this,new StringBuilder(Common.getAppPath(this))
            .append(Common.FILE_PRINT).toString());
            printManager.print("Document",printDocumentAdapter,new PrintAttributes.Builder().build());
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}