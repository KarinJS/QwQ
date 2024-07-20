package com.tencent.qqnt.kernel.nativeinterface;

import java.util.ArrayList;

public class MsgRecord {
    //public AnonymousExtInfo anonymousExtInfo;
    public int atType;
    public int avatarFlag;
    public String avatarMeta;
    public String avatarPendant;
    public int categoryManage;
    public String channelId;
    public String channelName;
    public int chatType;
    //public GuildClientIdentity clientIdentityInfo;
    public long clientSeq;
    public long cntSeq;
    public long commentCnt;
    public int directMsgFlag;
    //public ArrayList<DirectMsgMember> directMsgMembers;
    public boolean editable;
    public ArrayList<MsgElement> elements;
    //public ArrayList<MsgEmojiLikes> emojiLikesList;
    public byte[] extInfoForUI;
    public String feedId;
    public Integer fileGroupSize;
    //public FoldingInfo foldingInfo;
    //public FreqLimitInfo freqLimitInfo;
    public long fromAppid;
    //public FromRoleInfo fromChannelRoleInfo;
    //public FromRoleInfo fromGuildRoleInfo;
    public long fromUid;
    public byte[] generalFlags;
    public long guildCode;
    public String guildId;
    public String guildName;
    public boolean isImportMsg;
    public boolean isOnlineMsg;
    //public FromRoleInfo levelRoleInfo;
    //public HashMap<Integer, MsgAttributeInfo> msgAttrs;
    public byte[] msgEventInfo;
    public long msgId;
    public byte[] msgMeta;
    public long msgRandom;
    public long msgSeq;
    public long msgTime;
    public int msgType;
    //public MultiTransInfo multiTransInfo;
    public int nameType;
    public String peerName;
    public String peerUid;
    public long peerUin;
    //public GuildMedal personalMedal;
    public long recallTime;
    public ArrayList<MsgRecord> records;
    public long roleId;
    public int roleType;
    public String sendMemberName;
    public String sendNickName;
    public String sendRemarkName;
    public int sendStatus;
    public int sendType;
    public String senderUid;
    public long senderUin;
    public int sourceType;
    public int subMsgType;
    public long timeStamp;
}
