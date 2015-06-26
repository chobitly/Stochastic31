package org.lu.stochastic31.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.lu.stochastic31.R;
import org.lu.stochastic31.fragment.RandomDetailFragment;

public class RandomDetailActivity extends Activity {
    private RandomDetailFragment randomDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_detail);

        try {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            // do nothing
        }

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putLong(
                    RandomDetailFragment.ARG_ITEM_NAME_INDEX,
                    getIntent().getLongExtra(
                            RandomDetailFragment.ARG_ITEM_NAME_INDEX, 0));
            randomDetailFragment = new RandomDetailFragment();
            randomDetailFragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.random_detail_container, randomDetailFragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
//                NavUtils.navigateUpTo(this, new Intent(this,
//                        RandomListActivity.class));
                onBackPressed();
                return true;
            case R.id.menu_preference:
                startActivity(new Intent(this, Preferences.class));
                return true;
            default:
                if (randomDetailFragment != null)
                    return randomDetailFragment.onOptionsItemSelected(item);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (randomDetailFragment != null)
            randomDetailFragment.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_dommy, menu);
        return true;
    }
}
