package com.example.wojciech.recipemaster;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.wojciech.recipemaster.utils.ConnectivityUtil;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = MainActivity.class.getSimpleName();
    private View backCoverView;
    private CallbackManager callbackManager;
    private FloatingActionsMenu menuMultipleActions;
    private FloatingActionButton floatingActionButtonGetRecipe;
    private FloatingActionButton floatingActionButtonLoginWithFacebook;
    private ProfileTracker profileTracker;
    private Profile facebookUserProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());


        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "Login facebook success");
                        profileTracker = new ProfileTracker() {
                            @Override
                            protected void onCurrentProfileChanged(Profile profile, Profile currentProfile) {
                                profileTracker.stopTracking();
                                facebookUserProfile = currentProfile;
                                if (currentProfile != null) {
                                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.login_successful) + " " + facebookUserProfile.getFirstName() + " :)", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                                updateUserLogedState();
                            }

                        };
                        updateUserProfile();
                        updateUserLogedState();
                        profileTracker.startTracking();


                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(MainActivity.this, getString(R.string.login_cancelled), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        /*Login User if has loged before*/
        updateUserProfile();


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }


        backCoverView = (View) findViewById(R.id.view_backcover);
        backCoverView.setOnClickListener(this);
        menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.fab_menu);
        menuMultipleActions.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                backCoverView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onMenuCollapsed() {
                backCoverView.setVisibility(View.GONE);

            }
        });

        floatingActionButtonGetRecipe = (FloatingActionButton) findViewById(R.id.fab_get_recipe);
        floatingActionButtonLoginWithFacebook = (FloatingActionButton) findViewById(R.id.fab_login_fb);

        floatingActionButtonGetRecipe.setOnClickListener(this);
        floatingActionButtonLoginWithFacebook.setOnClickListener(this);

        updateUserLogedState();

    }

    private void updateUserProfile() {
        if (isUserLoggedInWithFacebook()) {
            facebookUserProfile = Profile.getCurrentProfile();
            if (facebookUserProfile == null) {
                Log.d(TAG, "Profile is null while user is loged");
                updateUserLogedState();
            } else {
                Log.d(TAG, "Profile exists");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
        if (menuMultipleActions != null && menuMultipleActions.isExpanded())
            menuMultipleActions.collapse();
        updateUserProfile();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_get_recipe:
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                if (facebookUserProfile != null && isUserLoggedInWithFacebook()) {
                    intent.putExtra(DetailsActivity.EXTRA_PROFILENAME, facebookUserProfile.getName());
                    intent.putExtra(DetailsActivity.EXTRA_AVATAR_URI, facebookUserProfile.getProfilePictureUri(64, 64).toString());
                }
                startActivity(intent);
                break;
            case R.id.fab_login_fb:
                if (!isUserLoggedInWithFacebook())
                    if (ConnectivityUtil.isNetworkAvailable(MainActivity.this)) {
                        LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile", "user_friends"));
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.error_network_connection), Toast.LENGTH_LONG).show();
                    }
                else {
                    new MaterialDialog.Builder(this)
                            .title(R.string.logout_dialog_title)
                            .content(R.string.logout_dialog_content)
                            .positiveText(R.string.logout_dialog_logout)
                            .negativeText(R.string.logout_dialog_cancel)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    LoginManager.getInstance().logOut();
                                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.logout), Snackbar.LENGTH_SHORT)
                                            .setAction("Action", null).show();
                                    updateUserLogedState();
                                }
                            }).show();

                }
                break;
            default:
                if (menuMultipleActions != null)
                    menuMultipleActions.collapse();
                break;
        }
    }

    private void updateUserLogedState() {
        if (isUserLoggedInWithFacebook()) {
            floatingActionButtonLoginWithFacebook.setTitle(getString((R.string.logout_with_facebook)));
        } else {
            floatingActionButtonLoginWithFacebook.setTitle(getString((R.string.login_with_facebook)));
        }
    }

    public boolean isUserLoggedInWithFacebook() {
        return AccessToken.getCurrentAccessToken() != null;
    }
}
