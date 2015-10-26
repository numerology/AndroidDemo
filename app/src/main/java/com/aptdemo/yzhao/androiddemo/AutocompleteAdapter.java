package com.aptdemo.yzhao.androiddemo;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;

/**
 * Created by Yicong on 10/23/15.
 */
public class AutocompleteAdapter extends ArrayAdapter<String> implements Filterable{

    ArrayList<String> resultList;

    Context mContext;
    int mResource;

    AutocompleteAPI mAutocompleteAPI = new AutocompleteAPI(Consts.AutocompleteType.ALL); // default auto complete api is matching all strings

    public AutocompleteAdapter(Context context, int resource){
        super(context, resource);

        mContext = context;
        mResource = resource;
    }
    public void setAutocompleteAPIType(Consts.AutocompleteType newType){
        mAutocompleteAPI = new AutocompleteAPI(newType);
    }
    @Override
    public int getCount(){
        return resultList.size();
    }

    @Override
    public String getItem(int position){
        return resultList.get(position);
    }

    @Override
    public Filter getFilter(){
        Filter filter = new Filter(){
            @Override
            protected FilterResults performFiltering(CharSequence constraint){
                FilterResults filterResults = new FilterResults();
                if (constraint != null){
                    resultList = mAutocompleteAPI.autocomplete(constraint.toString());

                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results){
                if (results != null && results.count >0){
                    notifyDataSetChanged();
                }
                else{
                    notifyDataSetInvalidated();
                }
            }
        }; // end of new filter
        return filter;
    }
}
