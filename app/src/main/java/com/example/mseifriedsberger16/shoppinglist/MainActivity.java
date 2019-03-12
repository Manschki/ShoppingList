package com.example.mseifriedsberger16.shoppinglist;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String FILENAME = "shoppingList.json";
    private Spinner spinner;
    private int RQ_read = 1;
    private int RQ_write = 2;
    private ListView listView;
    private TypeToken<Map<String, List<Article>>> token = new TypeToken<Map<String, List<Article>>>(){};

    private Map<String, List<Article>> shoppingList = new HashMap<>();

    private ArrayAdapter spinnerAdapter;
    private ArrayAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = findViewById(R.id.spinner);
        listView = findViewById(R.id.listView);

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RQ_read);
        } else {
            readJSON();
        }
    }

    private void bindAdapterToListView(){
        listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);

    }

    private void readJSON() {
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) return;
        File outFile = Environment.getExternalStorageDirectory();
        String path = outFile.getAbsolutePath();
        String fullPath = path + File.separator + FILENAME;
        try {
            FileInputStream fis = openFileInput(FILENAME);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));
            Gson gson = new Gson();
            shoppingList = gson.fromJson(in, token.getType());

            in.close();
        } catch (IOException exp) {
            Log.d("TAG", exp.getStackTrace().toString());
        }

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
        }
        else if(requestCode == RQ_write){
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                //user does not allow
            } else {
                writeJSON();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_new_article:

                break;
            case R.id.menu_new_shop:

                break;

            case R.id.menu_save:
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        !=PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},RQ_write);
                } else {
                   writeJSON();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void writeJSON() {
        Gson gson = new Gson();
        String sJson = gson.toJson(shoppingList);
        writeToFile(sJson);

    }

    private void writeToFile(String txtInput) {

        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) return;
        File outFile = Environment.getExternalStorageDirectory();
        String path = outFile.getAbsolutePath();
        String fullPath = path + File.separator + FILENAME;
        try {
            PrintWriter out = new PrintWriter(
                            new FileOutputStream(fullPath));
            out.print(txtInput);
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.e("TAG", e.getLocalizedMessage());
        }
    }


}
