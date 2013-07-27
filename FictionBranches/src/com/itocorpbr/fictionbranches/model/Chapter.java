package com.itocorpbr.fictionbranches.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Chapter implements Parcelable {
    public String mParent = null;
    public String mPage = null;
    public String mTitle = null;
    public String mContent = null;
    public String mAuthor = null;
    public long mDate = 0;
    public int mRead = 0;
    public int mFav = 0;

    /**
     * Default constructor.
     */
    public Chapter() {}

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Write the object attributes on Parcel.
     * 
     * @param out - Parcel to write the attributes.
     * @param flags - Parcel flags.
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mParent);
        out.writeString(mPage);
        out.writeString(mTitle);
        out.writeString(mContent);
        out.writeString(mAuthor);
        out.writeLong(mDate);
        out.writeInt(mRead);
        out.writeInt(mFav);
    }

    /**
     * Constructor based on Parcel.
     * 
     * @param in parcel to read the attributes.
     */
    protected Chapter(Parcel in) {
        mParent = in.readString();
        mPage = in.readString();
        mTitle = in.readString();
        mContent = in.readString();
        mAuthor = in.readString();
        mDate = in.readLong();
        mRead = in.readInt();
        mFav = in.readInt();
    }

    public static final Parcelable.Creator<Chapter> CREATOR = new Parcelable.Creator<Chapter>() {
        @Override
        public Chapter createFromParcel(Parcel in) {
            return new Chapter(in);
        }

        @Override
        public Chapter[] newArray(int size) {
            return new Chapter[size];
        }
    };
}
