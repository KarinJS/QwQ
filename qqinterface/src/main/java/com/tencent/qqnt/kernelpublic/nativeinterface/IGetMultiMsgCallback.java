package com.tencent.qqnt.kernelpublic.nativeinterface;

import java.util.ArrayList;

public interface IGetMultiMsgCallback {
    void onResult(int code, String why, ArrayList<MsgRecord> msgList);
}