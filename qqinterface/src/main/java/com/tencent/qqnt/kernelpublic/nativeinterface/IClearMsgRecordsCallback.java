package com.tencent.qqnt.kernelpublic.nativeinterface;

public interface IClearMsgRecordsCallback {
    void onResult(int code, String reason, long lastMsgId);
}
