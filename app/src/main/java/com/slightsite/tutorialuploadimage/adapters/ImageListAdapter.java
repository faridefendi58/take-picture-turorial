package com.slightsite.tutorialuploadimage.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.slightsite.tutorialuploadimage.R;

import java.util.ArrayList;
import java.util.HashMap;

public class ImageListAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, Bitmap>> data;
    private static LayoutInflater inflater = null;

    public ImageListAdapter(Activity a, ArrayList<HashMap<String, Bitmap>> d) {
        activity = a;
        data = d;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if(convertView == null)
            vi = inflater.inflate(R.layout.list_view_image, null);

        ImageView thumb_image = (ImageView)vi.findViewById(R.id.list_image); // thumb image

        thumb_image.setImageBitmap(data.get(position).get("img"));

        return vi;
    }
}
