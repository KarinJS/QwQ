package com.tencent.qqnt.kernelpublic.nativeinterface;

/* compiled from: P */
/* loaded from: classes2.dex */
public final class CommentFeedRsp {
    long createTime;
    String id;
    long sequence;

    public CommentFeedRsp() {
        this.id = "";
    }

    public long getCreateTime() {
        return this.createTime;
    }

    public String getId() {
        return this.id;
    }

    public long getSequence() {
        return this.sequence;
    }

    public String toString() {
        return "CommentFeedRsp{id=" + this.id + ",createTime=" + this.createTime + ",sequence=" + this.sequence + ",}";
    }

    public CommentFeedRsp(String str, long j2, long j3) {
        this.id = "";
        this.id = str;
        this.createTime = j2;
        this.sequence = j3;
    }
}
