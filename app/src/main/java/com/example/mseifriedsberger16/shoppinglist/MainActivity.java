package com.example.mseifriedsberger16.shoppinglist;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;

import static java.util.stream.Collectors.toList;

public class MainActivity extends AppCompatActivity {
    private static final String FILENAME = "shoppingList.json";
    private static final int RQ_ACCESS_FINE_LOCATION = 3;
    private static final String CHANNEL_ID = "12345";
    private static final int RQ_PREFERENCES = 324;
    private Spinner spinner;
    private int RQ_read = 1;
    private int RQ_write = 2;
    private ListView listView;
    private Context ctx = this;
    private SharedPreferences prefs;
    private TypeToken<Map<Shop, List<Article>>> token = new TypeToken<Map<Shop, List<Article>>>() {
    };

    private Map<Shop, List<Article>> shoppingList = new HashMap<>();

    private MyAdapter spinnerAdapter;
    private ArrayAdapter<Article> listAdapter;
    private AlertDialog ad_add_Article;
    private AlertDialog ad_add_Shop;
    private View vDialog;

    private LocationManager locationManager;
    private boolean isGPSAllowed = false;
    private boolean isNotiAllowed = false;
    private LocationListener locationListener;
    private int notificationId = 5000;
    private SharedPreferences.OnSharedPreferenceChangeListener prefChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        prefChangeListener = (sharedPrefs, key) -> preferenceChanged(sharedPrefs, key);
        spinner = findViewById(R.id.spinner);
        listView = findViewById(R.id.listView);
        registerForContextMenu(listView);
        registerSystemService();

        spinnerAdapter = new MyAdapter(this, R.layout.mylayout, new LinkedList(shoppingList.keySet()));
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        bindAdapterToListView();
        initSpinner();

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RQ_ACCESS_FINE_LOCATION);
        } else {
            gpsGranted();
        }

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RQ_read);
        } else {
            readJSON();
        }


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String shopName = spinner.getSelectedItem().toString();
                Shop shop = null;

                for (Shop s : shoppingList.keySet()) {

                    if (s.getName().equals(shopName)) {
                        shop = s;
                    }
                }

                if (shop != null) {
                    listAdapter.clear();
                    listAdapter.addAll(shoppingList.get(shop));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.d("TAG", "onLocationChanged");
                    if (shoppingList.size() > 0) {
                        isNotiAllowed = prefs.getBoolean("noti", false);

                        if (isNotiAllowed) {
                            for (Shop s : shoppingList.keySet()) {
                                Location l = new Location("destination");
                                l.setLatitude(s.getLat());
                                l.setLongitude(s.getLongi());

                                float distance = location.distanceTo(l);

                                if (distance <= 1000f) {
                                    List<Article> articles = shoppingList.get(spinner.getSelectedItem());

                                    StringBuilder sbuilder = new StringBuilder();
                                    for (Article a :
                                            articles) {
                                        sbuilder.append(a.toString() + "\n\r");
                                    }

                                    // Create an explicit intent for an Activity in your app
                                    Intent intent = new Intent(ctx, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0);

                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                                            .setContentTitle("Einkaufsliste für nahegelegenen " + s.getName())
                                            .setContentText(sbuilder.toString())
                                            .setStyle(new NotificationCompat.BigTextStyle()
                                                    .bigText(sbuilder.toString()))
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            // Set the intent that will fire when the user taps the notification
                                            .setContentIntent(pendingIntent)
                                            .setAutoCancel(true);

                                    createNotificationChannel();
                                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);

                                    // notificationId is a unique int for each notification that you must define
                                    notificationManager.notify(notificationId, builder.build());

                                }

                            }
                        }
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Log.d("TAG", "onStatusChanged");
                }

                @Override
                public void onProviderEnabled(String provider) {
                    Log.d("TAG", "onProviderEnabled");
                }

                @Override
                public void onProviderDisabled(String provider) {
                    Log.d("TAG", "onProviderDisabled");
                }
            };
        }


    private void preferenceChanged(SharedPreferences sharedPrefs, String key) {
        isNotiAllowed = sharedPrefs.getBoolean(key, false);
    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "Channel Name";
            String description = "Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("TAG", "onResume");
        super.onPostResume();
        if (isGPSAllowed) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    3000,
                    1,
                    locationListener);
        }
    }

    @Override
    protected void onPause() {
        Log.d("TAG", "onPause");
        super.onPause();
        if (isGPSAllowed) locationManager.removeUpdates(locationListener);
    }

    private void registerSystemService() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // from Api 23 and above you can call getSystemService this way:
        // locationManager = (LocationManager) getSystemService(LocationManager.class);
    }

    private void checkPermissionGPS() {
        Log.d("TAG", "checkPermissionGPS");
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        if (ActivityCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    RQ_ACCESS_FINE_LOCATION);
        } else {
            //isGPSAllowed = true;
            gpsGranted();
        }

    }

    private void gpsGranted() {
        Log.d("TAG", "gps permission granted!");
        isGPSAllowed = true;
        //showAvailableProviders();

    }

    private void initSpinner() {
        spinner.setAdapter(spinnerAdapter);
    }


    private void bindAdapterToListView() {
        listView.setAdapter(listAdapter);
    }

    private void readJSON() {
        /*
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) return;
        File outFile = Environment.getExternalStorageDirectory();
        //String path = outFile.getAbsolutePath();
        //String fullPath = path + FILENAME;
        File f = new File(outFile, FILENAME);
        try {
            //FileInputStream fis = openFileInput(FILENAME);
            //BufferedReader in = new BufferedReader(new InputStreamReader(fis));
            BufferedReader in = new BufferedReader(new FileReader(f));
            Gson gson = new Gson();
            shoppingList = gson.fromJson(in, token.getType());

            in.close();
        } catch (IOException exp) {
            Log.d("TAG", exp.getStackTrace().toString());
        }

        //try {
        //    FileInputStream fis = openFileInput(fullPath);
          //  BufferedReader in = new BufferedReader(new InputStreamReader(fis));
            //Gson gson = new Gson();
            //shoppingList = gson.fromJson(in, token.getType());
            //in.close();
        //} catch(IOException exp) {
        //    Log.d("TAG", exp.getStackTrace().toString());
        //}

        spinnerAdapter = new MyAdapter(this, R.layout.mylayout, new LinkedList(shoppingList.keySet()));
        initSpinner();
        spinnerAdapter.notifyDataSetChanged();
        */

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);
        if (requestCode == RQ_read) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                //user does not allow
            } else {
                readJSON();
            }
        } else if (requestCode == RQ_write) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                //user does not allow
            } else {
                writeJSON();
            }
        } else if (requestCode == RQ_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                //user does not allow
            } else {
                gpsGranted();
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        int viewId = v.getId();
        if (viewId == R.id.listView) {
            getMenuInflater().inflate(R.menu.context_menu, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_delete) {
            AdapterView.AdapterContextMenuInfo info =
                    (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            String name = "";
            if (info != null) {
                int pos = info.position;

                List<Article> list = shoppingList.get(spinner.getSelectedItem());

                list.remove(listView.getAdapter().getItem(pos));
                listAdapter.clear();
                listAdapter.addAll(list);
            }


            return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_new_article:
                createArticle();
                break;
            case R.id.menu_new_shop:
                createShop();
                break;

            case R.id.menu_save:

                writeJSON();

                break;

            case R.id.menu_preferences:
                Intent intent = new Intent(ctx, MySettingsActivity.class);
                startActivityForResult(intent, RQ_PREFERENCES);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createArticle() {
        vDialog = getLayoutInflater().inflate(R.layout.add_article_dialog, null);
        ad_add_Article = new AlertDialog.Builder(this)
                .setMessage("Neuen Artikel für " + ((Shop)spinner.getSelectedItem()).getName() + " anlegen:")
                .setView(vDialog)
                .setPositiveButton("Anlegen", (dialog, which) -> {
                    EditText e = vDialog.findViewById(R.id.article_name);
                    EditText n = vDialog.findViewById(R.id.amount);
                    List<Article> list = shoppingList.get(spinner.getSelectedItem());
                    list.add(new Article((list.size() + 1), e.getText().toString(), Float.parseFloat(n.getText().toString())));
                    listAdapter.clear();
                    listAdapter.addAll(shoppingList.get(spinner.getSelectedItem()));
                })
                .setNegativeButton("Beenden", null)
                .show();
    }

    private void createShop() {

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        EditText latitude = new EditText(this);
        latitude.setHint("Breitengrad");
        latitude.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        ll.addView(latitude);
        EditText longitude = new EditText(this);
        longitude.setHint("Längengrad");
        longitude.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        ll.addView(longitude);
        EditText name = new EditText(this);
        name.setHint("Shopname");
        ll.addView(name);


        new AlertDialog.Builder(this)
                .setMessage("Neuen Shop anlegen")
                .setView(ll)
                .setNeutralButton("Aktuelle Koordinaten übernehmen", ((dialog, which) -> {
                    if (!isGPSAllowed){
                        checkPermissionGPS();
                    }
                    else{

                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                       }
                        Location location = locationManager.getLastKnownLocation(
                                LocationManager.GPS_PROVIDER);

                        //latitude.setText(String.valueOf(location.getLatitude()));
                        //longitude.setText(String.valueOf(location.getLongitude()));


                        String shopName = name.getText().toString();
                        Double lat = Double.parseDouble(String.valueOf(location.getLatitude()));
                        Double longi = Double.parseDouble(String.valueOf(location.getLongitude()));
                        Shop s = new Shop(shopName, lat, longi);
                        shoppingList.put(s, new LinkedList<>());

                        List l = new LinkedList(shoppingList.keySet());

                        spinnerAdapter = new MyAdapter(this, R.layout.mylayout, l);
                        initSpinner();
                        spinnerAdapter.notifyDataSetChanged();
                    }


                }))
                .setPositiveButton("Anlegen", (dialog, which) -> {
                    String shopName = name.getText().toString();
                    Double lat = Double.parseDouble(latitude.getText().toString());
                    Double longi = Double.parseDouble(longitude.getText().toString());
                    Shop s = new Shop(shopName, lat, longi);
                    shoppingList.put(s, new LinkedList<>());

                    //ArrayList<Shop> shopList = new ArrayList<>();
                    //shopList.addAll(shoppingList.keySet());

                    spinnerAdapter = new MyAdapter(this, R.layout.mylayout, new LinkedList(shoppingList.keySet()));
                    initSpinner();
                    spinnerAdapter.notifyDataSetChanged();
                })
                .setNegativeButton("Beenden", null)
                .show();


        /*final EditText edit = new EditText(this);
        ad_add_Shop = new AlertDialog.Builder(this)
                .setMessage("Neuen Shop anlegen:")
                .setView(edit)
                .setPositiveButton("Anlegen", (dialog, which) -> {
                    String shopName = edit.getText().toString();
                    shoppingList.put(, new LinkedList<Article>());
                    spinnerAdapter.clear();
                    spinnerAdapter.addAll(shoppingList.keySet());
                })
                .setNegativeButton("Beenden", null)
                .show();*/

    }

    private void writeJSON() {
        Gson gson = new Gson();

        String sJson = gson.toJson(shoppingList, token.getType());
        writeToFile(sJson);

    }

    private void writeToFile(String txtInput) {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RQ_write);
        } else {
            String state = Environment.getExternalStorageState();
            if (!state.equals(Environment.MEDIA_MOUNTED)) return;
            File outFile = Environment.getExternalStorageDirectory();
            String path = outFile.getAbsolutePath();
            String fullPath = path + File.separator + FILENAME;
            try {
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(fullPath)));
                out.println(txtInput);
                out.flush();
                out.close();
            } catch (Exception e) {
                Log.e("TAG", e.getLocalizedMessage());
            }
        }

        /*try {
            FileOutputStream fos = openFileOutput(FILENAME, MODE_PRIVATE);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(fos));
            out.println(txtInput);
            out.flush();
            out.close();
        } catch (FileNotFoundException exp) {
            Log.d("TAG", exp.getStackTrace().toString());
        }*/
    }


}
