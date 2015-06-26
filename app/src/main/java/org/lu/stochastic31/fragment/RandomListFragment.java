package org.lu.stochastic31.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import org.lu.stochastic31.R;
import org.lu.stochastic31.adapter.RandomListAdapter;
import org.lu.stochastic31.content.SQLiteHelper;

public class RandomListFragment extends ListFragment {

    public static final String STATE_ACTIVATED_POSITION = "activated_position";

    private Callbacks mCallbacks = sDummyCallbacks;
    private int mActivatedPosition = ListView.INVALID_POSITION;

    private RandomListAdapter randomListAdapter;
    private Cursor cursor;

    public interface Callbacks {

        /**
         * 选中某项目时触发此方法。
         *
         * @param id 选中项目的id（非position）
         */
        void onItemSelected(long id, boolean startNewActivityIfNeeded);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(long id, boolean startNewActivityIfNeeded) {
            // Do nothing
        }
    };

    public RandomListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cursor = SQLiteHelper.getInstance(getActivity()).getNames();
        randomListAdapter = new RandomListAdapter(getActivity(),
                cursor, false);
        setListAdapter(randomListAdapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState
                    .getInt(STATE_ACTIVATED_POSITION));
        }

        // 设置ItemLongClickListener
        getListView().setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> l, View v,
                                                   final int position, long id) {
                        final int name_index = ((Cursor) randomListAdapter
                                .getItem(position)).getInt(0);
                        final String name = ((Cursor) randomListAdapter
                                .getItem(position)).getString(1);
                        new AlertDialog.Builder(getActivity())
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle(android.R.string.dialog_alert_title)
                                .setMessage(
                                        getString(
                                                R.string.message_delete_random,
                                                name))
                                .setNegativeButton(android.R.string.cancel,
                                        null)
                                .setPositiveButton(android.R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                SQLiteHelper.getInstance(
                                                        getActivity())
                                                        .deleteName(name_index);
                                                SQLiteHelper.getInstance(
                                                        getActivity())
                                                        .deleteRandoms(
                                                                name_index);
                                                refreshViews(mActivatedPosition == position);
                                            }
                                        }).show();
                        return true;
                    }
                });
    }

    @Override
    public void onDestroyView() {
        cursor.close();
        super.onDestroyView();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position,
                                long id) {
        super.onListItemClick(listView, view, position, id);
        mActivatedPosition = position;
        mCallbacks.onItemSelected(id, true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        getListView().setChoiceMode(
                activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
                        : ListView.CHOICE_MODE_NONE);
    }

    public int getActivatedPosition() {
        return mActivatedPosition;
    }

    public void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    private void refreshViews(boolean refreshDetailUI) {
        Cursor c = randomListAdapter.getCursor();
        randomListAdapter.changeCursor(SQLiteHelper.getInstance(getActivity())
                .getNames());
        randomListAdapter.notifyDataSetChanged();
        c.close();
        if (refreshDetailUI) {
            if (randomListAdapter.getCount() > 0) {
                mActivatedPosition = 0;
                mCallbacks
                        .onItemSelected(randomListAdapter.getItemId(0), false);
            } else {
                mActivatedPosition = ListView.INVALID_POSITION;
                mCallbacks.onItemSelected(0, false);
            }
            setSelection(mActivatedPosition);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_new_random:
                final EditText editText = new EditText(getActivity());
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.action_new_random)
                        .setView(editText)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        SQLiteHelper s = SQLiteHelper
                                                .getInstance(getActivity());
                                        String name = editText.getText().toString();
                                        if (!TextUtils.isEmpty(name)
                                                && s.getNameIndex(name) == 0) {
                                            s.insertName(name);
                                            refreshViews(false);
                                        }
                                    }
                                }).show();
                return true;
            case R.id.menu_clear:
                SQLiteHelper.getInstance(getActivity()).clearDataBase();
                refreshViews(true);
                return true;
            case R.id.menu_import:
                SQLiteHelper.getInstance(getActivity()).importFromFile();
                refreshViews(mActivatedPosition == ListView.INVALID_POSITION);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_random_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
}
