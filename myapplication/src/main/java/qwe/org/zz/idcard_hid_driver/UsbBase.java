package org.zz.idcard_hid_driver;

import android.os.*;
import android.util.*;
import android.hardware.usb.*;
import android.app.*;
import java.util.*;
import android.content.*;

public class UsbBase
{
    public static final int ERRCODE_SUCCESS = 0;
    public static final int ERRCODE_NODEVICE = -100;
    public static final int ERRCODE_MEMORY_OVER = -101;
    public static final int ERRCODE_NO_PERMISION = -1000;
    public static final int ERRCODE_NO_CONTEXT = -1001;
    public static final int SHOW_MSG = 255;
    private int m_iSendPackageSize;
    private int m_iRecvPackageSize;
    private UsbDevice m_usbDevice;
    private UsbInterface m_usbInterface;
    private UsbEndpoint m_inEndpoint;
    private UsbEndpoint m_outEndpoint;
    private UsbDeviceConnection m_connection;
    private Context m_ctx;
    private Handler m_fHandler;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver mUsbReceiver;
    
    public void SendMsg(final String obj) {
        if (org.zz.idcard_hid_driver.ConStant.DEBUG) {
            final Message message = new Message();
            message.what = org.zz.idcard_hid_driver.ConStant.SHOW_MSG;
            message.obj = obj;
            message.arg1 = 0;
            if (this.m_fHandler != null) {
                this.m_fHandler.sendMessage(message);
            }
        }
    }
    
    public UsbBase(final Context context) {
        this.m_iSendPackageSize = 0;
        this.m_iRecvPackageSize = 0;
        this.m_usbDevice = null;
        this.m_usbInterface = null;
        this.m_inEndpoint = null;
        this.m_outEndpoint = null;
        this.m_connection = null;
        this.m_ctx = null;
        this.m_fHandler = null;
        this.mUsbReceiver = new BroadcastReceiver() {
            public void onReceive(final Context context, final Intent intent) {
                final String action = intent.getAction();
                if ("com.android.example.USB_PERMISSION".equals(action)) {
                    synchronized (this) {
                        final UsbDevice device = (UsbDevice)intent.getParcelableExtra("device");
                        if (!intent.getBooleanExtra("permission", false)) {
                            Log.d("MIAXIS", "permission denied for device " + device);
                        }
                    }
                }
                if ("android.hardware.usb.action.USB_DEVICE_DETACHED".equals(action)) {
                    final UsbDevice device2 = (UsbDevice)intent.getParcelableExtra("device");
                    if (device2 != null) {
                        UsbBase.this.m_connection.releaseInterface(UsbBase.this.m_usbInterface);
                        UsbBase.this.m_connection.close();
                    }
                }
            }
        };
        this.m_ctx = context;
        this.m_fHandler = null;
        this.regUsbMonitor();
    }
    
    public UsbBase(final Context context, final Handler bioHandler) {
        this.m_iSendPackageSize = 0;
        this.m_iRecvPackageSize = 0;
        this.m_usbDevice = null;
        this.m_usbInterface = null;
        this.m_inEndpoint = null;
        this.m_outEndpoint = null;
        this.m_connection = null;
        this.m_ctx = null;
        this.m_fHandler = null;
        this.mUsbReceiver = new BroadcastReceiver() {
            public void onReceive(final Context context, final Intent intent) {
                final String action = intent.getAction();
                if ("com.android.example.USB_PERMISSION".equals(action)) {
                    synchronized (this) {
                        final UsbDevice device = (UsbDevice)intent.getParcelableExtra("device");
                        if (!intent.getBooleanExtra("permission", false)) {
                            Log.d("MIAXIS", "permission denied for device " + device);
                        }
                    }
                }
                if ("android.hardware.usb.action.USB_DEVICE_DETACHED".equals(action)) {
                    final UsbDevice device2 = (UsbDevice)intent.getParcelableExtra("device");
                    if (device2 != null) {
                        UsbBase.this.m_connection.releaseInterface(UsbBase.this.m_usbInterface);
                        UsbBase.this.m_connection.close();
                    }
                }
            }
        };
        this.m_ctx = context;
        this.m_fHandler = bioHandler;
        this.regUsbMonitor();
    }
    
    public int getDevNum(final int vid, final int pid) {
        if (this.m_ctx == null) {
            return -1001;
        }
        int iDevNum = 0;
        final UsbManager usbManager = (UsbManager)this.m_ctx.getSystemService("usb");
        final HashMap<String, UsbDevice> map = (HashMap<String, UsbDevice>)usbManager.getDeviceList();
        for (final UsbDevice device : map.values()) {
            if (!usbManager.hasPermission(device)) {
                final PendingIntent pi = PendingIntent.getBroadcast(this.m_ctx, 0, new Intent("com.android.example.USB_PERMISSION"), 0);
                usbManager.requestPermission(device, pi);
                return -1000;
            }
            if (vid != device.getVendorId()) {
                continue;
            }
            if (pid != device.getProductId()) {
                continue;
            }
            ++iDevNum;
        }
        return iDevNum;
    }
    
    public int openDev(final int vid, final int pid) {
        if (this.m_ctx == null) {
            return -1001;
        }
        final UsbManager usbManager = (UsbManager)this.m_ctx.getSystemService("usb");
        final HashMap<String, UsbDevice> map = (HashMap<String, UsbDevice>)usbManager.getDeviceList();
        for (final UsbDevice device : map.values()) {
            if (!usbManager.hasPermission(device)) {
                final PendingIntent pi = PendingIntent.getBroadcast(this.m_ctx, 0, new Intent("com.android.example.USB_PERMISSION"), 0);
                usbManager.requestPermission(device, pi);
                return -1000;
            }
            if (vid == device.getVendorId() && pid == device.getProductId()) {
                this.m_usbDevice = device;
                this.m_usbInterface = this.m_usbDevice.getInterface(0);
                this.m_inEndpoint = this.m_usbInterface.getEndpoint(0);
                this.m_outEndpoint = this.m_usbInterface.getEndpoint(1);
                (this.m_connection = usbManager.openDevice(this.m_usbDevice)).claimInterface(this.m_usbInterface, true);
                this.m_iSendPackageSize = this.m_outEndpoint.getMaxPacketSize();
                this.m_iRecvPackageSize = this.m_inEndpoint.getMaxPacketSize();
                return 0;
            }
        }
        return -100;
    }
    
    public int sendPacketSize() {
        return this.m_iSendPackageSize;
    }
    
    public int recvPacketSize() {
        return this.m_iRecvPackageSize;
    }
    
    public int sendData(final byte[] bSendBuf, final int iSendLen, final int iTimeOut) {
        int iRV = -1;
        if (iSendLen > bSendBuf.length) {
            return -101;
        }
        final int iPackageSize = this.sendPacketSize();
        if (iSendLen > iPackageSize) {
            return -101;
        }
        final byte[] bSendBufTmp = new byte[iPackageSize];
        System.arraycopy(bSendBuf, 0, bSendBufTmp, 0, iSendLen);
        iRV = this.m_connection.bulkTransfer(this.m_outEndpoint, bSendBufTmp, iPackageSize, iTimeOut);
        Log.w("IDData 发送:","预计长度："+iPackageSize);
        Log.w("IDData 发送:","实际长度："+iRV);
        Log.w("IDData 发送:",">>>>"+ org.zz.idcard_hid_driver.zzStringTrans.hex2str(bSendBufTmp));
        return iRV;
    }
    
    public int recvData(final byte[] bRecvBuf, final int iRecvLen, final int iTimeOut) {
        int iRV = -1;
        if (iRecvLen > bRecvBuf.length) {
            return -101;
        }
        final int iPackageSize = this.recvPacketSize();
        final byte[] bRecvBufTmp = new byte[iPackageSize];
        for (int i = 0; i < iRecvLen; i += iPackageSize) {
            int nDataLen = iRecvLen - i;
            if (nDataLen > iPackageSize) {
                nDataLen = iPackageSize;
            }
            Log.e("recvData:", "---------------------------------------------start------------------");
            iRV = this.m_connection.bulkTransfer(this.m_inEndpoint, bRecvBufTmp, nDataLen, iTimeOut);
            Log.e("IDData 接收:","预计长度："+iRecvLen);
            Log.e("IDData 接收:","实际长度："+iRV);
            Log.e("IDData 接收:","<<<<"+ org.zz.idcard_hid_driver.zzStringTrans.hex2str(bRecvBuf));
            if (iRV < 0) {
                return iRV;
            }
            System.arraycopy(bRecvBufTmp, 0, bRecvBuf, i, iRV);
        }
        return iRV;
    }
    
    public int closeDev() {
        if (this.m_connection != null) {
            this.m_connection.releaseInterface(this.m_usbInterface);
            this.m_connection.close();
            this.m_connection = null;
        }
        return 0;
    }
    
    private void regUsbMonitor() {
        final IntentFilter filter = new IntentFilter("com.android.example.USB_PERMISSION");
        this.m_ctx.registerReceiver(this.mUsbReceiver, filter);
    }
}
