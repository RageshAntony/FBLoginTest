package com.hfad.fblogintest;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    String email="===",name=".====";

    TextView txtstatus;
    EditText txt3;
    LoginButton login_button;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.hfad.fblogintest",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {


        } catch (NoSuchAlgorithmException e) {

        }

        FacebookSdk.sdkInitialize(getApplicationContext());
        LoginManager.getInstance().logOut();

        intializeControls();
        LoginWithFB();

    }

    private void intializeControls()
    {
        callbackManager = CallbackManager.Factory.create();
        txtstatus = (TextView) findViewById(R.id.textView);
        login_button = (LoginButton) findViewById(R.id.login_button);

        login_button.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday", "user_friends","user_posts", "user_status")); // Setting permissions
    }

    void LoginWithFB ()
    {
        login_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {

                final Profile profile = Profile.getCurrentProfile();
                txtstatus.setText("Login Success" );

                GraphRequest request = GraphRequest.newMeRequest(   //To read profile Info
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {

                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    TextView txt2 = (TextView) findViewById(R.id.textView2);
                                    TextView nametext = (TextView) findViewById(R.id.nameText);
                                  //  txt3 = (EditText) findViewById(R.id.textView3);


                                    // Application code
                                    name = object.getString("name");
                                    email = object.getString("email");

                                    nametext.setText(name);
                                    txt2.setText(email);


                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();

                //Now graph Query to read timeline posts
                Bundle params = new Bundle();
            //   params.putString("fields", "message,created_time,id,full_picture,status_type,source,comments.summary(true),likes.summary(true),attachments,comments");
                params.putString("fields", "message,created_time,id,full_picture,status_type,source,likes.summary(true),attachments,comments");
                //testing
                 // params.putString("fields", "message,comments");

                params.putString("limit", "1000");

    /* make the API call */
                //Method - 1
/*
                GraphRequest requestPost = GraphRequest.newMeRequest(   //To read profile Info
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {

                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                   // txt3.setText(response.toString());
                                    writeToFile(object.toString(2),"jsonobject");




                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                requestPost.setParameters(params);
                requestPost.executeAsync();
*/

                //Method - 2
                new GraphRequest( loginResult.getAccessToken(), "/me/posts", params, HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response)  {
                            /* handle the result */
                            try {
                                EditText postsText = (EditText) findViewById(R.id.postText);
                                String res = response.toString();
                                res = res.replaceAll("\\{Response:\\s*\\w*\\W*\\s*[0-9]*\\W*\\w*:", "");
                                res = res.replaceAll("\\,\\s*error:\\s*null\\W*\\s*","");
                                postsText.setText(res);
                                writeToFile(res,"response");
                            }
                            catch (Exception e) {
                                    e.printStackTrace();
                                }
                                }
                            }

                ).executeAsync();


            }

            @Override
            public void onCancel() {
                txtstatus.setText("Login canceled");
            }

            @Override
            public void onError(FacebookException error) {
                txtstatus.setText("Login failed"+error.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

    public void writeToFile(String data,String filename)
    {
        try
        {




         File path=null;
        // Get the directory for the user's public pictures directory.
        String state = Environment.getExternalStorageState();
            Toast.makeText(getApplicationContext(), Environment.DIRECTORY_DCIM + "/YourFolder/",Toast.LENGTH_LONG).show();
            Log.e("Direcotry ",Environment.DIRECTORY_DCIM + "/YourFolder/");

            if (Environment.MEDIA_MOUNTED.equals(state)) {
                Toast.makeText(getApplicationContext(), "media mounted",Toast.LENGTH_SHORT).show();
                Log.e("Media state: ","mounted");
/*
                path =
                    Environment.getExternalStoragePublicDirectory
                            (
                                    //Environment.DIRECTORY_PICTURES
                                     "/sdcard/DCIM"+ "/YourFolder/"
                                   // "/YourFolder/"
                            );
*/
                path = new File ( "/sdcard/DCIM"+ "/YourFolder/");

            // Make sure the path directory exists.
            if(!path.exists())
            {
                // Make it, if it doesn't exit
                path.mkdirs();
                Toast.makeText(getApplicationContext(),path.getAbsolutePath(),Toast.LENGTH_LONG).show();
                Log.e("Path",path.getAbsolutePath());
            }

        }

        else Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_LONG).show();


        final File file = new File(path,filename+ new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime())+".txt");
            Log.e("file Path",file.getAbsolutePath());

        // Save your stream, don't forget to flush() it before closing it.


            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);

            myOutWriter.close();

            fOut.flush();
            fOut.close();
        }


        catch (Exception e)
        {
            Log.e("Exception", "File write failed: " +e.toString());
            e.printStackTrace();
        }

        finally {
            Toast.makeText(getApplicationContext(),"Writed",Toast.LENGTH_LONG).show();
        }
    }




}
