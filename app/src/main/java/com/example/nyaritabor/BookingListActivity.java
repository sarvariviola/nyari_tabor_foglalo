package com.example.nyaritabor;

import android.os.Vibrator;
import android.content.Context;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.content.IntentFilter;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.Manifest;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class BookingListActivity extends AppCompatActivity {
    private static final String LOG_TAG = BookingListActivity.class.getName();

    private FirebaseUser user;
    private int favouriteItems = 0;
    private FirebaseUser mAuth;
    private RecyclerView mRecyclerView;
    private ArrayList<BookingCamp> mItemsData;
    private BookingCampAdapter mAdapter;
    private FirebaseFirestore mFirestore;
    private CollectionReference mItems;

    private Integer itemLimit = 30;


    private int gridNumber = 1;
    private NotificationHelper mNotificationHelper;
    private AlarmManager mAlarmManager;
    private static final String ADMIN_EMAIL = "admin@example.com";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking_list);


        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            Log.d(LOG_TAG, "Authenticated user!");
        } else {
            Log.d(LOG_TAG, "Unauthenticated user!");
            finish();
        }

        mRecyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager lm = new GridLayoutManager(this, gridNumber);
        mRecyclerView.setLayoutManager(lm);
        mItemsData = new ArrayList<>();
        mAdapter = new BookingCampAdapter(this, mItemsData);
        mRecyclerView.setAdapter(mAdapter);

        mFirestore = FirebaseFirestore.getInstance();
        mItems = mFirestore.collection("Camps");

        queryData();


        mAdapter.setOnBookingClickListener(new BookingCampAdapter.OnBookingClickListener() {
            @Override
            public void onBookingClick(BookingCamp camp) {
                foglalasLefoglalasa(camp);
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(powerReceiver, filter);
        mNotificationHelper = new NotificationHelper(this);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        setAlarmManager();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }


    }

    BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();

            if (intentAction == null)
                return;

            switch (intentAction) {
                case Intent.ACTION_POWER_CONNECTED:
                    itemLimit = 10;
                    queryData();
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    itemLimit = 5;
                    queryData();
                    break;
            }
        }
    };

    private void initializeData() {
        String[] campsList = getResources()
                .getStringArray(R.array.camp_item_names);
        String[] campsInfo = getResources()
                .getStringArray(R.array.camp_item_desc);
        String[] campsDate = getResources()
                .getStringArray(R.array.camp_item_date);
        String[] campsAgeGroup = getResources()
                .getStringArray(R.array.camp_item_age_groups);
        String[] campsPrice = getResources()
                .getStringArray(R.array.camp_item_prices);
        TypedArray campsImageResources = getResources().obtainTypedArray(R.array.camp_item_images);
        TypedArray campsRate = getResources().obtainTypedArray(R.array.camp_item_rates);
        String[] campsPlace = getResources()
                .getStringArray(R.array.camp_item_locations);


        for (int i = 0; i < campsList.length; i++) {
            int parsedPrice = Integer.parseInt(campsPrice[i].replace("€", "").trim());

            mItems.add(new BookingCamp(
                    campsImageResources.getResourceId(i, 0),
                    campsInfo[i],
                    campsList[i],
                    parsedPrice,
                    campsRate.getFloat(i, 0),
                    campsAgeGroup[i],
                    campsDate[i],
                    150,
                    campsPlace[i]
            ));
        }

        campsImageResources.recycle();

    }

    private void queryData() {
        mItemsData.clear();

        mItems.orderBy("freePlace", Query.Direction.DESCENDING).limit(itemLimit).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                BookingCamp item = document.toObject(BookingCamp.class);
                item.setId(document.getId());
                mItemsData.add(item);
            }

            if (mItemsData.size() == 0) {
                initializeData();
                queryData();
            }

            // Notify the adapter of the change.
            mAdapter.notifyDataSetChanged();
        });
    }
    public void deleteCamp(BookingCamp item) {
        DocumentReference ref = mItems.document(item._getId());
        ref.delete()
                .addOnSuccessListener(success -> {
                    Log.d(LOG_TAG, "Item is successfully deleted: " + item._getId());
                })
                .addOnFailureListener(fail -> {
                    Toast.makeText(this, "Item " + item._getId() + " cannot be deleted.", Toast.LENGTH_LONG).show();
                });

        queryData();
        mNotificationHelper.cancel();
    }

    private void foglalasLefoglalasa(BookingCamp camp) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.isAnonymous()) {
            Toast.makeText(this, "Csak regisztrált felhasználók foglalhatnak!", Toast.LENGTH_SHORT).show();
            return;
        }
        int szabadHelyek = camp.getFreePlace();

        if (szabadHelyek > 0) {
            szabadHelyek--;
            camp.setFreePlace(szabadHelyek);

            DocumentReference ref = mItems.document(camp._getId());
            ref.update("freePlace", szabadHelyek)
                    .addOnSuccessListener(aVoid -> {
                        mAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Sikeres foglalás!", Toast.LENGTH_SHORT).show();

                        String message = "Foglalásod sikeres! Nézz meg további táborokat is!";
                        mNotificationHelper.send(message);

                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        if (vibrator != null && vibrator.hasVibrator()) {
                            vibrator.vibrate(200); // 200 ms rezgés
                        }

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Hiba a foglalás során: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } else {
            Toast.makeText(this, "Nincs több szabad hely!", Toast.LENGTH_SHORT).show();
        }
    }

    private void complexQuery1() {
        mItemsData.clear();
        mAdapter.notifyDataSetChanged();

        mItems
                .whereEqualTo("place", "Budapest")
                .orderBy("freePlace", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snaps -> {
                    Log.d(LOG_TAG, "Budapest items count: " + snaps.size());
                    for (QueryDocumentSnapshot doc : snaps) {
                        BookingCamp item = doc.toObject(BookingCamp.class);
                        item.setId(doc.getId());
                        mItemsData.add(item);
                    }
                    mAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG, "complexQuery1 failed: " + e.getMessage());
                    Toast.makeText(this, "Lekérdezés hiba: index épülhet", Toast.LENGTH_SHORT).show();
                });
    }
    private void complexQuery2() {
        mItemsData.clear();
        mItems.whereLessThan("price", 100)
                .orderBy("price")
                .orderBy("name")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        BookingCamp item = document.toObject(BookingCamp.class);
                        item.setId(document.getId());
                        mItemsData.add(item);
                    }
                    mAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(LOG_TAG, "complexQuery2 failed: " + e.getMessage()));
    }

    private void complexQuery3() {
        mItemsData.clear();

        mItems
                .whereGreaterThanOrEqualTo("ratedInfo", 4.0)
                .orderBy("price", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        BookingCamp item = document.toObject(BookingCamp.class);
                        item.setId(document.getId());
                        mItemsData.add(item);
                    }
                    mAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e(LOG_TAG, "complexQuery3 failed: " + e.getMessage())
                );
    }

    private void complexQuery4() {
        mItemsData.clear();
        mAdapter.notifyDataSetChanged();

        mItems
                .whereEqualTo("place", "Szeged")
                .orderBy("freePlace", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snaps -> {
                    Log.d(LOG_TAG, "Szeged items count: " + snaps.size());
                    for (QueryDocumentSnapshot doc : snaps) {
                        BookingCamp item = doc.toObject(BookingCamp.class);
                        item.setId(doc.getId());
                        mItemsData.add(item);
                    }
                    mAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG, "complexQuery1 failed: " + e.getMessage());
                    Toast.makeText(this, "Lekérdezés hiba: index épülhet", Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.booking_list_menu, menu);
        // Keressük meg az admin menüpontot
        MenuItem addCampItem = menu.findItem(R.id.add_camp);

        // Ellenőrizzük, hogy bejelentkezett-e admin
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null && user.getEmail().equals(ADMIN_EMAIL)) {
            addCampItem.setVisible(true);  // csak az admin látja
        } else {
            addCampItem.setVisible(false);
        }


        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(LOG_TAG, s);
                mAdapter.getFilter().filter(s);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.log_out_button) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return true;
        }
        else if (id == R.id.query1) {
            complexQuery1();
            return true;
        }
        else if (id == R.id.query2) {
            complexQuery2();
            return true;
        }
        else if (id == R.id.query3) {
            complexQuery3();
            return true;
        }
        else if (id == R.id.query4) {
            complexQuery4();
            return true;
        }
        else if (id == R.id.add_camp) {
            // Ide jön az új tábort hozzáadó Activity indítása
            Intent intent = new Intent(this, AddCampActivity.class);
            startActivity(intent);
            return true; }
        else if (id == R.id.about_menu) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(powerReceiver);
    }

    private void setAlarmManager() {
        long repeatInterval = 60000;
        long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        mAlarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTime,
                repeatInterval,
                pendingIntent);
    }

}
