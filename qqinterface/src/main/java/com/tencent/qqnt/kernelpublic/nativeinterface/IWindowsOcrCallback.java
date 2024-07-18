package com.tencent.qqnt.kernelpublic.nativeinterface;

import java.util.ArrayList;

public interface IWindowsOcrCallback {
    void onResult(int i2, String str, ArrayList<WeChatOCRResultLine> arrayList);
}
