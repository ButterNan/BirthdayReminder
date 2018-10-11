package com.nancy.birthdayreminder;

import android.Manifest;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor>{

    private LoginButton loginButton;
    private Button loadContactButton;
    private AccessToken mAccessToken;
    private CallbackManager callbackManager;
    private boolean firstimeLoad =false;
    public static final int CONTACT_LOADER = 1;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static String LOG_FOR_DEBUG = " Log for debug ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean loggedOut = AccessToken.getCurrentAccessToken() == null;
        loginButton = findViewById(R.id.login_button);
        loadContactButton = findViewById(R.id.button2);

        if (!loggedOut) {
            Log.d(" LOG_FOR_DEBUG ", "Username is: " + Profile.getCurrentProfile().getName());

            //Using Graph API
            getUserProfile(AccessToken.getCurrentAccessToken());
        }



        loginButton.setReadPermissions(Arrays.asList("email", "public_profile"));
        callbackManager = CallbackManager.Factory.create();

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                //loginResult.getAccessToken();
                //loginResult.getRecentlyDeniedPermissions()
                //loginResult.getRecentlyGrantedPermissions()
                boolean loggedIn = AccessToken.getCurrentAccessToken() == null;
                Log.d(" LOG_FOR_DEBUG ", loggedIn + " ??");
                String userId = loginResult.getAccessToken().getUserId();
                getUserProfile(AccessToken.getCurrentAccessToken());

            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });


        loadContactButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button2:
                if(firstimeLoad==false) {
                    checkForPermission();
                    getLoaderManager().initLoader(CONTACT_LOADER, null, this);
                    firstimeLoad = true;
                }
                else
                {
                    getLoaderManager().restartLoader(CONTACT_LOADER,null,this);
                    //getLoaderManager().restartLoader(BIRTHDAY_LOADER, null, this);
                }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkForPermission()
    {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            Log.d(" LOG_FOR_DEBUG " , "Permission already granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getUserProfile(AccessToken currentAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(
                currentAccessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d("TAG", object.toString());
                        try {
                            String first_name = object.getString("first_name");
                            String last_name = object.getString("last_name");
                            String email = object.getString("email");
                            String id = object.getString("id");
                            //String bday = object.getString("birthday");
                            String image_url = "https://graph.facebook.com/" + id + "/picture?type=normal";

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name,last_name,email,id");
        request.setParameters(parameters);
        request.executeAsync();

    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        switch (i) {
            case CONTACT_LOADER:
                String[] projection = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME };
                CursorLoader contactCursor = new CursorLoader(this,ContactsContract.Contacts.CONTENT_URI, projection, null, null,
                        ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
                return contactCursor;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case CONTACT_LOADER:
                if (cursor != null && cursor.getCount() > 0) {
                    ContentResolver cr = getContentResolver();
                    while(cursor.moveToNext())
                    {
                        Map<String, String> contactInfoMap = new HashMap<String, String>();
                        String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Data._ID));
                        String displayName =  cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                        String columns[] = {
                                ContactsContract.CommonDataKinds.Event.START_DATE,
                                ContactsContract.CommonDataKinds.Event.TYPE,
                                ContactsContract.CommonDataKinds.Event.MIMETYPE,
                        };

                        String where = Event.TYPE + "=" + Event.TYPE_BIRTHDAY +
                                " and " + Event.MIMETYPE + " = '" + Event.CONTENT_ITEM_TYPE + "' and "                  + ContactsContract.Data.CONTACT_ID + " = " + contactId;

                        String[] selectionArgs = null;
                        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME;
                        Cursor birthdayCur = cr.query(ContactsContract.Data.CONTENT_URI, columns, where, selectionArgs, sortOrder);
                        if (birthdayCur.getCount() > 0) {
                            while (birthdayCur.moveToNext()) {
                                String birthday = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                                Log.d(" LOG_FOR_DEBUG ", birthday +" "+displayName);
                            }
                        }
                        birthdayCur.close();
                    }
                }
                else {
                    Log.e(" LOG_FOR_DEBUG ", "No contacts available");
                }
                break;

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
