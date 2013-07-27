package com.itocorpbr.fictionbranches.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ImageURL implements Parcelable {
    public String mURL = null;
    public String mPath = null;
    public int mDownloaded = 0;

    /**
     * Default constructor.
     */
    public ImageURL() {}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mURL);
        out.writeString(mPath);
        out.writeInt(mDownloaded);
    }

    protected ImageURL(Parcel in) {
        mURL = in.readString();
        mPath = in.readString();
        mDownloaded = in.readInt();
    }

    public static final Parcelable.Creator<ImageURL> CREATOR = new Parcelable.Creator<ImageURL>() {
        @Override
        public ImageURL createFromParcel(Parcel in) {
            return new ImageURL(in);
        }

        @Override
        public ImageURL[] newArray(int size) {
            return new ImageURL[size];
        }
    };
}
