package com.aptdemo.yzhao.androiddemo;


import android.text.TextDirectionHeuristic;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.GridView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageCaptionAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> imageURLs;
    private ArrayList<String> captions;
    //TODO: adding case when the url is unavailable
    public ImageCaptionAdapter(Context c, ArrayList<String> imageURLs, ArrayList<String> captions ) {
        mContext = c;
        this.imageURLs = imageURLs;
        this.captions = captions;
    }

    public int getCount() {
        return imageURLs.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        TextView textView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            //   imageView = new ImageView(mContext);
            //   imageView.setLayoutParams(new GridView.LayoutParams(250, 250));
            //   imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            LayoutInflater i = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = (View) i.inflate(R.layout.image_caption_view, parent, false);
            imageView = (ImageView) convertView.findViewById(R.id.image);
            textView = (TextView) convertView.findViewById(R.id.caption);
            convertView.setLayoutParams(new GridView.LayoutParams(250, 250));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        } else {
            imageView = (ImageView) convertView.findViewById(R.id.image);
            textView = (TextView) convertView.findViewById(R.id.caption);
        }
        if ((imageURLs.get(position)!=null && !imageURLs.get(position).isEmpty())) {
            Picasso.with(mContext).load(imageURLs.get(position)).into(imageView);
            if(!captions.get(position).equals("null")) {
                textView.setText(captions.get(position).substring(0,20));
            }else{
                textView.setText("");
            }
        }
        return convertView;
    }

}