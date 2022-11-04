package com.miaxis.face.util;

import android.app.ProgressDialog;
import android.app.smdt.SmdtManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.miaxis.face.R;
import com.miaxis.face.adapter.FileAdapter;
import com.miaxis.face.app.App;
import com.miaxis.face.bean.IDCardRecord;
import com.miaxis.face.bean.Record;
import com.miaxis.face.constant.Constants;
import com.miaxis.face.greendao.gen.IDCardRecordDao;
import com.miaxis.face.manager.DaoManager;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2017/5/21 0021.
 */

public class FileUtil {

    public static final String FACE_MAIN_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "miaxis" + File.separator + "Face_check";
    private static final String LICENCE_NAME = "st_lic.txt";
    private static final String IMG_PATH_NAME = "zzFaces";
    private static final String WLT_PATH_NAME = "wlt";
    private static final String MODEL_PATH_NAME = "zzFaceModel";
    private static final String ADVERTISEMENT_FILE_PATH_NAME = "adFile";
    private static final String ADVERTISEMENT_CACHE = "cache";

    public static void initDirectory(Context context) {
        File modelDir = new File(FileUtil.getFaceModelPath());
        if (!modelDir.exists()) {
            modelDir.mkdirs();
        }
        File zzFacesDir = new File(FileUtil.getAvailableImgPath(context));
        if (!zzFacesDir.exists()) {
            zzFacesDir.mkdirs();
        }
        File adFileDir = new File(FileUtil.getAdvertisementFilePath());
        if (!adFileDir.exists()) {
            adFileDir.mkdirs();
        }
        File wltlibDir = new File(FileUtil.getAvailableWltPath(context));
        if (!wltlibDir.exists()) {
            wltlibDir.mkdirs();
        }
        File cacheDir = new File(FileUtil.getAdvertisementCachePath());
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    public static String readLicence() {
        File lic = new File(FACE_MAIN_PATH, LICENCE_NAME);
        return readFileToString(lic);
    }

    public static String readFileToString(File file) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String readLine;
            while ((readLine = br.readLine()) != null) {
                sb.append(readLine);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static byte[] readFileToBytes(String filePath) throws IOException {
        File f = new File(filePath);
        if (!f.exists()) {
            throw new FileNotFoundException("file not exists");
        }
        BufferedInputStream in = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream((int) f.length())) {
            in = new BufferedInputStream(new FileInputStream(f));
            int buf_size = 1024;
            byte[] buffer = new byte[buf_size];
            int len;
            while (-1 != (len = in.read(buffer, 0, buf_size))) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static void writeFile(File file, String content, boolean isAdd) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file, isAdd));
            bw.write(content);
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("FileUtil", "writeFile exception " + e.getMessage());
        } finally {
            try {
                if(bw != null) {
                    bw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String UsbPath() {
        String filePath = "/proc/mounts";
        File file = new File(filePath);
        List<String> lineList = new ArrayList<>();
        InputStream inputStream =null;
        try {
            inputStream = new FileInputStream(file);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "GBK");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("vfat")) {
                        lineList.add(line);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(lineList.size()>1){
            String editPath = lineList.get(lineList.size() - 1);
            Log.e("editPath:",editPath);
            int start = editPath.indexOf("/mnt");
            int end = editPath.indexOf(" vfat");
            String path = editPath.substring(start, end);
            Log.d("SelectBusLineDialog", "path: " + path);
            return path;
        }
        return null;

    }

    public static void writeFile(InputStream is, File file) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            byte[] b = new byte[1024];
            while ((is.read(b)) != -1) {
                fos.write(b);
            }
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void delDirectory(File file) {
        File[] files = file.listFiles();
        for (File file1 : files) {
            if (file1.isFile()) {
                File photoFile = new File(file1.getPath());
                if (photoFile.isDirectory()) {
                    delDirectory(photoFile);
                } else {
                    photoFile.delete();
                }
            }
        }
        file.delete();
    }

    public static String getAvailablePath(Context context) {
        File saveDir = new File(new SmdtManager(context).smdtGetSDcardPath(context));
        if (!saveDir.exists() || !saveDir.canWrite()) {
            return FACE_MAIN_PATH;
        } else {
            return saveDir.getPath();
        }
    }

    public static String getAvailableFeaturePath(Context context) {
        String path = getAvailablePath(context) + File.separator + "localFeature";
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }
        return path;
    }

    public static int getAvailablePathType(Context context) {
        File saveDir = new File(new SmdtManager(context).smdtGetSDcardPath(context));
        if (!saveDir.exists() || !saveDir.canWrite()) {
            return Constants.PATH_LOCAL;
        } else {
            return Constants.PATH_TF_CARD;
        }
    }

    public static String getFaceModelPath() {
        return FACE_MAIN_PATH + File.separator + MODEL_PATH_NAME;
    }

    public static String getAdvertisementFilePath() {
        return FACE_MAIN_PATH + File.separator + ADVERTISEMENT_FILE_PATH_NAME;
    }

    public static String getAvailableImgPath(Context context) {
        return getAvailablePath(context) + File.separator + IMG_PATH_NAME;
    }

    public static String getAvailableWltPath(Context context) {
        return getAvailablePath(context) + File.separator + WLT_PATH_NAME;
    }

    public static String getAdvertisementCachePath() {
        return FACE_MAIN_PATH + File.separator + ADVERTISEMENT_CACHE;
    }

    public static void saveBitmap(Bitmap bitmap, String path, String name) throws Exception {
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }
        File file = new File(path, name);
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.flush();
        out.close();
    }

    public static void writeBytesToFile(byte[] bfile, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if(!dir.exists() || !dir.isDirectory()){//判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(filePath, fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static long getSDFreeSize(String path) {
        File file = new File(path);
        //取得SD卡文件路径
        StatFs sf = new StatFs(file.getPath());
        //获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        //空闲的数据块的数量
        long freeBlocks = sf.getAvailableBlocks();
        //返回SD卡空闲大小
        //return freeBlocks * blockSize;  //单位Byte
        //return (freeBlocks * blockSize)/1024;   //单位KB
        return (freeBlocks * blockSize)/1024 /1024; //单位MB
    }

    public static long getSDAllSize(String path) {
        File file = new File(path);
        StatFs sf = new StatFs(file.getPath());
        //获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        //获取所有数据块数
        long allBlocks = sf.getBlockCount();
        //返回SD卡大小
        //return allBlocks * blockSize; //单位Byte
        //return (allBlocks * blockSize)/1024; //单位KB
        return (allBlocks * blockSize)/1024/1024; //单位MB
    }

    public static void deleteImg(String path) {
        File f = new File(path);
        if (!f.delete()) {
            LogUtil.writeLog("删除失败" + path);
        }
    }

    public static void saveRecordImg(Record record, Context context) {
        byte[] cardImgBytes = record.getCardImgData();
        if (cardImgBytes != null && cardImgBytes.length > 0) {
            File cardImg = new File(getAvailableImgPath(context), record.getCardNo() + "_" + record.getName() + ".jpg");
            if (!cardImg.exists()) {
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(cardImg);
                    fos.write(cardImgBytes);
                    fos.flush();
                    record.setCardImg(cardImg.getPath());
                } catch (IOException e) {
                    LogUtil.writeLog("saveRecordImg" + e.getMessage());
                } finally {
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                        LogUtil.writeLog("saveRecordImg" + e.getMessage());
                    }
                }
            } else {
                record.setCardImg(cardImg.getPath());
            }
        }

        byte[] faceImgBytes = record.getFaceImgData();
        if (faceImgBytes != null && faceImgBytes.length > 0) {
            File faceImg = new File(getAvailableImgPath(context), record.getCardNo() + "_" + record.getName() + "_" + System.currentTimeMillis() + ".jpg");
            if (faceImg.exists()) {
                faceImg.delete();
            }
            FileOutputStream fos2 = null;
            try {
                fos2 = new FileOutputStream(faceImg);
                fos2.write(faceImgBytes);
                fos2.flush();
                record.setFaceImg(faceImg.getPath());
            } catch (IOException e) {
                LogUtil.writeLog("saveRecordImg" + e.getMessage());
            } finally {
                try {
                    if (fos2 != null)
                        fos2.close();
                } catch (IOException e) {
                    LogUtil.writeLog("saveRecordImg" + e.getMessage());
                }
            }
        }
    }

    public static String copyImg(Record record) {
        FileOutputStream fos = null;
        FileInputStream fis = null;
        String newPath = record.getFaceImg().replace(record.getName(), "测试数据" + System.currentTimeMillis());
        try {
            fis = new FileInputStream(new File(record.getFaceImg()));
            fos = new FileOutputStream(new File(newPath));
            byte[] b = new byte[1024];
            while ((fis.read(b)) != -1) {
                fos.write(b);
            }
            fos.flush();
        } catch (Exception e) {

        } finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return newPath;
        }
    }

    public static String pathToBase64(String path) {
        try {
            byte[] bytes = toByteArray(path);
            String str = Base64.encodeToString(bytes, Base64.DEFAULT);
            bytes = null;
            System.gc();
            return str;
        } catch (Exception e) {
            LogUtil.writeLog("pathToBase64" + e.getMessage());
            return "";
        }

    }

    public static byte[] toByteArray(String filename) throws Exception {

        File f = new File(filename);
        if (!f.exists()) {
            throw new FileNotFoundException(filename);
        }

        BufferedInputStream in = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream((int) f.length())) {
            in = new BufferedInputStream(new FileInputStream(f));
            int buf_size = 1024;
            byte[] buffer = new byte[buf_size];
            int len = 0;
            while (-1 != (len = in.read(buffer, 0, buf_size))) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static void copyAssetsFile(Context context, String fileSrc, String fileDst) {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = context.getAssets().open(fileSrc);
            File file = new File(fileDst);
            os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = is.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null ) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String readFromUSBPath(Context context, String fileName) {
        String usbPath = getUSBPath(context);
        File file = new File(usbPath, fileName);
        if (file.exists()) {
            return readFileToString(file);
        } else {
            return null;
        }
    }

    public static String getUSBPath(Context context) {
        SmdtManager smdtManager = SmdtManager.create(context);
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            return smdtManager.smdtGetUSBPath(context, 1);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            return smdtManager.smdtGetUSBPath(context, 2);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {
            return smdtManager.smdtGetUSBPath(context, 2);
        } else {
            return null;
        }
    }

    public static File searchFileFromU(Context context, String fileName) {
        String usbPath = getUSBPath(context);
        if (usbPath == null) {
            return null;
        }
        File usbDir = new File(usbPath);
        if (!usbDir.exists()) {
            usbDir = usbDir.getParentFile();
        }
        if (!usbDir.isDirectory()) {
            return null;
        }
        File[] usbFiles = usbDir.listFiles();
        if (usbFiles == null) {
            return null;
        }
        for (File file : usbFiles) {
            if (file.getName().equals(fileName)) {
                return file;
            }
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files == null) {
                    continue;
                }
                for (File file1 : files) {
                    if (file1.getName().equals(fileName)) {
                        return file1;
                    }
                }
            }
        }
        return null;
    }

    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.NO_WRAP);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    private static final String Filepath= "/storage/emulated";

    public static void showFile(Context context,String start, String end, List<IDCardRecord> Exportlist,boolean check){
        List<String> list=new ArrayList<>();
        View view= LayoutInflater.from(context).inflate(R.layout.dialog_export,null,false);
        RecyclerView rv=view.findViewById(R.id.fileRv);
        Button cancle=view.findViewById(R.id.cancle);
        Button sure=view.findViewById(R.id.sure);
        TextView back=view.findViewById(R.id.back);
        TextView path=view.findViewById(R.id.path);
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setCancelable(false);
        AlertDialog alertDialog=builder.create();
        File fileRoot = new File(Filepath);
        File[] files=fileRoot.listFiles();
        for (File f:files){
            if (f.isDirectory())
                list.add(f.getPath());
        }
        String usbpath=FileUtil.UsbPath();
        if (usbpath!=null){
            File f=new File(usbpath);
            list.add(f.getPath());
        }
        FileAdapter fileAdapter=new FileAdapter(list,context);
        sure.setOnClickListener(v->{
            if (!TextUtils.isEmpty(path.getText().toString().trim())){
                alertDialog.dismiss();
                ProgressDialog progressDialog=new ProgressDialog(context);
                progressDialog.setTitle("保存中");
                progressDialog.setCancelable(false);
                progressDialog.show();
                Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
                    boolean excle=false;
                    if(check) {
                        excle = ExcelUtils.writeTagToExcel(path.getText().toString().trim() + "/" + start + "-" + end, Exportlist);
                    }else {
                        IDCardRecordDao recordDao = DaoManager.getInstance().getDaoSession().getIDCardRecordDao();
                        QueryBuilder<IDCardRecord> queryBuilder = recordDao.queryBuilder();
                        queryBuilder.where(IDCardRecordDao.Properties.VerifyTime.ge(DateUtil.StringtoDate(start)));
                        queryBuilder.where(IDCardRecordDao.Properties.VerifyTime.le(DateUtil.addOneDay(DateUtil.StringtoDate(end))));
                        List<IDCardRecord> export=queryBuilder.orderDesc(IDCardRecordDao.Properties.Id).list();
                        excle = ExcelUtils.writeTagToExcel(path.getText().toString().trim() + "/" + start + "-" + end, export);
                    }
                    emitter.onNext(excle);
                })
                        .subscribeOn(Schedulers.from(App.getInstance().getThreadExecutor()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(flag -> {
                            progressDialog.dismiss();
                            if (flag){
                                Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show();
                            }
                        }, throwable -> {
                            progressDialog.dismiss();
                            Toast.makeText(context, "保存失败"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
        cancle.setOnClickListener(v->{
            list.clear();
            alertDialog.dismiss();});
        back.setOnClickListener(v -> {
            list.clear();
            if(TextUtils.isEmpty(path.getText().toString())){
                alertDialog.dismiss();
                return;
            }
            list.clear();
            String usb=FileUtil.UsbPath();
            File file=new File(path.getText().toString());
            File f=new File(file.getParent());
            if (f.getPath().equals(Filepath)||(usb!=null&&file.getPath().equals(usb))){
                path.setText("");
                File fileR = new File(Filepath);
                File[] fils=fileR.listFiles();
                for (File fs:fils){
                    if (fs.isDirectory())
                        list.add(fs.getPath());
                }
                if (usb!=null){
                    File fff=new File(usb);
                    list.add(fff.getPath());
                }
            }else {
                path.setText(f.getPath());
                for (File ff:f.listFiles()){
                    if(ff.isDirectory()){
                        list.add(ff.getPath());
                    }
                }
            }
            fileAdapter.setList(list);
        });
        FileAdapter.AdaperListener listener= name -> {
            try {
                path.setText(name);
                File file=new File(name);
                list.clear();
                for (File f1 :file.listFiles()){
                    if (f1.isDirectory())
                        list.add(f1.getPath());
                }
                fileAdapter.setList(list);
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(context, "暂无该文件夹", Toast.LENGTH_SHORT).show();
            }
        };
        fileAdapter.setListener(listener);
        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.setAdapter(fileAdapter);
        alertDialog.show();
        WindowManager.LayoutParams params =
                alertDialog.getWindow().getAttributes();
        params.width = 800;
        params.height =  WindowManager.LayoutParams.WRAP_CONTENT;
        alertDialog.getWindow().setAttributes(params);
    }
}
