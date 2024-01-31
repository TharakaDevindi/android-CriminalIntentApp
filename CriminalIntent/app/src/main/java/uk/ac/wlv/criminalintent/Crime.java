package uk.ac.wlv.criminalintent;

import java.util.UUID;
import java.util.Date;

public class Crime {

    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;
    private String mSuspect;
    private long mSuspectId;

    public  Crime() {
        this(UUID.randomUUID());
    }

    public Crime(UUID id){
        mId = id;
        mDate = new Date();
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public UUID getMid() {
        return mId;
    }

    public String getSuspect(){ return mSuspect; }

    public void setSuspect(String suspect) { mSuspect = suspect; }

    public  String getPhotoFilename(){
        return "IMG" +getId().toString()+ ".jpg";
    }
    public long getSuspectId() {
        return mSuspectId;
    }

    public void setSuspectId(long suspectId) {
        mSuspectId = suspectId;
    }
}
