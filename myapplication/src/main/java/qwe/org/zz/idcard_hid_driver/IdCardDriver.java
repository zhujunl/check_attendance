package org.zz.idcard_hid_driver;

import android.os.*;
import android.content.*;
import java.math.*;
import com.guoguang.jni.*;

public class IdCardDriver
{
    public static byte CMD_IDCARD_COMMAND;
    public static short CMD_ANTCTL_CONTROL;
    public static short CMD_READIDVER_CONTROL;
    public static short CMD_READIDMSG_CONTROL;
    public static short CMD_GETSAMID_CONTROL;
    public static short CMD_FindCARD_CONTROL;
    public static short CMD_SELECTCARD_CONTROL;
    public static short CMD_READMSG_CONTROL;
    public static short CMD_READFULLMSG_CONTROL;
    private static int IMAGE_X;
    private static int IMAGE_Y;
    private static int IMAGE_SIZE;
    private static byte CMD_GET_IMAGE;
    private static byte CMD_READ_VERSION;
    private static byte CMD_GET_HALF_IMG;
    private static final int mPhotoWidth = 102;
    private static final int mPhotoWidthBytes = 308;
    private static final int mPhotoHeight = 126;
    private static final int mPhotoSize = 38862;
    private UsbBase m_usbBase;
    private Handler m_fHandler;
    
    public void mxSetTraceLevel(final int iTraceLevel) {
        if (iTraceLevel != 0) {
            ConStant.DEBUG = true;
        }
        else {
            ConStant.DEBUG = false;
        }
    }
    
    public void SendMsg(final String obj) {
        if (ConStant.DEBUG) {
            final Message message = new Message();
            message.what = ConStant.SHOW_MSG;
            message.obj = obj;
            message.arg1 = 0;
            if (this.m_fHandler != null) {
                this.m_fHandler.sendMessage(message);
            }
        }
    }
    
    public IdCardDriver(final Context context) {
        this.m_fHandler = null;
        this.m_usbBase = new UsbBase(context);
    }
    
    public IdCardDriver(final Context context, final Handler bioHandler) {
        this.m_fHandler = null;
        this.m_fHandler = bioHandler;
        this.m_usbBase = new UsbBase(context, bioHandler);
    }
    
    public String mxGetJarVersion() {
        final String strVersion = "MIAXIS IdCard Driver V1.0.8.20170605";
        return "MIAXIS IdCard Driver V1.0.8.20170605";
    }
    
    public int mxGetDevNum() {
        return this.m_usbBase.getDevNum(ConStant.VID, ConStant.PID);
    }
    
    public int mxGetDevVersion(final byte[] bVersion) {
        int nRet = ConStant.ERRCODE_SUCCESS;
        final int[] wRecvLength = { 56 };
        nRet = this.ExeCommand(IdCardDriver.CMD_READ_VERSION, null, 0, 100, bVersion, wRecvLength, ConStant.CMD_TIMEOUT);
        return nRet;
    }
    
    public int mxGetIdCardModuleVersion(final byte[] bVersion) {
        int iRet = ConStant.ERRCODE_SUCCESS;
        iRet = this.GetIdCardModuleVersion(bVersion);
        if (iRet != 144) {
            return iRet;
        }
        return ConStant.ERRCODE_SUCCESS;
    }
    
    public int mxReadCardId(final byte[] bCardId) {
        int iRet = ConStant.ERRCODE_SUCCESS;
        iRet = this.GetIdCardNo(bCardId);
        if (iRet != 144) {
            return iRet;
        }
        iRet = this.AntControl(0);
        if (iRet != 144) {
            return iRet;
        }
        return ConStant.ERRCODE_SUCCESS;
    }
    
    String SAMIDToNum(final byte[] SAMID) {
        final byte[] szIDtemp = new byte[256];
        final byte[] temp1 = new byte[2];
        final byte[] temp2 = new byte[2];
        final byte[] temp3 = new byte[4];
        final byte[] temp4 = new byte[4];
        final byte[] temp5 = new byte[4];
        int offsize = 0;
        for (int i = 0; i < 2; ++i) {
            temp1[i] = SAMID[offsize + i];
        }
        offsize += 2;
        for (int i = 0; i < 2; ++i) {
            temp2[i] = SAMID[offsize + i];
        }
        offsize += 2;
        for (int i = 0; i < 4; ++i) {
            temp3[i] = SAMID[offsize + i];
        }
        offsize += 4;
        for (int i = 0; i < 4; ++i) {
            temp4[i] = SAMID[offsize + i];
        }
        offsize += 4;
        for (int i = 0; i < 4; ++i) {
            temp5[i] = SAMID[offsize + i];
        }
        final short sTemp1 = MXDataCode.byteArrayToShort(temp1, 0);
        final short sTemp2 = MXDataCode.byteArrayToShort(temp2, 0);
        final BigInteger sTemp3 = MXDataCode.byteArrayToBigInteger(temp3);
        final BigInteger sTemp4 = MXDataCode.byteArrayToBigInteger(temp4);
        final BigInteger sTemp5 = MXDataCode.byteArrayToBigInteger(temp5);
        return String.format("%02d%02d%08d%010d%010d", sTemp1, sTemp2, sTemp3, sTemp4, sTemp5);
    }
    
    public String mxReadSAMId() {
        final byte[] pucManaInfo = new byte[256];
        final int iRet = this.GetSAMID(pucManaInfo);
        if (iRet != 144) {
            return null;
        }
        return this.SAMIDToNum(pucManaInfo);
    }
    
    public int mxReadCardInfo(final byte[] bCardInfo) {
        this.SendMsg("========================");
        this.SendMsg("mxReadCardInfo");
        if (bCardInfo.length < 1280) {
            return ConStant.ERRCODE_MEMORY_OVER;
        }
        int iRet = ConStant.ERRCODE_SUCCESS;
        final byte[] ucCHMsg = new byte[256];
        final byte[] ucPHMsg = new byte[1024];
        final byte[] pucManaInfo = new byte[256];
        final int[] uiCHMsgLen = { 0 };
        final int[] uiPHMsgLen = { 0 };
        final byte[] bmp = new byte[38862];
        this.SendMsg("GetSAMID");
        iRet = this.GetSAMID(pucManaInfo);
        if (iRet != 144) {
            this.AntControl(0);
            return iRet;
        }
        this.SendMsg("StartFindIDCard");
        iRet = this.StartFindIDCard(pucManaInfo);
        if (iRet != 159) {
            iRet = this.StartFindIDCard(pucManaInfo);
        }
        this.SendMsg("SelectIDCard");
        iRet = this.SelectIDCard(pucManaInfo);
        if (iRet != 144) {
            return iRet;
        }
        this.SendMsg("ReadBaseMsgUnicode");
        iRet = this.ReadBaseMsgUnicode(ucCHMsg, uiCHMsgLen, ucPHMsg, uiPHMsgLen);
        if (iRet != 144) {
            this.AntControl(0);
            return iRet;
        }
        for (int i = 0; i < uiCHMsgLen[0]; ++i) {
            bCardInfo[i] = ucCHMsg[i];
        }
        for (int i = 0; i < uiPHMsgLen[0]; ++i) {
            bCardInfo[i + 256] = ucPHMsg[i];
        }
        this.SendMsg("AntControl(0)");
        this.AntControl(0);
        this.SendMsg("========================");
        return ConStant.ERRCODE_SUCCESS;
    }
    
    public int mxReadCardFullInfo(final byte[] bCardFullInfo) {
        this.SendMsg("========================");
        this.SendMsg("mxReadCardFullInfo");
        if (bCardFullInfo.length < 2304) {
            return ConStant.ERRCODE_MEMORY_OVER;
        }
        int iRet = ConStant.ERRCODE_SUCCESS;
        final byte[] ucCHMsg = new byte[256];
        final byte[] ucPHMsg = new byte[1024];
        final byte[] ucFPMsg = new byte[1024];
        final byte[] pucManaInfo = new byte[256];
        final int[] uiCHMsgLen = { 0 };
        final int[] uiPHMsgLen = { 0 };
        final int[] uiFPMsgLen = { 0 };
        final byte[] bmp = new byte[38862];
        this.SendMsg("GetSAMID");
        iRet = this.GetSAMID(pucManaInfo);
        if (iRet != 144) {
            this.AntControl(0);
            return iRet;
        }
        this.SendMsg("StartFindIDCard");
        iRet = this.StartFindIDCard(pucManaInfo);
        if (iRet != 159) {
            iRet = this.StartFindIDCard(pucManaInfo);
            if (iRet != 159) {
                return iRet;
            }
        }
        this.SendMsg("SelectIDCard");
        iRet = this.SelectIDCard(pucManaInfo);
        if (iRet != 144) {
            this.SendMsg("SelectIDCard iRet=" + iRet);
            return iRet;
        }
        this.SendMsg("ReadFullMsgUnicode");
        iRet = this.ReadFullMsgUnicode(ucCHMsg, uiCHMsgLen, ucPHMsg, uiPHMsgLen, ucFPMsg, uiFPMsgLen);
        if (iRet != 144) {
            this.SendMsg("ReadBaseMsgUnicode,iRet=" + iRet);
            this.AntControl(0);
            return ConStant.ERRCODE_ID_CARD_READ;
        }
        for (int i = 0; i < uiCHMsgLen[0]; ++i) {
            bCardFullInfo[i] = ucCHMsg[i];
        }
        for (int i = 0; i < uiPHMsgLen[0]; ++i) {
            bCardFullInfo[i + 256] = ucPHMsg[i];
        }
        for (int i = 0; i < uiFPMsgLen[0]; ++i) {
            bCardFullInfo[i + 256 + 1024] = ucFPMsg[i];
        }
        this.SendMsg("AntControl(0)");
        this.AntControl(0);
        this.SendMsg("========================");
        if (uiFPMsgLen[0] == 0) {
            return ConStant.ERRCODE_SUCCESS_1;
        }
        return ConStant.ERRCODE_SUCCESS;
    }
    
    int GetIdCardModuleVersion(final byte[] bVersion) {
        int lRV = ConStant.ERRCODE_SUCCESS;
        final byte[] oPackDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN];
        final int[] oPackLen = { oPackDataBuffer.length };
        final byte[] oRecvDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN];
        final int[] oRecvLen = { oRecvDataBuffer.length };
        final int[] result = { 0 };
        final byte[] bSendBuf = { 0 };
        lRV = this.SendIDCardPack(IdCardDriver.CMD_READIDVER_CONTROL, null, 0, oPackDataBuffer, oPackLen);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            return lRV;
        }
        lRV = this.IDCardAPDU(oPackDataBuffer, oPackLen[0], 100, oRecvDataBuffer, oRecvLen, 500);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            return lRV;
        }
        for (int i = 0; i < oPackDataBuffer.length; ++i) {
            oPackDataBuffer[i] = 0;
        }
        oPackLen[0] = oPackDataBuffer.length;
        lRV = this.RecvIDCardPack(oRecvDataBuffer, oRecvLen[0], oPackDataBuffer, oPackLen, result);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            return lRV;
        }
        if (result[0] != 144) {
            return result[0];
        }
        for (int i = 0; i < oPackLen[0]; ++i) {
            bVersion[i] = oPackDataBuffer[i];
        }
        return result[0];
    }
    
    int GetIdCardNo(final byte[] bVersion) {
        int lRV = ConStant.ERRCODE_SUCCESS;
        final byte[] oPackDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN];
        final int[] oPackLen = { oPackDataBuffer.length };
        final byte[] oRecvDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN];
        final int[] oRecvLen = { oRecvDataBuffer.length };
        final int[] result = { 0 };
        final byte[] bSendBuf = { 0 };
        lRV = this.SendIDCardPack(IdCardDriver.CMD_READIDMSG_CONTROL, null, 0, oPackDataBuffer, oPackLen);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            return lRV;
        }
        lRV = this.IDCardAPDU(oPackDataBuffer, oPackLen[0], 100, oRecvDataBuffer, oRecvLen, 500);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            return lRV;
        }
        for (int i = 0; i < oPackDataBuffer.length; ++i) {
            oPackDataBuffer[i] = 0;
        }
        oPackLen[0] = oPackDataBuffer.length;
        lRV = this.RecvIDCardPack(oRecvDataBuffer, oRecvLen[0], oPackDataBuffer, oPackLen, result);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            return lRV;
        }
        if (result[0] != 144) {
            return result[0];
        }
        for (int i = 0; i < oPackLen[0]; ++i) {
            bVersion[i] = oPackDataBuffer[i];
        }
        return result[0];
    }
    
    int GetSAMID(final byte[] bVersion) {
        int lRV = ConStant.ERRCODE_SUCCESS;
        final byte[] oPackDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN];
        final int[] oPackLen = { oPackDataBuffer.length };
        final byte[] oRecvDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN];
        final int[] oRecvLen = { oRecvDataBuffer.length };
        final int[] result = { 0 };
        final byte[] bSendBuf = { 0 };
        lRV = this.SendIDCardPack(IdCardDriver.CMD_GETSAMID_CONTROL, null, 0, oPackDataBuffer, oPackLen);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            return lRV;
        }
        lRV = this.IDCardAPDU(oPackDataBuffer, oPackLen[0], 100, oRecvDataBuffer, oRecvLen, 500);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            return lRV;
        }
        for (int i = 0; i < oPackDataBuffer.length; ++i) {
            oPackDataBuffer[i] = 0;
        }
        oPackLen[0] = oPackDataBuffer.length;
        lRV = this.RecvIDCardPack(oRecvDataBuffer, oRecvLen[0], oPackDataBuffer, oPackLen, result);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            return lRV;
        }
        if (result[0] != 144) {
            return result[0];
        }
        for (int i = 0; i < oPackLen[0]; ++i) {
            bVersion[i] = oPackDataBuffer[i];
        }
        return result[0];
    }
    
    int StartFindIDCard(final byte[] bVersion) {
        int lRV = ConStant.ERRCODE_SUCCESS;
        final byte[] oPackDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN];
        final int[] oPackLen = { oPackDataBuffer.length };
        final byte[] oRecvDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN];
        final int[] oRecvLen = { oRecvDataBuffer.length };
        final int[] result = { 0 };
        final byte[] bSendBuf = { 0 };
        lRV = this.SendIDCardPack(IdCardDriver.CMD_FindCARD_CONTROL, null, 0, oPackDataBuffer, oPackLen);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            return lRV;
        }
        lRV = this.IDCardAPDU(oPackDataBuffer, oPackLen[0], 100, oRecvDataBuffer, oRecvLen, 500);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            return lRV;
        }
        for (int i = 0; i < oPackDataBuffer.length; ++i) {
            oPackDataBuffer[i] = 0;
        }
        oPackLen[0] = oPackDataBuffer.length;
        lRV = this.RecvIDCardPack(oRecvDataBuffer, oRecvLen[0], oPackDataBuffer, oPackLen, result);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            return lRV;
        }
        if (result[0] != 144) {
            return result[0];
        }
        for (int i = 0; i < oPackLen[0]; ++i) {
            bVersion[i] = oPackDataBuffer[i];
        }
        return result[0];
    }
    
    int SelectIDCard(final byte[] bVersion) {
        int lRV = ConStant.ERRCODE_SUCCESS;
        final byte[] oPackDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN];
        final int[] oPackLen = { oPackDataBuffer.length };
        final byte[] oRecvDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN];
        final int[] oRecvLen = { oRecvDataBuffer.length };
        final int[] result = { 0 };
        final byte[] bSendBuf = { 0 };
        this.SendMsg("SendIDCardPack");
        lRV = this.SendIDCardPack(IdCardDriver.CMD_SELECTCARD_CONTROL, null, 0, oPackDataBuffer, oPackLen);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            this.SendMsg("SendIDCardPack lRV=" + lRV);
            return lRV;
        }
        this.SendMsg("IDCardAPDU");
        lRV = this.IDCardAPDU(oPackDataBuffer, oPackLen[0], 100, oRecvDataBuffer, oRecvLen, 500);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            this.SendMsg("IDCardAPDU lRV=" + lRV);
            return lRV;
        }
        for (int i = 0; i < oPackDataBuffer.length; ++i) {
            oPackDataBuffer[i] = 0;
        }
        oPackLen[0] = oPackDataBuffer.length;
        this.SendMsg("RecvIDCardPack");
        lRV = this.RecvIDCardPack(oRecvDataBuffer, oRecvLen[0], oPackDataBuffer, oPackLen, result);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            this.SendMsg("RecvIDCardPack lRV=" + lRV);
            return lRV;
        }
        if (result[0] != 144) {
            this.SendMsg("RecvIDCardPack result[0]=" + result[0]);
            return result[0];
        }
        for (int i = 0; i < oPackLen[0]; ++i) {
            bVersion[i] = oPackDataBuffer[i];
        }
        return result[0];
    }
    
    int ReadBaseMsgUnicode(final byte[] pucCHMsg, final int[] puiCHMsgLen, final byte[] PucPHMsg, final int[] puiPHMsgLen) {
        int lRV = ConStant.ERRCODE_SUCCESS;
        final byte[] oPackDataBuffer = new byte[ConStant.CMD_BUFSIZE];
        final int[] oPackLen = { oPackDataBuffer.length };
        final byte[] oRecvDataBuffer = new byte[ConStant.CMD_BUFSIZE];
        final int[] oRecvLen = { oRecvDataBuffer.length };
        final int[] result = { 0 };
        final byte[] bSendBuf = { 0 };
        lRV = this.SendIDCardPack(IdCardDriver.CMD_READMSG_CONTROL, null, 0, oPackDataBuffer, oPackLen);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            return lRV;
        }
        lRV = this.IDCardAPDU(oPackDataBuffer, oPackLen[0], 100, oRecvDataBuffer, oRecvLen, 500);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            return lRV;
        }
        for (int i = 0; i < oPackDataBuffer.length; ++i) {
            oPackDataBuffer[i] = 0;
        }
        oPackLen[0] = oPackDataBuffer.length;
        lRV = this.RecvIDCardPack(oRecvDataBuffer, oRecvLen[0], oPackDataBuffer, oPackLen, result);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            return lRV;
        }
        if (result[0] != 144) {
            return result[0];
        }
        if (oPackLen[0] != 1295) {
            return ConStant.ERRCODE_CRC;
        }
        for (int i = 0; i < 256; ++i) {
            pucCHMsg[i] = oPackDataBuffer[i + 4];
        }
        puiCHMsgLen[0] = 256;
        for (int i = 0; i < 1024; ++i) {
            PucPHMsg[i] = oPackDataBuffer[i + 4 + 256];
        }
        puiPHMsgLen[0] = 1024;
        return result[0];
    }
    
    int ReadFullMsgUnicode(final byte[] pucCHMsg, final int[] puiCHMsgLen, final byte[] PucPHMsg, final int[] puiPHMsgLen, final byte[] PucFPMsg, final int[] puiFPMsgLen) {
        int lRV = ConStant.ERRCODE_SUCCESS;
        final byte[] oPackDataBuffer = new byte[ConStant.CMD_BUFSIZE];
        final int[] oPackLen = { oPackDataBuffer.length };
        final byte[] oRecvDataBuffer = new byte[ConStant.CMD_BUFSIZE];
        final int[] oRecvLen = { oRecvDataBuffer.length };
        final int[] result = { 0 };
        final byte[] bSendBuf = { 0 };
        lRV = this.SendIDCardPack(IdCardDriver.CMD_READFULLMSG_CONTROL, null, 0, oPackDataBuffer, oPackLen);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            return lRV;
        }
        lRV = this.IDCardAPDU(oPackDataBuffer, oPackLen[0], 100, oRecvDataBuffer, oRecvLen, 500);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            return lRV;
        }
        puiFPMsgLen[0] = oRecvDataBuffer[14] * 256 + oRecvDataBuffer[13];
        this.SendMsg("puiFPMsgLen[0]=" + puiFPMsgLen[0]);
        for (int i = 0; i < oPackDataBuffer.length; ++i) {
            oPackDataBuffer[i] = 0;
        }
        oPackLen[0] = oPackDataBuffer.length;
        this.SendMsg("RecvIDCardPack");
        lRV = this.RecvIDCardPack(oRecvDataBuffer, oRecvLen[0], oPackDataBuffer, oPackLen, result);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            this.SendMsg("RecvIDCardPack lRV=" + lRV);
            return lRV;
        }
        if (result[0] != 144) {
            this.SendMsg("RecvIDCardPack result[0]=" + result[0]);
            return result[0];
        }
        for (int i = 0; i < 256; ++i) {
            pucCHMsg[i] = oPackDataBuffer[i + 4 + 2];
        }
        puiCHMsgLen[0] = 256;
        for (int i = 0; i < 1024; ++i) {
            PucPHMsg[i] = oPackDataBuffer[i + 4 + 2 + 256];
        }
        puiPHMsgLen[0] = 1024;
        for (int i = 0; i < puiFPMsgLen[0]; ++i) {
            PucFPMsg[i] = oPackDataBuffer[i + 4 + 2 + 256 + 1024];
        }
        return result[0];
    }
    
    int AntControl(final int dAntState) {
        int lRV = ConStant.ERRCODE_SUCCESS;
        final byte[] oPackDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN];
        final int[] oPackLen = { oPackDataBuffer.length };
        final byte[] oRecvDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN];
        final int[] oRecvLen = { oRecvDataBuffer.length };
        final int[] result = { 0 };
        final byte[] bSendBuf = { (byte)dAntState };
        lRV = this.SendIDCardPack(IdCardDriver.CMD_ANTCTL_CONTROL, bSendBuf, 1, oPackDataBuffer, oPackLen);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            return lRV;
        }
        lRV = this.IDCardAPDU(oPackDataBuffer, oPackLen[0], 100, oRecvDataBuffer, oRecvLen, 500);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            return lRV;
        }
        for (int i = 0; i < oPackDataBuffer.length; ++i) {
            oPackDataBuffer[i] = 0;
        }
        oPackLen[0] = oPackDataBuffer.length;
        lRV = this.RecvIDCardPack(oRecvDataBuffer, oRecvLen[0], oPackDataBuffer, oPackLen, result);
        if (lRV != ConStant.ERRCODE_SUCCESS) {
            return lRV;
        }
        return result[0];
    }
    
    int SendIDCardPack(final short IDCardCommandIDAndIDCardparam, final byte[] SendDataBuffer, final int SendLen, final byte[] oPackDataBuffer, final int[] oPackLen) {
        final byte[] tempBufferData = new byte[ConStant.DATA_BUFFER_SIZE_MIN];
        int i = 0;
        int offsize = 0;
        byte AddCheck = 0;
        short len = 0;
        final byte[] FlagStart = new byte[5];
        final byte[] dtemp = new byte[2];
        FlagStart[0] = -86;
        FlagStart[2] = (FlagStart[1] = -86);
        FlagStart[3] = -106;
        FlagStart[4] = 105;
        dtemp[1] = (dtemp[0] = 0);
        if (SendLen > ConStant.DATA_BUFFER_SIZE_MIN - 10 || SendLen < 0) {
            return ConStant.ERRCODE_MEMORY_OVER;
        }
        for (i = 0; i < FlagStart.length; ++i) {
            tempBufferData[offsize + i] = FlagStart[i];
        }
        offsize += FlagStart.length;
        len = (short)(2 + SendLen + 1);
        dtemp[0] = (byte)((byte)(len / 256) & 0xFF);
        dtemp[1] = (byte)((byte)len & 0xFF);
        for (i = 0; i < dtemp.length; ++i) {
            tempBufferData[offsize + i] = dtemp[i];
        }
        offsize += dtemp.length;
        for (i = 0; i < dtemp.length; ++i) {
            dtemp[i] = 0;
        }
        dtemp[0] = (byte)((byte)(IDCardCommandIDAndIDCardparam >> 8) & 0xFF);
        dtemp[1] = (byte)((byte)IDCardCommandIDAndIDCardparam & 0xFF);
        for (i = 0; i < dtemp.length; ++i) {
            tempBufferData[offsize + i] = dtemp[i];
        }
        offsize += dtemp.length;
        if (SendLen > 0 && SendLen < ConStant.DATA_BUFFER_SIZE_MIN - 10) {
            for (i = 0; i < SendLen; ++i) {
                tempBufferData[offsize + i] = SendDataBuffer[i];
            }
            offsize += SendLen;
        }
        for (i = 0; i < len + 2; ++i) {
            AddCheck ^= tempBufferData[i + 5];
        }
        tempBufferData[offsize] = AddCheck;
        ++offsize;
        if (oPackLen[0] < offsize) {
            return ConStant.ERRCODE_MEMORY_OVER;
        }
        oPackLen[0] = (short)offsize;
        for (i = 0; i < offsize; ++i) {
            oPackDataBuffer[i] = tempBufferData[i];
        }
        return ConStant.ERRCODE_SUCCESS;
    }
    
    int RecvIDCardPack(final byte[] RecvDataBuffer, final int RecvLen, final byte[] oPackDataBuffer, final int[] oPackLen, final int[] oResult) {
        final byte[] tempBufferData = new byte[ConStant.CMD_BUFSIZE];
        int offsize = 0;
        short len = 0;
        byte dresult = -1;
        byte recvCheck = 0;
        byte currentCheck = 0;
        final byte[] FlagStart = new byte[5];
        final byte[] dtemp = new byte[2];
        final byte[] Reser = new byte[2];
        FlagStart[0] = -86;
        FlagStart[2] = (FlagStart[1] = -86);
        FlagStart[3] = -106;
        FlagStart[4] = 105;
        dtemp[1] = (dtemp[0] = 0);
        Reser[1] = (Reser[0] = 0);
        for (int i = 0; i < FlagStart.length; ++i) {
            if (RecvDataBuffer[i] != FlagStart[i]) {
                return ConStant.ERRCODE_CRC;
            }
        }
        offsize += 5;
        len = (short)(256 * RecvDataBuffer[offsize] + RecvDataBuffer[offsize + 1]);
        offsize += 2;
        Reser[0] = RecvDataBuffer[offsize];
        Reser[1] = RecvDataBuffer[offsize + 1];
        for (int i = 0; i < Reser.length; ++i) {
            if (Reser[i] != 0) {
                return ConStant.ERRCODE_CRC;
            }
        }
        offsize += 2;
        dresult = RecvDataBuffer[offsize];
        ++offsize;
        if (len > 4) {
            for (int i = 0; i < len - 4; ++i) {
                tempBufferData[i] = RecvDataBuffer[offsize + i];
            }
            offsize = offsize + len - 4;
        }
        recvCheck = RecvDataBuffer[offsize];
        for (int i = 0; i < len + 2 - 1; ++i) {
            currentCheck ^= RecvDataBuffer[i + 5];
        }
        ++offsize;
        if (currentCheck != recvCheck) {
            return ConStant.ERRCODE_CRC;
        }
        if (oPackDataBuffer != null && oPackLen[0] > len - 4) {
            oPackLen[0] = (short)offsize;
            for (int i = 0; i < len - 4; ++i) {
                oPackDataBuffer[i] = tempBufferData[i];
            }
            if ((oResult[0] = dresult) < 0) {
                oResult[0] = dresult + 256;
            }
            return ConStant.ERRCODE_SUCCESS;
        }
        return ConStant.ERRCODE_MEMORY_OVER;
    }
    
    int IDCardAPDU(final byte[] lpSendData, final int wSendLength, final int iSendTime, final byte[] lpRecvData, final int[] io_wRecvLength, final int iRecvTime) {
        int lRV = ConStant.ERRCODE_SUCCESS;
        lRV = this.ExeCommand(IdCardDriver.CMD_IDCARD_COMMAND, lpSendData, wSendLength, iSendTime, lpRecvData, io_wRecvLength, iRecvTime);
        return lRV;
    }
    
    int ExeCommand(final byte nCommandID, final byte[] lpSendData, final int wSendLength, final int iSendTime, final byte[] lpRecvData, final int[] io_wRecvLength, final int iRecvTime) {
        final int iMaxRecvLen = io_wRecvLength[0];
        this.SendMsg("nCommandID:" + nCommandID);
        int iRet = ConStant.ERRCODE_SUCCESS;
        iRet = this.m_usbBase.openDev(ConStant.VID, ConStant.PID);
        if (iRet != 0) {
            return iRet;
        }
        final byte[] DataBuffer = new byte[ConStant.CMD_DATA_BUF_SIZE];
        do {
            iRet = this.m_usbBase.recvData(DataBuffer, DataBuffer.length, 5);
        } while (iRet == 0);
        iRet = this.sendPacket(nCommandID, lpSendData, wSendLength);
        if (iRet != 0) {
            this.m_usbBase.closeDev();
            return iRet;
        }
        final byte[] bResult = { 0 };
        final byte[] bRecvBuf = new byte[ConStant.CMD_DATA_BUF_SIZE];
        iRet = this.recvPacket(bResult, bRecvBuf, io_wRecvLength, ConStant.CMD_TIMEOUT);
        if (iRet != 0) {
            this.m_usbBase.closeDev();
            return iRet;
        }
        int len = 0;
        len = bRecvBuf[7] * 256 + bRecvBuf[8] + 7;
        this.SendMsg("len=" + len);
        int packsize = len / ConStant.REVC_BUFFER_SIZE_MIN;
        if (len % ConStant.REVC_BUFFER_SIZE_MIN != 0) {
            ++packsize;
        }
        this.SendMsg("packsize=" + packsize);
        final byte[] outBuffer = new byte[ConStant.CMD_BUFSIZE];
        int realsize = 0;
        this.SendMsg("io_wRecvLength[0]=" + io_wRecvLength[0]);
        if (io_wRecvLength[0] >= 2) {
            for (int i = 2; i < io_wRecvLength[0]; ++i) {
                outBuffer[i - 2 + realsize] = bRecvBuf[i];
            }
            realsize = realsize + io_wRecvLength[0] - 2;
        }
        else {
            realsize = realsize;
        }
        this.SendMsg("realsize=" + realsize);
        for (int k = 1; k < packsize; ++k) {
            iRet = this.recvPacket(bResult, bRecvBuf, io_wRecvLength, ConStant.CMD_TIMEOUT);
            if (iRet != 0) {
                this.m_usbBase.closeDev();
                return iRet;
            }
            if (io_wRecvLength[0] >= 2) {
                for (int j = 2; j < io_wRecvLength[0]; ++j) {
                    outBuffer[j - 2 + realsize] = bRecvBuf[j];
                }
                realsize = realsize + io_wRecvLength[0] - 2;
            }
            else {
                realsize = realsize;
            }
        }
        this.SendMsg("====realsize=" + realsize);
        this.SendMsg("====iMaxRecvLen=" + iMaxRecvLen);
        if (realsize > iMaxRecvLen) {
            this.m_usbBase.closeDev();
            return ConStant.ERRCODE_MEMORY_OVER;
        }
        if (realsize >= 2) {
            for (int i = 0; i < realsize; ++i) {
                lpRecvData[i] = outBuffer[i];
            }
            io_wRecvLength[0] = realsize;
        }
        this.m_usbBase.closeDev();
        return ConStant.ERRCODE_SUCCESS;
    }
    
    private int sendPacket(final byte bCmd, final byte[] bSendBuf, final int iDataLen) {
        int iRet = -1;
        int offsize = 0;
        short iCheckSum = 0;
        final byte[] DataBuffer = new byte[ConStant.CMD_DATA_BUF_SIZE];
        DataBuffer[offsize++] = ConStant.CMD_REQ_FLAG;
        DataBuffer[offsize++] = 0;
        DataBuffer[offsize++] = 0;
        DataBuffer[offsize++] = (byte)(iDataLen + 1 & 0xFF);
        DataBuffer[offsize++] = (byte)(iDataLen + 1 >> 8);
        DataBuffer[offsize++] = bCmd;
        if (iDataLen > 1) {
            for (int i = 0; i < iDataLen; ++i) {
                DataBuffer[offsize++] = bSendBuf[i];
            }
        }
        for (int j = 3; j < offsize; ++j) {
            short tmp = DataBuffer[j];
            if (tmp < 0) {
                tmp += 256;
            }
            iCheckSum += tmp;
        }
        if (iCheckSum < 0) {
            iCheckSum += 256;
        }
        DataBuffer[offsize++] = (byte)(iCheckSum & 0xFF);
        DataBuffer[offsize++] = (byte)((byte)(iCheckSum >> 8) & 0xFF);
        iRet = this.m_usbBase.sendData(DataBuffer, DataBuffer.length, ConStant.CMD_TIMEOUT);
        if (iRet < 0) {
            return -1;
        }
        return 0;
    }
    
    private int recvPacket(final byte[] bResult, final byte[] bRecvBuf, final int[] iRecvLen, final int iTimeOut) {
        int iRet = -1;
        int offsize = 0;
        int iDataLen = 0;
        int a = 0;
        int b = 0;
        final byte[] DataBuffer = new byte[ConStant.CMD_DATA_BUF_SIZE];
        final byte[] SRN = new byte[2];
        short recvCheckSum = 0;
        short currentCheckSum = 0;
        iRet = this.m_usbBase.recvData(DataBuffer, DataBuffer.length, iTimeOut);
        if (iRet < 0) {
            return iRet;
        }
        if (DataBuffer[offsize++] != ConStant.CMD_RET_FLAG) {
            return ConStant.ERRCODE_CRC;
        }
        SRN[0] = DataBuffer[offsize++];
        SRN[1] = DataBuffer[offsize++];
        a = DataBuffer[offsize++];
        if (a < 0) {
            a += 256;
        }
        b = DataBuffer[offsize++];
        if (b < 0) {
            b += 256;
        }
        iDataLen = b * 256 + a;
        if (iDataLen > ConStant.CMD_DATA_BUF_SIZE - 5) {
            return ConStant.ERRCODE_CRC;
        }
        bResult[0] = DataBuffer[offsize];
        if (iDataLen - 1 > 0) {
            for (int i = 1; i < iDataLen; ++i) {
                bRecvBuf[i - 1] = DataBuffer[offsize + i];
            }
        }
        iRecvLen[0] = iDataLen - 1;
        offsize += iDataLen;
        for (int i = 3; i < offsize; ++i) {
            a = DataBuffer[i];
            if (a < 0) {
                a += 256;
            }
            currentCheckSum += (short)a;
        }
        a = DataBuffer[offsize++];
        if (a < 0) {
            a += 256;
        }
        b = DataBuffer[offsize++];
        if (b < 0) {
            b += 256;
        }
        recvCheckSum = (short)(b * 256 + a);
        if (currentCheckSum != recvCheckSum) {
            return ConStant.ERRCODE_CRC;
        }
        return ConStant.ERRCODE_SUCCESS;
    }
    
    public int Wlt2Bmp(final byte[] wlt, final byte[] bmp) {
        if (bmp.length < 38862) {
            return ConStant.ERRCODE_MEMORY_OVER;
        }
        JniCall.Huaxu_Wlt2Bmp(wlt, bmp, 0);
        return 0;
    }
    
    public int Base64Encode(final byte[] pInput, final int inputLen, final byte[] pOutput, final int outputbufsize) {
        return zzJavaBase64.JavaBase64Encode(pInput, inputLen, pOutput, outputbufsize);
    }
    
    static {
        IdCardDriver.CMD_IDCARD_COMMAND = -79;
        IdCardDriver.CMD_ANTCTL_CONTROL = -1519;
        IdCardDriver.CMD_READIDVER_CONTROL = -1296;
        IdCardDriver.CMD_READIDMSG_CONTROL = -1390;
        IdCardDriver.CMD_GETSAMID_CONTROL = 4863;
        IdCardDriver.CMD_FindCARD_CONTROL = 8193;
        IdCardDriver.CMD_SELECTCARD_CONTROL = 8194;
        IdCardDriver.CMD_READMSG_CONTROL = 12289;
        IdCardDriver.CMD_READFULLMSG_CONTROL = 12304;
        IdCardDriver.IMAGE_X = 256;
        IdCardDriver.IMAGE_Y = 360;
        IdCardDriver.IMAGE_SIZE = IdCardDriver.IMAGE_X * IdCardDriver.IMAGE_Y;
        IdCardDriver.CMD_GET_IMAGE = 10;
        IdCardDriver.CMD_READ_VERSION = 13;
        IdCardDriver.CMD_GET_HALF_IMG = 20;
    }
}
