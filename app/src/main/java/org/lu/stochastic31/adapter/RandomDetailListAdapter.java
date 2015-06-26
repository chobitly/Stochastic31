package org.lu.stochastic31.adapter;

import org.lu.stochastic31.R;

import android.content.Context;
import android.database.Cursor;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RandomDetailListAdapter extends CursorAdapter {

	public RandomDetailListAdapter(Context context, Cursor c,
			boolean autoRequery) {
		super(context, c, autoRequery);
	}

//	public RandomDetailListAdapter(Context context, Cursor c, int flags) {
//		super(context, c, flags);
//	}

	@Override
	public void bindView(View v, Context context, Cursor c) {
		if (v == null) {
			throw new IllegalStateException(
					"the View should not be null when calling RandomDetailListAdapter.bindView()!");
		}
		TextView tv = (TextView) v;
		tv.setText(c.getString(1));
	}

	@Override
	public View newView(Context context, Cursor c, ViewGroup viewGroup) {
		return LayoutInflater.from(context).inflate(
				R.layout.random_detail_list_item, viewGroup, false);
	}

}
