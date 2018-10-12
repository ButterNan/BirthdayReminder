package com.nancy.birthdayreminder;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

public class DataLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor>{


    private final Context mContext;
    private CursorHandler mCursorHandler;


    public DataLoaderCallbacks(Context context) {
        this.mContext = context;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        switch (i) {
            case Constants.CONTACT_LOADER:
                String[] projection = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME };
                CursorLoader contactCursor = new CursorLoader(mContext,ContactsContract.Contacts.CONTENT_URI, projection, null, null,ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
                return contactCursor;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        switch (loader.getId()) {
            case Constants.CONTACT_LOADER:
                if (cursor != null && cursor.getCount() > 0) {
                    ContentResolver cr = mContext.getContentResolver();
                    while(cursor.moveToNext())
                    {
                        ContactDetails contact= new ContactDetails();
                        String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                        contact.setContactName(displayName);

                        //get birthday
                        String columns[] = {
                                ContactsContract.CommonDataKinds.Event.START_DATE,
                                ContactsContract.CommonDataKinds.Event.TYPE,
                                ContactsContract.CommonDataKinds.Event.MIMETYPE,
                        };
                        String where = ContactsContract.CommonDataKinds.Event.TYPE + "=" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY +
                                " and " + ContactsContract.CommonDataKinds.Event.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE + "' and "+ ContactsContract.Data.CONTACT_ID + " = " + contactId;
                        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME;
                        Cursor birthdayCur = cr.query(ContactsContract.Data.CONTENT_URI, columns, where, null, sortOrder);
                        if (birthdayCur.getCount() > 0) {
                            while (birthdayCur.moveToNext()) {
                                String birthday = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                                if(birthday!=null) {
                                    contact.setContactBday(birthday);
                                    Log.d(" LOG_FOR_DEBUG ", birthday + " " + displayName);
                                }
                            }
                            birthdayCur.close();
                        }

                        //get email ID
                        Cursor emailCur = cr.query(
                                ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId,
                                null, null);
                        if(emailCur!=null && emailCur.getCount()>0) {
                            while (emailCur.moveToNext()) {
                                //to get the contact names
                                String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                                Log.d("LOG_FOR_DEBUG", "Email "+ email);
                                if (email != null) {
                                    contact.setContactEmail(email);
                                }
                            }
                            emailCur.close();
                        }


                        //get Phone number
                        Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "+contactId, null, null);
                        if (phoneCursor != null && phoneCursor.moveToFirst()) {
                            String phone = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            contact.setContactPhone(phone);
                            phoneCursor.close();
                        }

                        //contactList.add(contact);
                        if (mCursorHandler != null) {
                            mCursorHandler.handleCursor(contact);
                        }
                    }
                }
                else {
                    Log.d(" LOG_FOR_DEBUG ", "No contacts available");
                }
                //Log.d(" LOG_FOR_DEBUG ", "ArrayList size "+contactList.size());
                cursor.close();
                break;

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mCursorHandler != null) {
            mCursorHandler.handleCursor(null);
        }
    }

    public void setCursorHandler(CursorHandler cursorHandler) {
        this.mCursorHandler = cursorHandler;
    }

    public interface CursorHandler {
        void handleCursor(ContactDetails contact);
    }
}
