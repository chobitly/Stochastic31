package org.lu.stochastic31.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import org.lu.stochastic31.R;
import org.lu.stochastic31.activity.Preferences;
import org.lu.stochastic31.adapter.RandomDetailListAdapter;
import org.lu.stochastic31.content.SQLiteHelper;

import java.util.Date;
import java.util.Random;

public class RandomDetailFragment extends ListFragment implements
        SensorEventListener {
    public static final String ARG_ITEM_NAME_INDEX = "name_index";

    private RandomDetailListAdapter randomDetailAdapter;

    // 定义sensor管理器
    private SensorManager mSensorManager;
    // 震动
    private Vibrator vibrator;

    private Cursor cursor;
    private long name_index = 0;
    private String name = "Name";

    private Random rand = new Random(new Date().getTime());

    public RandomDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null
                && getArguments().containsKey(ARG_ITEM_NAME_INDEX)) {
            name_index = getArguments().getLong(ARG_ITEM_NAME_INDEX);
            name = SQLiteHelper.getInstance(getActivity()).getName(
                    name_index);

            cursor = SQLiteHelper.getInstance(getActivity())
                    .getRandoms(name_index);

            mSensorManager = (SensorManager) getActivity()
                    .getSystemService(Context.SENSOR_SERVICE);// 获取传感器管理服务
            vibrator = (Vibrator) getActivity().getSystemService(
                    Service.VIBRATOR_SERVICE);// 震动

            randomDetailAdapter = new RandomDetailListAdapter(
                    getActivity(), cursor, false);
            setListAdapter(randomDetailAdapter);

            Toast.makeText(getActivity(),
                    getText(R.string.toast_shake_phone), Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 设置ItemLongClickListener
        getListView().setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> l, View v,
                                                   final int position, long id) {
                        new AlertDialog.Builder(getActivity())
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle(name)
                                .setMessage(
                                        getString(
                                                R.string.message_delete_random_item,
                                                ((Cursor) randomDetailAdapter
                                                        .getItem(position))
                                                        .getString(1)))
                                .setNegativeButton(android.R.string.cancel,
                                        null)
                                .setPositiveButton(android.R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                SQLiteHelper
                                                        .getInstance(
                                                                getActivity())
                                                        .deleteRandom(
                                                                ((Cursor) randomDetailAdapter
                                                                        .getItem(position))
                                                                        .getInt(0));
                                                refreshViews();
                                            }
                                        }).show();
                        return true;
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshViews();
        // 加速度传感器
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                // 还有SENSOR_DELAY_UI、SENSOR_DELAY_FASTEST、SENSOR_DELAY_GAME等，
                // 根据不同应用，需要的反应速率不同，具体根据实际情况设定
                SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onStop() {
        mSensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        cursor.close();
        super.onDestroyView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_run_random:
                runRandom();
                return true;
            case R.id.menu_new_item:
                final EditText editText = new EditText(getActivity());
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.action_new_item)
                        .setView(editText)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        SQLiteHelper s = SQLiteHelper
                                                .getInstance(getActivity());
                                        String random = editText.getText()
                                                .toString();
                                        if (!TextUtils.isEmpty(random)) {
                                            s.insertRandom(name_index, random);
                                            refreshViews();
                                        }
                                    }
                                }).show();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_random_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // private boolean getRepeatFlag() {
    // return getSherlockActivity().getSharedPreferences(
    // Preferences.PREFS_NAME, Preferences.MODE_PRIVATE).getBoolean(
    // getString(R.string.preference_can_repeat), false);
    // }

    private void refreshViews() {
        Cursor c = randomDetailAdapter.getCursor();
        randomDetailAdapter.changeCursor(SQLiteHelper.getInstance(
                getActivity()).getRandoms(name_index));
        randomDetailAdapter.notifyDataSetChanged();
        c.close();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 当传感器精度改变时回调该方法，Do nothing.
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                float[] values = event.values;// values[0]:X轴，values[1]：Y轴，values[2]：Z轴
            /*
             * 因为一般正常情况下，任意轴数值最大就在9.8~10之间，只有在你突然摇动手机的时候，瞬时加速度才会突然增大或减少。
			 * 所以，经过实际测试，只需监听任一轴的加速度大于14的时候，改变你需要的设置就OK了~~~
			 */
                if ((Math.abs(values[0]) > 14 || Math.abs(values[1]) > 14 || Math
                        .abs(values[2]) > 14)) {// 摇动手机后
                    runRandom();
                }
                break;
            default:
                break;
        }
    }

    private void runRandom() {
        if (randomDetailAdapter.getCount() > 0 && !progressRandoming) {
            new MyAsyncTask().execute();
        }
    }

    boolean progressRandoming = false;

    public class MyAsyncTask extends AsyncTask<String, Integer, String> {
        private String randomResult = "";
        private ProgressDialog progressDialog;
        private AlertDialog dialog;

        /*
         * 这个方法的参数不能改,其返回类型与第三个参数一致，其参数与第一个参数类型一致 (non-Javadoc)
         *
         * @see android.os.AsyncTask#doInParams[])
         */
        @Override
        protected String doInBackground(String... params) {
            int count = 4;
            for (int i = 1; i <= count; i++) {
                publishProgress(i * (100 / count), 300);
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return ((Cursor) randomDetailAdapter.getItem(rand
                    .nextInt(randomDetailAdapter.getCount()))).getString(1);// 也作为下一个方法的参数
        }

        /*
         * 当异步结束时触发此方法，其参数类型与第三个参数类型一致 (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                publishProgress(100, 500);
                randomResult = result;
                progressDialog.dismiss();
                dialog.setMessage(randomResult);
                dialog.show();
            }
            super.onPostExecute(result);
        }

        /*
         * 当异步开始的时候触发 (non-Javadoc)
         *
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            progressRandoming = true;
            System.out.println("异步开始");
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(getActivity());
                progressDialog
                        .setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(false);
                progressDialog.setMax(100);
            }
            progressDialog.show();
            if (dialog == null) {
                dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(name)
                        .setPositiveButton(R.string.dialog_ok, dialogAction)
                                // .setNegativeButton(R.string.dialog_bad,
                                // dialogAction)
                        .setNeutralButton(R.string.dialog_good, dialogAction)
                        .create();
            }
            dialog.dismiss();
            super.onPreExecute();
        }

        /*
         * 正在处理的时候触发，与主UI线程交互，其参数与第二个参数一致 (non-Javadoc)
         *
         * @see android.os.AsyncTask#onProgressUpdate(Progress[])
         */
        @Override
        protected void onProgressUpdate(Integer... values) {// 第二个可变参数，由上面的publishProgress方法的参数决定
            progressDialog.setProgress(values[0]);
            if (getActivity()
                    .getSharedPreferences(Preferences.PREFS_NAME,
                            Preferences.MODE_PRIVATE)
                    .getBoolean(
                            getString(R.string.preference_vibrate_after_random),
                            true)) {
                vibrator.vibrate(values[1]);// 伴随震动提示~~
            }
            super.onProgressUpdate(values);
        }

        private DialogInterface.OnClickListener dialogAction = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dlg, int which) {// TODO
                switch (which) {
                    case Dialog.BUTTON_NEGATIVE:// Bad
                        break;
                    case Dialog.BUTTON_NEUTRAL:// Good
                        SQLiteHelper.getInstance(getActivity())
                                .insertRandom(name_index, randomResult);
                        refreshViews();
                        break;
                    case Dialog.BUTTON_POSITIVE:// Okay
                    default:
                        break;
                }
                progressRandoming = false;
            }
        };
    }

}
