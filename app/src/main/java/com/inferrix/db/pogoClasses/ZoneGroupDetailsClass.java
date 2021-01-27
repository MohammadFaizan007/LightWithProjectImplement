package com.inferrix.db.pogoClasses;

import android.os.Parcel;
import android.os.Parcelable;

public class ZoneGroupDetailsClass implements Parcelable {
    String groupZoneName="";
    int groupZoneId=0;
    boolean groupZoneStatus=false;
    int groupZoneDimming=0;

    public int getGroupZoneId() {
        return groupZoneId;
    }

    public void setGroupZoneId(int groupZoneId) {
        this.groupZoneId = groupZoneId;
    }

    public String getGroupZoneName() {
        return groupZoneName;
    }

    public void setGroupZoneName(String groupZoneName) {
        this.groupZoneName = groupZoneName;
    }

    public void setGroupZoneDimming(int groupZoneDimming) {
        this.groupZoneDimming = groupZoneDimming;
    }

    public int getGroupZoneDimming() {
        return groupZoneDimming;
    }

    public boolean getZoneGroupStatus() {
        return groupZoneStatus;
    }





    public void setGroupZoneStatus(boolean groupZoneStatus) {
        this.groupZoneStatus = groupZoneStatus;
    }

    public ZoneGroupDetailsClass() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.groupZoneName);
        dest.writeInt(this.groupZoneId);
        dest.writeByte(this.groupZoneStatus ? (byte) 1 : (byte) 0);
        dest.writeInt(this.groupZoneDimming);
    }

    protected ZoneGroupDetailsClass(Parcel in) {
        this.groupZoneName = in.readString();
        this.groupZoneId = in.readInt();
        this.groupZoneStatus = in.readByte() != 0;
        this.groupZoneDimming = in.readInt();
    }

    public static final Creator<ZoneGroupDetailsClass> CREATOR = new Creator<ZoneGroupDetailsClass>() {
        @Override
        public ZoneGroupDetailsClass createFromParcel(Parcel source) {
            return new ZoneGroupDetailsClass (source);
        }

        @Override
        public ZoneGroupDetailsClass[] newArray(int size) {
            return new ZoneGroupDetailsClass[size];
        }
    };
}
