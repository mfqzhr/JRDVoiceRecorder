package com.tyjradio.jrdvoicerecorder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tyjradio.jrdvoicerecorder.R;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends BaseAdapter {

    private List<String> searchHistorys = new ArrayList<>();
    private Context context;

    public HistoryAdapter(Context context,List<String> searchHistorys ){
        this.context=context;
        this.searchHistorys=searchHistorys;

    }
    @Override
    public int getCount() {
        return searchHistorys.size();
    }

    @Override
    public Object getItem(int position) {
        return searchHistorys.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder=null;
        if(convertView==null){
            convertView=LayoutInflater.from(context).inflate(R.layout.item_history_adapter,null);
            viewHolder=new ViewHolder();
            viewHolder.tvName=convertView.findViewById(R.id.id_search_tv);
            convertView.setTag(viewHolder);
        }
        viewHolder= (ViewHolder) convertView.getTag();
        viewHolder.tvName.setText(searchHistorys.get(position));
        return convertView;
    }


    class  ViewHolder{
        TextView tvName;
    }
}
