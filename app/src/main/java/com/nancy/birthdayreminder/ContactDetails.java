package com.nancy.birthdayreminder;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;

public class ContactDetails implements Parcelable{

    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String contactBday;
    private boolean isChecked;

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    protected ContactDetails(Parcel in) {
        contactName = in.readString();
        contactEmail = in.readString();
        contactPhone = in.readString();
        contactBday = in.readString();
    }

    public ContactDetails()
    {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(contactName);
        dest.writeString(contactEmail);
        dest.writeString(contactPhone);
        dest.writeString(contactBday);
    }

    public static final Creator<ContactDetails> CREATOR = new Creator<ContactDetails>() {
        @Override
        public ContactDetails createFromParcel(Parcel in) {
            return new ContactDetails(in);
        }

        @Override
        public ContactDetails[] newArray(int size) {
            return new ContactDetails[size];
        }
    };

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactBday() {

        return contactBday;
    }

    public void setContactBday(String contactBday) {
        this.contactBday = contactBday;
    }
}

