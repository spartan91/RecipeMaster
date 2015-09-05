package com.example.wojciech.recipemaster;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.example.wojciech.recipemaster.model.Recipe;
import com.example.wojciech.recipemaster.utils.ConnectivityUtil;
import com.example.wojciech.recipemaster.utils.DataParserUtil;
import com.example.wojciech.recipemaster.utils.DownloadUtil;
import com.example.wojciech.recipemaster.utils.DownloadUtil.DownloadUtilCallback;
import com.example.wojciech.recipemaster.utils.FileUtil;
import com.example.wojciech.recipemaster.utils.NetworkConnectionUtil;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DetailsActivity extends AppCompatActivity implements NetworkConnectionUtil.NetworkConnectionCallback, DownloadUtilCallback {

    private final static String TAG = DetailsActivity.class.getSimpleName();
    private final static String JSONURL = "http://mooduplabs.com/test/info.php";

    public final static String EXTRA_PROFILENAME = "profile_name";
    public final static String EXTRA_AVATAR_URI = "avatar_uri";

    public final static String SAVED_PROFILENAME = "saved_profile_name";
    public final static String SAVED_AVATAR_URI = "saved_avatar_uri";
    public final static String SAVED_RECIPE = "saved_recipe";

    private CollapsingToolbarLayout collapsingToolbar;
    private Recipe recipe;
    private RecipeHolder recipeHolder;
    private String userName;
    private String userAvatarUri;
    BroadcastReceiver receiver;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_PROFILENAME, userName);
        outState.putString(SAVED_AVATAR_URI, userAvatarUri);
        if (recipe != null)
            outState.putParcelable(SAVED_RECIPE, recipe);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(getString(R.string.title_recipe));

        setupRecipeHolder();

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                userName = extras.getString(EXTRA_PROFILENAME);
                userAvatarUri = extras.getString(EXTRA_AVATAR_URI);
            }
        } else {
            userName = savedInstanceState.getString(SAVED_PROFILENAME);
            userAvatarUri = savedInstanceState.getString(SAVED_AVATAR_URI);
            recipe = savedInstanceState.getParcelable(SAVED_RECIPE);
            try {
                setData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        setupFbLogin();

        if (recipe == null)
            loadDataFromNet();

        receiver = new
                BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        try {
                            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                                long downloadId = intent.getLongExtra(
                                        DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                                DownloadManager.Query query = new DownloadManager.Query();

                                query.setFilterById(downloadId);
                                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                Cursor cursor = dm.query(query);
                                if (cursor.moveToFirst()) {
                                    int columnIndex = cursor
                                            .getColumnIndex(DownloadManager.COLUMN_STATUS);
                                    if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(columnIndex)) {
                                        String fileName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
                                        Log.d(TAG, fileName + " downloaded succesfully");
                                        Snackbar.make(findViewById(android.R.id.content), fileName + " " + getString(R.string.image_downloaded), Snackbar.LENGTH_SHORT)
                                                .setAction("Action", null).show();
                                    }
                                }
                                cursor.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (receiver != null)
            registerReceiver(receiver, new IntentFilter(
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (receiver != null)
            unregisterReceiver(receiver);
    }

    private void setupFbLogin() {

        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(userAvatarUri)) {
            recipeHolder.footer.setVisibility(View.VISIBLE);
            Glide.with(this).load(userAvatarUri).centerCrop().into(recipeHolder.footerAvatar);
            recipeHolder.footerFbLogin.setText("Loged as " + userName);
        } else {
            recipeHolder.footer.setVisibility(View.GONE);
        }
    }

    private void loadDataFromNet() {
        if (ConnectivityUtil.isNetworkAvailable(this)) {
            new NetworkConnectionUtil(this).execute(JSONURL);
        } else {
            Toast.makeText(this, getString(R.string.error_network_connection), Toast.LENGTH_LONG).show();
            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);
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
    public void onContentDownload(String content, int errorCode) {
        if (errorCode == 1) {
            Toast.makeText(this, getString(R.string.error_network_connection), Toast.LENGTH_LONG).show();
        } else {
            try {
                recipe = DataParserUtil.parseToRecipe(content);
                setData();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupRecipeHolder() {
        recipeHolder = new RecipeHolder();
        recipeHolder.title = (TextView) findViewById(R.id.recipe_dish_title);
        recipeHolder.description = (TextView) findViewById(R.id.recipe_description);
        recipeHolder.preparing = (TextView) findViewById(R.id.recipe_preparing);
        recipeHolder.ingredients = (TextView) findViewById(R.id.recipe_ingredients);
        recipeHolder.backdrop = (ImageView) findViewById(R.id.backdrop);


        recipeHolder.imagesLayout = (LinearLayout) findViewById(R.id.recipe_images_layout);

        /*Footer*/
        recipeHolder.footer = (LinearLayout) findViewById(R.id.recipe_footer);
        recipeHolder.footerFbLogin = (TextView) findViewById(R.id.recipe_footer_text);
        recipeHolder.footerAvatar = (CircleImageView) findViewById(R.id.recipe_footer_avatar);
    }

    private void setData() {
        if (recipe != null && recipeHolder != null) {
            if (collapsingToolbar != null && !TextUtils.isEmpty(recipe.title)) {
                collapsingToolbar.setTitle(recipe.title + " " + getString(R.string.title_recipe));
            }
            if (!TextUtils.isEmpty(recipe.title))
                recipeHolder.title.setText(recipe.title);
            if (!TextUtils.isEmpty(recipe.description))
                recipeHolder.description.setText(recipe.description);

            if (recipe.ingredients != null && recipe.ingredients.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (String ingredient : recipe.ingredients) {
                    sb.append("-").append(ingredient).append("\n");
                }
                recipeHolder.ingredients.setText(sb.toString());
            }

            if (recipe.preparing != null && recipe.preparing.size() > 0) {
                StringBuilder sb = new StringBuilder();
                int i = 1;
                for (String prep : recipe.preparing) {
                    sb.append(i).append(". ").append(prep).append("\n\n");
                    i++;
                }
                recipeHolder.preparing.setText(sb.toString());
            }


            if (recipe.imgs != null && recipe.imgs.size() > 0) {
                Glide.with(this).load(recipe.imgs.get(0)).centerCrop().into(recipeHolder.backdrop);
                for (final String imageUrl : recipe.imgs) {

                    final ImageView newImg = new ImageView(this);
                    LinearLayout.LayoutParams viewParamsCenter = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    viewParamsCenter.setMargins(10, 10, 10, 10);
                    newImg.setLayoutParams(viewParamsCenter);
                    Glide.with(this).load(imageUrl).fitCenter().into(newImg);
                    newImg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new MaterialDialog.Builder(DetailsActivity.this)
                                    .title(R.string.download_dialog_title)
                                    .content(R.string.download_dialog_content)
                                    .positiveText(R.string.download_dialog_positive)
                                    .negativeText(R.string.download_dialog_save)
                                    .neutralText(R.string.download_dialog_cancel)
                                    .callback(new MaterialDialog.ButtonCallback() {
                                        @Override
                                        public void onNegative(MaterialDialog dialog) {

                                            if (FileUtil.saveImageToPublicCameraDir(newImg.getDrawable(), FilenameUtils.getName(imageUrl))) {
                                                Snackbar.make(findViewById(android.R.id.content), FilenameUtils.getName(imageUrl) + " " + getString(R.string.image_saved), Snackbar.LENGTH_SHORT)
                                                        .setAction("Action", null).show();
                                            } else
                                                Snackbar.make(findViewById(android.R.id.content), FilenameUtils.getName(imageUrl) + " " + getString(R.string.error_image_exists), Snackbar.LENGTH_SHORT)
                                                        .setAction("Action", null).show();

                                        }

                                        @Override
                                        public void onPositive(MaterialDialog dialog) {

                                            try {
                                                if (ConnectivityUtil.isNetworkAvailable(DetailsActivity.this)) {
                                                    if (DownloadUtil.downloadWithDownloadManager(FilenameUtils.getBaseName(imageUrl), FilenameUtils.getName(imageUrl), imageUrl, DetailsActivity.this) == -1) {
                                                        Snackbar.make(findViewById(android.R.id.content), FilenameUtils.getName(imageUrl) + " " + getString(R.string.error_image_exists), Snackbar.LENGTH_SHORT)
                                                                .setAction("Action", null).show();
                                                    }
                                                } else {
                                                    Toast.makeText(DetailsActivity.this, getString(R.string.error_network_connection), Toast.LENGTH_LONG).show();
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    })
                                    .show();
                        }
                    });

                    CardView cardViewImage = new CardView(this);
                    LinearLayout.LayoutParams viewParamsCenterCard = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    int margin = (int) getResources().getDimension(R.dimen.card_margin);
                    viewParamsCenterCard.setMargins(margin, margin, margin, margin);
                    cardViewImage.setLayoutParams(viewParamsCenterCard);

                    LinearLayout linearLayout = new LinearLayout(this);
                    LinearLayout.LayoutParams viewParamsCenterLinear = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.setLayoutParams(viewParamsCenterLinear);


                    linearLayout.addView(newImg);
                    cardViewImage.addView(linearLayout);
                    recipeHolder.imgs.add(newImg);
                    recipeHolder.imagesLayout.addView(cardViewImage);

                }
            }

            /*BackDrop*/
            if (recipe.imgs != null && recipe.imgs.size() > 0)
                Glide.with(this).load(recipe.imgs.get(0)).centerCrop().into(recipeHolder.backdrop);
        }
    }

    @Override
    public void onImageDownload(String imageDownloaded) {
        if (!TextUtils.isEmpty(imageDownloaded))
            Snackbar.make(findViewById(android.R.id.content), imageDownloaded + " " + getString(R.string.image_downloaded), Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        else
            Snackbar.make(findViewById(android.R.id.content), imageDownloaded + " " + getString(R.string.error_image_download), Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
    }

    private class RecipeHolder {
        public ImageView backdrop;
        public TextView title;
        public TextView description;
        public TextView ingredients;
        public TextView preparing;
        public List<ImageView> imgs = new ArrayList<>();
        public LinearLayout imagesLayout;

        public LinearLayout footer;
        public TextView footerFbLogin;
        public CircleImageView footerAvatar;
    }
}
