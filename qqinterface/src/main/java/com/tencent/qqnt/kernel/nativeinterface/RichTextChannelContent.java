package com.tencent.qqnt.kernel.nativeinterface;

/* compiled from: P */
/* loaded from: classes2.dex */
public final class RichTextChannelContent {
    FeedChannelInfo channelInfo;

    public RichTextChannelContent() {
        this.channelInfo = new FeedChannelInfo();
    }

    public FeedChannelInfo getChannelInfo() {
        return this.channelInfo;
    }

    public String toString() {
        return "RichTextChannelContent{channelInfo=" + this.channelInfo + ",}";
    }

    public RichTextChannelContent(FeedChannelInfo feedChannelInfo) {
        this.channelInfo = new FeedChannelInfo();
        this.channelInfo = feedChannelInfo;
    }
}
