package org.lu.stochastic31.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import org.lu.stochastic31.R;
import org.lu.stochastic31.content.SQLiteHelper;
import org.lu.stochastic31.fragment.RandomDetailFragment;
import org.lu.stochastic31.fragment.RandomListFragment;

/**
 * see <code>refs.xml</code> in values folders.
 *
 * @author chobitly
 */
public class RandomListActivity extends Activity implements
        RandomListFragment.Callbacks {

    /**
     * flag for two pane layout.
     */
    private boolean mTwoPane;
    /**
     * always shown.
     */
    private RandomListFragment randomListFragment;
    /**
     * shown in two pane layout.
     */
    private RandomDetailFragment randomDetailFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_random_list);
        // setContentView(R.layout.activity_random_twopane);

        try {
            getActionBar().setTitle(R.string.title_random_list);
        } catch (NullPointerException e) {
            setTitle(R.string.title_random_list);
        }

        randomListFragment = (RandomListFragment) getFragmentManager()
                .findFragmentById(R.id.random_list);
        if (randomListFragment == null)
            throw new IllegalStateException(
                    "Activity must contain an instance of RandomListFragment.");

        if (findViewById(R.id.random_detail_container) != null) {
            mTwoPane = true;
            randomListFragment.setActivateOnItemClick(true);

            // Set ActivatedPosition when this Activity load for the first time.
            if (randomListFragment.getActivatedPosition() == ListView.INVALID_POSITION
                    && randomListFragment.getListView().getCount() > 0)
                randomListFragment.setActivatedPosition(0);

            randomListFragment.getListView().performItemClick(
                    randomListFragment.getListView(),
                    randomListFragment.getActivatedPosition(),
                    randomListFragment.getListView().getItemIdAtPosition(
                            randomListFragment.getActivatedPosition()));
        }
    }

    @Override
    protected void onDestroy() {
        {// Close Databases
            SQLiteHelper sqLiteHelper = SQLiteHelper.getInstance(this);
            try {
                sqLiteHelper.getReadableDatabase().close();
            } catch (Exception e) {
                // do nothing
            }
            try {
                sqLiteHelper.getWritableDatabase().close();
            } catch (Exception e) {
                // do nothing
            }
        }

        super.onDestroy();
    }

    @Override
    public void onItemSelected(long name_index, boolean startNewActivityIfNeeded) {
        if (mTwoPane) {// When mTwoPane==true, replace RandomDetailFragment
            Bundle arguments = new Bundle();
            arguments.putLong(RandomDetailFragment.ARG_ITEM_NAME_INDEX,
                    name_index);
            randomDetailFragment = new RandomDetailFragment();
            randomDetailFragment.setArguments(arguments);
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.random_detail_container, randomDetailFragment)
                    .commit();
        } else if (startNewActivityIfNeeded) {
            // When mTwoPane==false, start RandomDetailActivity if needed
            Intent detailIntent = new Intent(this, RandomDetailActivity.class);
            detailIntent.putExtra(RandomDetailFragment.ARG_ITEM_NAME_INDEX,
                    name_index);
            startActivity(detailIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_preference:
                startActivity(new Intent(this, Preferences.class));
                return true;
            default:
                if (mTwoPane && randomDetailFragment != null) {
                    if (!randomDetailFragment.onOptionsItemSelected(item)
                            && randomListFragment != null) {
                        return randomListFragment.onOptionsItemSelected(item);
                    }
                } else if (randomListFragment != null) {
                    return randomListFragment.onOptionsItemSelected(item);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        // Let the Fragments use add() to add MenuItems.
        if (mTwoPane) {
            randomDetailFragment.onCreateOptionsMenu(menu, inflater);
        }
        randomListFragment.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_dommy, menu);
        return true;
    }
}
