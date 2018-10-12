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
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DataAccessor, DataReceiver{

    private LoginButton loginButton;
    private Button loadContactButton;
    private RecyclerView contactListRecyclerView;
    private CursorRecyclerViewAdapter adpater;
    private AccessToken mAccessToken;
    private CallbackManager callbackManager;
    private OnItemListener listener;
    private boolean firstimeLoad =false;
    private static String LOG_FOR_DEBUG = " Log for debug ";
    List<ContactDetails> contactList=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactList.clear();

        boolean loggedOut = AccessToken.getCurrentAccessToken() == null;
        loginButton = findViewById(R.id.login_button);
        loadContactButton = findViewById(R.id.button2);
        contactListRecyclerView = findViewById(R.id.contact_recycler);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        contactListRecyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration decoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        contactListRecyclerView.addItemDecoration(decoration);

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

        listener= new OnItemListener() {
            @Override
            public void onRowClicked(View view, int position) {

            }

            @Override
            public void onCheckBoxChecked(View view, int position) {

            }

            @Override
            public void onCheckBoxUnChecked(View view, int position) {

            }
        };

        DataAccessor accessor = (DataAccessor)this;
        accessor.requestDetailItems(this);

        adpater = new CursorRecyclerViewAdapter(this, contactList, listener);
        contactListRecyclerView.setAdapter(adpater);

    }


    @Override
    public void onResume() {
        super.onResume();

        if (adpater.getItemCount() > 0) {
            // already read from the database
        }
        else if (this instanceof DataAccessor) {
            // request detail items from SQLite to load the RecyclerView
//            DataAccessor accessor = (DataAccessor) this;
//            accessor.requestDetailItems(this);
//            adpater = new CursorRecyclerViewAdapter(this, contactList, listener);
//            contactListRecyclerView.setAdapter(adpater);
        }
    }

    @Override
    public void onClick(View view) {
        DataAccessor accessor = (DataAccessor)this;
        accessor.requestDetailItems(this);
        adpater.notifyDataSetChanged();

//        Intent intent = getIntent();
//        finish();
//        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkForPermission()
    {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, Constants.PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            Log.d(" LOG_FOR_DEBUG " , "Permission already granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == Constants.PERMISSIONS_REQUEST_READ_CONTACTS) {
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
    public void receiveDetailItems(ContactDetails items) {
        contactList.add(items);
        adpater.notifyDataSetChanged();

    }

    @Override
    public void requestDetailItems(DataReceiver receiver) {
        DataDAO dao = new DataDAO(this,Constants.CONTACT_LOADER);
        dao.requestDetailItems(receiver);
    }
}
