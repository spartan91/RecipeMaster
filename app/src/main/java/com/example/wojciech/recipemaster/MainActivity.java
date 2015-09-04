package com.example.wojciech.recipemaster;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private View backCoverView;

    //private  FloatingActionsMenu menuMultipleActions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //doesnt need to be used anymore
            //actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
        backCoverView = (View) findViewById(R.id.view_backcover);
        final FloatingActionsMenu menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.fab_menu);
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

        final FloatingActionButton floatingActionButtonGetRecipe = (FloatingActionButton) findViewById(R.id.fab_get_recipe);
        final FloatingActionButton floatingActionButtonLoginWithFacebook = (FloatingActionButton) findViewById(R.id.fab_login_fb);

        floatingActionButtonGetRecipe.setOnClickListener(this);
        floatingActionButtonLoginWithFacebook.setOnClickListener(this);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.fab_get_recipe:
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                //intent.putExtra(DetailActivity.EXTRA_CV, cV);
                startActivity(intent);
                break;
            case R.id.fab_login_fb:
                break;
             default:
                break;
        }
    }
}
