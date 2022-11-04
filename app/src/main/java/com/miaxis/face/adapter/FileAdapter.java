package com.miaxis.face.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.miaxis.face.R;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.MyViewHolder> {

    private List<String> list;
    private Context context;
    private MyViewHolder viewHolder;
    private AdaperListener listener;


    public FileAdapter(List<String> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_file,viewGroup,false);
        viewHolder=new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder h, int i) {
        String name=list.get(i);
        h.path.setText(name);
        h.path.setOnClickListener(v -> listener.OnClick(name));
    }

    @Override
    public int getItemCount() {
        if (list!=null){
            return list.size();
        }
        return 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setList(List<String> list){
        this.list=list;
        notifyDataSetChanged();
    }

    public void setListener(AdaperListener listener){
        this.listener=listener;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView path;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            path=itemView.findViewById(R.id.filepath);
        }
    }

    public interface AdaperListener{
        void OnClick(String name);
    }
}
