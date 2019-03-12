package com.example.mseifriedsberger16.shoppinglist;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;

public class MainActivity extends AppCompatActivity {
    private static final String FILENAME = "shoppingList.json";
    private Spinner spinner;
    private int RQ_read = 1;
    private int RQ_write = 2;
    private ListView listView;
    private TypeToken<Map<String, List<Article>>> token = new TypeToken<Map<String, List<Article>>>() {
    };

    private Map<String, List<Article>> shoppingList = new HashMap<>();

    private ArrayAdapter<String> spinnerAdapter;
    private ArrayAdapter<Article> listAdapter;
    private AlertDialog ad_add_Article;
    private AlertDialog ad_add_Shop;
    private View vDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = findViewById(R.id.spinner);
        listView = findViewById(R.id.listView);

        spinnerAdapter  = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        listAdapter =  new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RQ_read);
        } else {
            readJSON();
        }

        bindAdapterToListView();
        initSpinner();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String shop = spinner.getSelectedItem().toString();
                listAdapter.clear();
                listAdapter.addAll(shoppingList.get(shop));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void initSpinner() {
        spinner.setAdapter(spinnerAdapter);
    }


    private void bindAdapterToListView() {
        listView.setAdapter(listAdapter);
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
        } else if (requestCode == RQ_write) {
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
                createArticle();
                break;
            case R.id.menu_new_shop:
                createShop();
                break;

            case R.id.menu_save:
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RQ_write);
                } else {
                    writeJSON();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createArticle() {
        vDialog = getLayoutInflater().inflate(R.layout.add_article_dialog, null);
        ad_add_Article = new AlertDialog.Builder(this)
                .setMessage("Neuen Artikel für " + spinner.getSelectedItem() + " anlegen:")
                .setView(vDialog)
                .setPositiveButton("Anlegen", ( dialog, which) -> {
                    EditText e = vDialog.findViewById(R.id.article_name);
                    EditText n = vDialog.findViewById(R.id.amount);
                    List<Article> list = shoppingList.get(spinner.getSelectedItem());
                    list.add(new Article((list.size()+1), e.getText().toString(), Float.parseFloat(n.getText().toString())));
                    listAdapter.clear();
                    listAdapter.addAll(shoppingList.get(spinner.getSelectedItem()));
                })
                .setNegativeButton("Beenden", null)
                .show();
    }

    private void createShop() {
        final EditText edit = new EditText(this);
        ad_add_Shop = new AlertDialog.Builder(this)
                .setMessage("Neuen Shop anlegen:")
                .setView(edit)
                .setPositiveButton("Anlegen", ( dialog, which) -> {
                    shoppingList.put(edit.getText().toString(), new LinkedList<Article>());
                    spinnerAdapter.clear();
                    spinnerAdapter.addAll(shoppingList.keySet());
                })
                .setNegativeButton("Beenden", null)
                .show();

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
