package com.itocorpbr.fictionbranches.view;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.itocorpbr.fictionbranches.Application;
import com.itocorpbr.fictionbranches.R;
import com.itocorpbr.fictionbranches.common.model.CursorLoader;
import com.itocorpbr.fictionbranches.model.Chapter;
import com.itocorpbr.fictionbranches.model.ChapterModel;

public class UnreadListActivity extends Activity implements LoaderCallbacks<Cursor> {
    public static final SimpleDateFormat sFormatter = new SimpleDateFormat("MMM dd yyyy", Locale.US);

    /** The view to show the ad. */
    private ListView mChapterList;
    private MyCursorAdapter mAdapter = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        mChapterList = (ListView) findViewById(R.id.childList);
        mAdapter = new MyCursorAdapter();
        mChapterList.setAdapter(mAdapter);
        mChapterList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Chapter chapter = mAdapter.getItem(position);
                Application.getAccountModel().setLastChapter(chapter.mPage);
                Intent intent = new Intent(UnreadListActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        TextView title = (TextView) findViewById(R.id.chapterTitle);
        title.setText("Unread chapters");

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /** Called before the activity is destroyed. */
    @Override
    public void onDestroy() {
        mAdapter = null;
        getLoaderManager().destroyLoader(0);
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MyLoader(Application.getContext());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> paramLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> paramLoader) {
        if (mAdapter != null) {
            mAdapter.swapCursor(null);
        }
    }

    private static class MyLoader extends CursorLoader {
        public MyLoader(Context context) {
            super(context);
        }

        public Cursor doQuery() {
            return Application.getChapterModel().getUnreadList();
        }
    }

    private static class MyCursorAdapter extends CursorAdapter {
        public MyCursorAdapter() {
            super(Application.getContext(), null, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.list_item, viewGroup, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mTitle = (TextView) view.findViewById(R.id.titleText);
            viewHolder.mReadChapter = (ImageView) view.findViewById(R.id.readChapter);
            view.setTag(viewHolder);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Chapter chapter = ChapterModel.toChapter(cursor);
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.mTitle.setText(chapter.mTitle);
            holder.mReadChapter.setVisibility(View.INVISIBLE);
        }

        @Override
        public Chapter getItem(int position) {
            Cursor cursor = (Cursor) super.getItem(position);
            if (cursor != null) {
                return ChapterModel.toChapter(cursor);
            } else {
                return null;
            }
        }
    }

    private static class ViewHolder {
        TextView mTitle;
        ImageView mReadChapter;
    }
}