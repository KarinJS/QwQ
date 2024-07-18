package com.tencent.qqnt.kernelpublic.nativeinterface;

public interface IDeleteGroupFileCallback {
    void onResult(int code, String why, DeleteGroupFileResult result);
}