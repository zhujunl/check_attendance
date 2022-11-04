package com.miaxis.face.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.miaxis.face.R;
import com.miaxis.face.app.GlideApp;
import com.miaxis.face.bean.IDCardRecord;
import com.miaxis.face.util.DateUtil;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LogAdpter extends BaseAdapter  {

    List<IDCardRecord> list;
    private Context context;

    public LogAdpter( Context context) {
        this.context = context;
    }

    public void setList(List<IDCardRecord> list){
        this.list=list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if(list!=null){
            return list.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if(list!=null){
            return list.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_log, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        IDCardRecord st=list.get(position);
        holder.name.setText(st.getName());
        holder.idcard.setText(st.getCardNumber());
        holder.way.setText(st.getWay());
        holder.time.setText(DateUtil.toMonthDay(st.getVerifyTime()));
        showimg(holder.img,st.getFacePhotoPath());
        return convertView;
    }

    static class ViewHolder{
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.idcard)
        TextView idcard;
        @BindView(R.id.way)
        TextView way;
        @BindView(R.id.time)
        TextView time;
        @BindView(R.id.img)
        ImageView img;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    void showimg(ImageView view,String path){
        if (path!=null){
            File file=new File(path);
            GlideApp.with(context).load(file)
                    .override(300,300)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(view);
        }
    }

}
