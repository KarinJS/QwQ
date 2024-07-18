package com.tencent.qqnt.kernelpublic.nativeinterface;

import java.util.ArrayList;

/* loaded from: classes2.dex */
public final class GProGetHotSearchWordRsp {
    byte[] cookies;
    GProForumBody forums;
    ArrayList<GProHotSearchWord> words;

    public GProGetHotSearchWordRsp() {
        this.words = new ArrayList<>();
        this.cookies = new byte[0];
        this.forums = new GProForumBody();
    }

    public byte[] getCookies() {
        return this.cookies;
    }

    public GProForumBody getForums() {
        return this.forums;
    }

    public ArrayList<GProHotSearchWord> getWords() {
        return this.words;
    }

    public String toString() {
        return "GProGetHotSearchWordRsp{words=" + this.words + ",cookies=" + this.cookies + ",forums=" + this.forums + ",}";
    }

    public GProGetHotSearchWordRsp(ArrayList<GProHotSearchWord> arrayList, byte[] bArr, GProForumBody gProForumBody) {
        this.words = new ArrayList<>();
        this.cookies = new byte[0];
        this.forums = new GProForumBody();
        this.words = arrayList;
        this.cookies = bArr;
        this.forums = gProForumBody;
    }
}
