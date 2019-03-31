package com.example.mseifriedsberger16.shoppinglist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

public class MyAdapter extends BaseAdapter {
    private List<Shop> shops;
    private int layoudId;
    private LayoutInflater inflater;

    public MyAdapter(Context ctx, int layoutId, List shops) {
        this.shops = shops;
        this.layoudId = layoutId;
        this.inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return shops.size();
    }

    @Override
    public Object getItem(int i) {
        return shops.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Shop s = shops.get(i);
        View listItem = (view == null) ? inflater.inflate(this.layoudId, null) : view;
        ((TextView) listItem.findViewById(R.id.txt_article)).setText(s.getName());

        return listItem;
    }
}
