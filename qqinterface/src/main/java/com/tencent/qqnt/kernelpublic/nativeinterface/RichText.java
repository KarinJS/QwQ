package com.tencent.qqnt.kernelpublic.nativeinterface;

import java.util.ArrayList;

/* compiled from: P */
/* loaded from: classes2.dex */
public final class RichText {
    ArrayList<RichTextContent> contents;

    public RichText() {
        this.contents = new ArrayList<>();
    }

    public ArrayList<RichTextContent> getContents() {
        return this.contents;
    }

    public String toString() {
        return "RichText{contents=" + this.contents + ",}";
    }

    public RichText(ArrayList<RichTextContent> arrayList) {
        this.contents = new ArrayList<>();
        this.contents = arrayList;
    }
}
