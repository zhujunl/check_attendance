package com.miaxis.face.view.custom;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.miaxis.face.R;
import com.miaxis.face.adapter.FileAdapter;
import com.miaxis.face.adapter.LogAdpter;
import com.miaxis.face.app.App;
import com.miaxis.face.bean.IDCardRecord;
import com.miaxis.face.event.SearchDoneEvent;
import com.miaxis.face.service.QueryRecordService;
import com.miaxis.face.util.ExcelUtils;
import com.miaxis.face.util.FileUtil;
import com.miaxis.face.view.fragment.BaseDialogFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class CheckLogFragment extends BaseDialogFragment {

    @BindView(R.id.log_back)
    TextView back;
    @BindView(R.id.log_export)
    TextView export;
    @BindView(R.id.loglist)
    ListView recyclelist;
    @BindView(R.id.checklog)
    TextView checklog;

    private Unbinder bind;
    private LogAdpter logAdpter;
    View view;
    private ProgressDialog pd;
    String start;
    String end;

    public static CheckLogFragment newInstance(String s,String e) {
        CheckLogFragment checkLogFragment = new CheckLogFragment();
        checkLogFragment.setTime(s,e);
        return checkLogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_logcheck, container, false);
        bind = ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        QueryRecordService.startActionQuery(getActivity(), start,end);
        checklog.setText("日志查看 "+start+"~"+end);
        pd = new ProgressDialog(getActivity());
        pd.setMessage("正在检索...");
        pd.setCancelable(false);
        pd.show();
        logAdpter=new LogAdpter(getActivity());
        recyclelist.setAdapter(logAdpter);
        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity());
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
        return dialog;
    }

    public void setTime(String s, String e){
        start=s;
        end=e;
    }

    @OnClick(R.id.log_back)
    void back(){dismiss();}

    @OnClick(R.id.log_export)
    void export(){
       FileUtil.showFile(getActivity(),start,end,Exportlist,true);
    }

    List<IDCardRecord> Exportlist;
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSearchDoneEvent(SearchDoneEvent e) {
        Log.e("onSearchDoneEvent", "----------"+e.getRecordList());
        pd.dismiss();
        Exportlist=e.getRecordList();
        logAdpter.setList(e.getRecordList());
    }
}
