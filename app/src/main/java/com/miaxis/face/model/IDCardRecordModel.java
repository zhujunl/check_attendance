package com.miaxis.face.model;

import com.miaxis.face.app.App;
import com.miaxis.face.bean.IDCardRecord;
import com.miaxis.face.constant.Constants;
import com.miaxis.face.greendao.gen.IDCardRecordDao;
import com.miaxis.face.manager.DaoManager;
import com.miaxis.face.util.FileUtil;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
import java.util.List;

public class IDCardRecordModel {

    public static void saveIDCardRecord(IDCardRecord idCardRecord) throws Exception {
        IDCardRecordDao recordDao =DaoManager.getInstance().getDaoSession().getIDCardRecordDao();
        List<IDCardRecord> recordList=recordDao.queryBuilder().list();
        if (recordList.size()> Constants.RECORDMAX){
            List<IDCardRecord> list=recordDao.queryBuilder().offset(0).limit(recordList.size()-Constants.RECORDMAX).orderAsc(IDCardRecordDao.Properties.Id).list();
            for (IDCardRecord record:list){
                FileUtil.deleteImg(record.getFacePhotoPath());
                FileUtil.deleteImg(record.getCardPhotoPath());
            }
            recordDao.deleteInTx(list);
        }
        String cardPhotoName = idCardRecord.getCardNumber() + System.currentTimeMillis() + ".png";
        String facePhotoName = idCardRecord.getCardNumber() + "face" + System.currentTimeMillis() + ".png";
        if (idCardRecord.getCardBitmap() != null) {
            FileUtil.saveBitmap(idCardRecord.getCardBitmap(), FileUtil.getAvailableImgPath(App.getInstance()), cardPhotoName);
            idCardRecord.setCardPhotoPath(FileUtil.getAvailableImgPath(App.getInstance()) + File.separator + cardPhotoName);
        }
        if (idCardRecord.getFaceBitmap() != null) {
            FileUtil.saveBitmap(idCardRecord.getFaceBitmap(), FileUtil.getAvailableImgPath(App.getInstance()), facePhotoName);
            idCardRecord.setFacePhotoPath(FileUtil.getAvailableImgPath(App.getInstance()) + File.separator + facePhotoName);
        }
        DaoManager.getInstance().getDaoSession().getIDCardRecordDao().insert(idCardRecord);
    }

    public static void updateRecord(IDCardRecord idCardRecord) {
        DaoManager.getInstance().getDaoSession().getIDCardRecordDao().update(idCardRecord);
    }

}
