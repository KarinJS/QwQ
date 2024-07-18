package com.tencent.qqnt.kernelpublic.nativeinterface;

import java.util.HashMap;

public interface IGetTransferableMemberCallback {
    void onResult(int i2, String str, HashMap<String, MemberInfo> hashMap);
}