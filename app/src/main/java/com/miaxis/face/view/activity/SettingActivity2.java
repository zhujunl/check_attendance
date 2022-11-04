package com.miaxis.face.view.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.miaxis.face.R;
import com.miaxis.face.bean.Config;
import com.miaxis.face.manager.CardManager;
import com.miaxis.face.manager.ConfigManager;
import com.miaxis.face.manager.GpioManager;
import com.miaxis.face.view.custom.LogFragment;
import com.miaxis.face.view.custom.ParameterFragment;
import com.miaxis.face.view.custom.VersionFragment;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity2 extends BaseActivity {

    @BindView(R.id.log)
    TextView log;
    @BindView(R.id.version)
    TextView version;
    @BindView(R.id.parameter)
    TextView parameter;
    @BindView(R.id.set_back)
    TextView back;
    @BindView(R.id.exit)
    TextView exit;

    private Config config;
    Fragment cache;
    LogFragment logFragment=new LogFragment();
    ParameterFragment parameterFragment=new ParameterFragment();
    VersionFragment versionFragment=new VersionFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting2);
        Log.e("setting:","oncreate");
        ButterKnife.bind(this);
        GpioManager.getInstance().setSmdtStatusBar(this, false);
        config = ConfigManager.getInstance().getConfig();
        log();
    }

    public FragmentTransaction SwitchFragment(Fragment fragment, FragmentActivity fragmentActivity,int id){
        FragmentManager fragmentManager=fragmentActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        if (!fragment.isAdded()){
            if (cache!=null){
                fragmentTransaction.hide(cache);
            }
            fragmentTransaction.add(id,fragment,fragment.getClass().getName());
        }else {
            fragmentTransaction.hide(cache).show(fragment);
        }
        cache=fragment;
        return fragmentTransaction;
    }


    @OnClick(R.id.set_back)
    void back(){
        startActivity(new Intent(this,VerifyActivity.class));
        finish();
    }

    @OnClick(R.id.exit)
    void exit(){
        GpioManager.getInstance().reduction(this);
        CardManager.getInstance().closeReadCard();
        System.exit(0);
    }
    @OnClick(R.id.log)
    void log(){
        SwitchFragment(logFragment,SettingActivity2.this,R.id.fg).commit();
    }

    @OnClick(R.id.version)
    void version(){
        SwitchFragment(versionFragment,SettingActivity2.this,R.id.fg).commit();
    }

    @OnClick(R.id.parameter)
    void parameter(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        final EditText et = new EditText(this);
        et.setInputType(0x00000012);//键盘设置为密码键盘
        builder.setTitle("请输入密码")
                .setView(et)
                .setNegativeButton("取消", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton("确定", (dialog, which) -> {
                    if (et.getText().toString().trim().equals(config.getPassword())){
                        SwitchFragment(parameterFragment,SettingActivity2.this,R.id.fg).commit();
                    }else {
                        Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }).show();

    }


}