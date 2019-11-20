package com.tyjradio.jrdvoicerecorder.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tyjradio.jrdvoicerecorder.DetailActivity;
import com.tyjradio.jrdvoicerecorder.R;
import com.tyjradio.jrdvoicerecorder.bean.AudioRecorderItemBean;
import com.tyjradio.jrdvoicerecorder.utils.AudioUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends BaseAdapter {
    private List<AudioRecorderItemBean> audios=new ArrayList<>();
    private Context context;


    public SearchAdapter(Context context, List<AudioRecorderItemBean> audios){
        this.context=context;
        this.audios=audios;
    }

    public void refreshData(List<AudioRecorderItemBean> audios){
        this.audios=audios;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return audios.size();
    }

    @Override
    public Object getItem(int position) {
        return audios.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder=null;
        if(convertView==null){
            convertView=LayoutInflater.from(context).inflate(R.layout.item_audio_adapter,null);
            viewHolder=new ViewHolder();
            viewHolder.tvName=convertView.findViewById(R.id.id_tv_name);
            viewHolder.tvYear=convertView.findViewById(R.id.id_year);
            viewHolder.tvTime=convertView.findViewById(R.id.id_time);
            viewHolder.ivAni=convertView.findViewById(R.id.id_play_ani_iv);
            viewHolder.ivInfo=convertView.findViewById(R.id.id_info_iv);
            convertView.setTag(viewHolder);
        }
        viewHolder= (ViewHolder) convertView.getTag();
        viewHolder.tvName.setText(audios.get(position).getAudioFile());
        viewHolder.tvYear.setText(audios.get(position).getCallingTime());
        viewHolder.tvTime.setText(AudioUtils.timeParse(audios.get(position).getDurationTime()));
        viewHolder.ivInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到详情页面
                Intent intent=new Intent();
                intent.setClass(context,DetailActivity.class);
                intent.putExtra("audio",audios.get(position));
                context.startActivity(intent);
            }
        });

        return convertView;
    }


    class ViewHolder{
        TextView tvName;
        TextView tvYear;
        TextView tvTime;
        ImageView ivAni;
        ImageView ivInfo;



    }
}
