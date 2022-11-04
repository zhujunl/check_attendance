package com.miaxis.face.view.custom;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.miaxis.face.R;
import com.miaxis.face.app.App;
import com.miaxis.face.bean.Config;
import com.miaxis.face.greendao.gen.IDCardRecordDao;
import com.miaxis.face.manager.ConfigManager;
import com.miaxis.face.manager.DaoManager;
import com.miaxis.face.util.DateUtil;
import com.miaxis.face.util.FileUtil;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class LogFragment extends Fragment {

    @BindView(R.id.check_log)
    Button check_log;
    @BindView(R.id.export_log)
    Button export_log;
    @BindView(R.id.log_clear)
    Button log_clear;
    @BindView(R.id.check_start)
    TextView check_start;
    @BindView(R.id.check_end)
    TextView check_end;
    @BindView(R.id.export_start)
    TextView export_start;
    @BindView(R.id.export_end)
    TextView export_end;

    private Config config;

    private Unbinder bind;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_log,container,false);
        bind = ButterKnife.bind(this, view);
        Date date=new Date();
        check_start.setText(DateUtil.toMonthDay(date));
        check_end.setText(DateUtil.toMonthDay(date));
        export_start.setText(DateUtil.toMonthDay(date));
        export_end.setText(DateUtil.toMonthDay(date));
        config = ConfigManager.getInstance().getConfig();
        return view;
    }

    private void SetDate(TextView v){
        Calendar calendar=Calendar.getInstance();
        DatePickerDialog dialog=new DatePickerDialog(getActivity(), (view, year, month, dayOfMonth) -> {
            int mon=month+1;
            v.setText(year+"."+mon+"."+dayOfMonth);
        },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        dialog.setCancelable(false);

        dialog.show();
    }

    public void Clear(){
        IDCardRecordDao recordDao=DaoManager.getInstance().getDaoSession().getIDCardRecordDao();
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("是否删除所有日志")
                .setCancelable(false)
                .setNegativeButton("取消", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton("确定", (dialog, which) -> {
                    ProgressDialog pd=new ProgressDialog(getActivity());
                    pd.setTitle("正在删除");
                    pd.setCancelable(false);
                    pd.show();
                    Observable.create((ObservableOnSubscribe<Boolean>) result->{
                        recordDao.deleteAll();
                        result.onNext(Boolean.TRUE);
                    })
                    .subscribeOn(Schedulers.from(App.getInstance().getThreadExecutor()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result->{
                        pd.dismiss();
                        Toast.makeText(getActivity(), "删除成功", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    },throwable -> {
                        pd.dismiss();
                        Toast.makeText(getActivity(), "删除失败", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                }).show();
    }

    @OnClick(R.id.check_log)
    void CheckLog(){
        String start=check_start.getText().toString();
        String end=check_end.getText().toString();
        try {
            if(DateUtil.StringtoDate(start).getTime()>DateUtil.StringtoDate(end).getTime()){
                Toast.makeText(getActivity(), "开始时间不能大于结束时间", Toast.LENGTH_SHORT).show();
            }else {
                CheckLogFragment checkLogFragment=CheckLogFragment.newInstance(start,end);
                checkLogFragment.show(getFragmentManager(),"log");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.export_log)
    void ExportLog(){
        String start=export_start.getText().toString();
        String end=export_end.getText().toString();
        try {
            if(DateUtil.StringtoDate(start).getTime()>DateUtil.StringtoDate(end).getTime()){
                Toast.makeText(getActivity(), "开始时间不能大于结束时间", Toast.LENGTH_SHORT).show();
            }else {
                FileUtil.showFile(getActivity(),start,end,null,false);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.log_clear)
    void ClearLog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        final EditText et = new EditText(getActivity());
        et.setInputType(0x00000012);//键盘设置为密码键盘
        builder.setTitle("请输入密码")
                .setView(et)
                .setCancelable(false)
                .setNegativeButton("取消", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton("确认", (dialog, which) -> {
                    if (et.getText().toString().equals(config.getPassword())){
                        Clear();
                    }else {
                        Toast.makeText(getActivity(), "密码错误", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }).show();
    }

    @OnClick(R.id.check_start)
    void CheckStart(){
        SetDate(check_start);
    }

    @OnClick(R.id.check_end)
    void CheckEnd(){
        SetDate(check_end);
    }

    @OnClick(R.id.export_start)
    void ExporStart(){
        SetDate(export_start);
    }

    @OnClick(R.id.export_end)
    void ExportEnd(){
        SetDate(export_end);
    }


}
