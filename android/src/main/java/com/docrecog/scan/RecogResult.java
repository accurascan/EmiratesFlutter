package com.docrecog.scan;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

public class RecogResult implements Serializable
{
    public String lines = "";//
    public String docType = "";//
    public String country = "";//
    public String surname = "";//
    public String givenname = "";//
    public String docnumber = "";//
    public String docchecksum = "";//
    public String nationality = "";//
    public String birth = "";//
    public String birthchecksum = "";//
    public String sex = "";//
    public String expirationdate = "";//
    public String expirationchecksum = "";//
    public String otherid = "";//
    public String otheridchecksum = "";//
    public String secondrowchecksum = "";
    public int ret = 0;

    public RecType recType = RecType.INIT;
    public boolean bRecDone = false;
    public boolean bFaceReplaced = false;

    public Bitmap faceBitmap = null;
    public void SetResult(int[] intData)
    {
        int i,k=0,len;
        byte[] tmp = new byte[100];
        len = intData[k++]; for(i=0;i<len;++i) tmp[i] = (byte)intData[k++]; tmp[i] = 0; lines = convchar2string(tmp);
        len = intData[k++]; for(i=0;i<len;++i) tmp[i] = (byte)intData[k++]; tmp[i] = 0; docType = convchar2string(tmp);
        len = intData[k++]; for(i=0;i<len;++i) tmp[i] = (byte)intData[k++]; tmp[i] = 0; country = convchar2string(tmp);
        len = intData[k++]; for(i=0;i<len;++i) tmp[i] = (byte)intData[k++]; tmp[i] = 0; surname = convchar2string(tmp);
        len = intData[k++]; for(i=0;i<len;++i) tmp[i] = (byte)intData[k++]; tmp[i] = 0; givenname = convchar2string(tmp);
        len = intData[k++]; for(i=0;i<len;++i) tmp[i] = (byte)intData[k++]; tmp[i] = 0; docnumber = convchar2string(tmp);
        len = intData[k++]; for(i=0;i<len;++i) tmp[i] = (byte)intData[k++]; tmp[i] = 0; docchecksum = convchar2string(tmp);
        len = intData[k++]; for(i=0;i<len;++i) tmp[i] = (byte)intData[k++]; tmp[i] = 0; nationality = convchar2string(tmp);
        len = intData[k++]; for(i=0;i<len;++i) tmp[i] = (byte)intData[k++]; tmp[i] = 0; birth = convchar2string(tmp);
        len = intData[k++]; for(i=0;i<len;++i) tmp[i] = (byte)intData[k++]; tmp[i] = 0; birthchecksum = convchar2string(tmp);
        len = intData[k++]; for(i=0;i<len;++i) tmp[i] = (byte)intData[k++]; tmp[i] = 0; sex = convchar2string(tmp);
        len = intData[k++]; for(i=0;i<len;++i) tmp[i] = (byte)intData[k++]; tmp[i] = 0; expirationdate = convchar2string(tmp);
        len = intData[k++]; for(i=0;i<len;++i) tmp[i] = (byte)intData[k++]; tmp[i] = 0; expirationchecksum = convchar2string(tmp);
        len = intData[k++]; for(i=0;i<len;++i) tmp[i] = (byte)intData[k++]; tmp[i] = 0; otherid = convchar2string(tmp);
        len = intData[k++]; for(i=0;i<len;++i) tmp[i] = (byte)intData[k++]; tmp[i] = 0; otheridchecksum = convchar2string(tmp);
        len = intData[k++]; for(i=0;i<len;++i) tmp[i] = (byte)intData[k++]; tmp[i] = 0; secondrowchecksum = convchar2string(tmp);
    }

    public static int getByteLength(byte[] str, int maxLen) {
        int i, len = 0;
        for (i = 0; i < maxLen; ++i)
        {
            if (str[i] == 0)
            {
                break;
            }
        }
        len = i;
        return len;
    }

    public static String convchar2string(byte[] chstr)
    {
        int len = getByteLength(chstr, 2000);
        String outStr = null;
        try {
            outStr = new String(chstr, 0, len ,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return outStr;
    }
}
