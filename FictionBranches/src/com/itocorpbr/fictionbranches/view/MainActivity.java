package com.itocorpbr.fictionbranches.view;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.itocorpbr.fictionbranches.Application;
import com.itocorpbr.fictionbranches.R;
import com.itocorpbr.fictionbranches.client.ClientResult;
import com.itocorpbr.fictionbranches.common.model.CursorLoader;
import com.itocorpbr.fictionbranches.common.request.http.HttpRequest;
import com.itocorpbr.fictionbranches.common.request.http.HttpRequest.HttpRequestListener;
import com.itocorpbr.fictionbranches.model.Chapter;
import com.itocorpbr.fictionbranches.model.ChapterModel;

public class MainActivity extends Activity implements HttpRequestListener, LoaderCallbacks<Cursor> {
    private static final int CHAPTER_HEADER_LOADER_ID = 0;
    private static final int CHAPTER_CONTENT_LOADER_ID = 1;
    private static final int CHAPTER_CHILD_LOADER_ID = 2;

    private static final SimpleDateFormat sFormatter = new SimpleDateFormat("MMM dd yyyy", Locale.US);

    /** The view to show the ad. */
    private AdView adView;
    private TextView mTitle;
    private TextView mSubTitle;
    private TextView mContent;
    private ScrollView mContentScroll;
    private ListView mChapterList;
    private ChapterAdapter mAdapter = null;
    private ProgressDialog mProgress = null;
    private boolean mProcessHttpRequest = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Create an ad.
        adView = (AdView) findViewById(R.id.ad);
        AdRequest adRequest = new AdRequest();
        adView.loadAd(adRequest);

        mTitle = (TextView) findViewById(R.id.titleText);
        mSubTitle = (TextView) findViewById(R.id.subtitleText);
        mContent = (TextView) findViewById(R.id.descriptionText);

        mContentScroll = (ScrollView) findViewById(R.id.contentContainer);

        mChapterList = (ListView) findViewById(R.id.childList);
        mAdapter = new ChapterAdapter();
        mChapterList.setAdapter(mAdapter);
        mChapterList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startProgress();

                Chapter chapter = mAdapter.getItem(position);
                if (chapter.mRead == 0) {
                    if (HttpRequest.isOnline()) {
                        mProcessHttpRequest = true;
                        Application.getClient().getChapter(chapter.mPage, MainActivity.this);
                    } else {
                        createDialogInternetConnection();
                    }
                } else {
                    updateScreen(chapter);
                    if (HttpRequest.isOnline()) {
                        Application.getClient().getChapter(chapter.mPage, null);
                    }
                }
            }
        });

        String lastPage = Application.getAccountModel().getLastChapter();
        Chapter chapter = Application.getChapterModel().getChapter(lastPage);
        if ((chapter == null) || (chapter.mRead == 0)) {
            if (HttpRequest.isOnline()) {
                mProcessHttpRequest = true;
                Application.getClient().getChapter(lastPage, this);
            } else {
                createDialogInternetConnection();
            }
        } else {
            if (HttpRequest.isOnline()) {
                Application.getClient().getChapter(lastPage, null);
            }
        }

        startProgress();
        getLoaderManager().initLoader(CHAPTER_HEADER_LOADER_ID, null, this);
        getLoaderManager().initLoader(CHAPTER_CONTENT_LOADER_ID, null, this);
        getLoaderManager().initLoader(CHAPTER_CHILD_LOADER_ID, null, this);
    }

    /** Called before the activity is destroyed. */
    @Override
    public void onDestroy() {
        mAdapter = null;
        getLoaderManager().destroyLoader(CHAPTER_HEADER_LOADER_ID);
        getLoaderManager().destroyLoader(CHAPTER_CONTENT_LOADER_ID);
        getLoaderManager().destroyLoader(CHAPTER_CHILD_LOADER_ID);

        // Destroy the AdView.
        if (adView != null) {
            adView.destroy();
        }

        super.onDestroy();
    }

    @Override
    public void onPause() {
        endProgress();
        super.onPause();
    }

    @Override
    public void onHttpRequestFinished(ClientResult result) {
        endProgress();
        mProcessHttpRequest = false;

        if (result.mData == null) {
            return;
        }

        switch (result.mReqId) {
        case GET_CHAPTER:
            updateScreen((Chapter) result.mData);
            break;
        default:
            break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
        case CHAPTER_HEADER_LOADER_ID:
            return new ChapterHeaderLoader(Application.getContext());
        case CHAPTER_CONTENT_LOADER_ID:
            return new ChapterContentLoader(Application.getContext());
        case CHAPTER_CHILD_LOADER_ID:
            return new ChapterChildLoader(Application.getContext());
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> paramLoader, Cursor cursor) {
        switch (paramLoader.getId()) {
        case CHAPTER_HEADER_LOADER_ID:
            if (cursor.moveToFirst()) {
                Chapter chapter = ChapterModel.toChapter(cursor);
                mTitle.setText(chapter.mTitle);
                mSubTitle.setText("Author: " + chapter.mAuthor + " - " + sFormatter.format(new Date(chapter.mDate)));
            }
            cursor.close();
            break;
        case CHAPTER_CONTENT_LOADER_ID:
            if (cursor.moveToFirst()) {
                Chapter chapter = ChapterModel.toChapter(cursor);
                mContent.setText(chapter.mContent);
            }
            cursor.close();
            break;
        case CHAPTER_CHILD_LOADER_ID:
            if (!mProcessHttpRequest) {
                endProgress();
            }
            mAdapter.swapCursor(cursor);
            break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> paramLoader) {
        switch (paramLoader.getId()) {
        case CHAPTER_HEADER_LOADER_ID:
            mTitle.setText(null);
            mSubTitle.setText(null);
            mContent.setText(null);
            break;
        case CHAPTER_CONTENT_LOADER_ID:
            mContent.setText(null);
            break;
        case CHAPTER_CHILD_LOADER_ID:
            if (mAdapter != null) {
                mAdapter.swapCursor(null);
            }
            break;
        }
    }

    private static class ChapterHeaderLoader extends CursorLoader {
        public ChapterHeaderLoader(Context context) {
            super(context);
        }

        public Cursor doQuery() {
            String page = Application.getAccountModel().getLastChapter();
            return Application.getChapterModel().getChapterCursor(page);
        }
    }

    private static class ChapterContentLoader extends CursorLoader {
        public ChapterContentLoader(Context context) {
            super(context);
        }

        public Cursor doQuery() {
            String page = Application.getAccountModel().getLastChapter();
            return Application.getChapterModel().getChapterCursor(page);
        }
    }

    private static class ChapterChildLoader extends CursorLoader {
        public ChapterChildLoader(Context context) {
            super(context);
        }

        public Cursor doQuery() {
            String parent = Application.getAccountModel().getLastChapter();
            return Application.getChapterModel().getChildList(parent);
        }
    }

    private static class ChapterAdapter extends CursorAdapter {
        public ChapterAdapter() {
            super(Application.getContext(), null, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.list_item, viewGroup, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mTitle = (TextView) view.findViewById(R.id.titleText);
            viewHolder.mSubtitle = (TextView) view.findViewById(R.id.subtitleText);
            viewHolder.mReadChapter = (ImageView) view.findViewById(R.id.readChapter);
            view.setTag(viewHolder);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Chapter chapter = ChapterModel.toChapter(cursor);
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.mTitle.setText(chapter.mTitle);
            holder.mSubtitle.setText(chapter.mAuthor);
            if (chapter.mRead == 1) {
                holder.mReadChapter.setVisibility(View.INVISIBLE);
            } else {
                holder.mReadChapter.setVisibility(View.VISIBLE);
            }
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
        TextView mSubtitle;
        ImageView mReadChapter;
    }

    private void createDialogInternetConnection() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 
        // set title
        alertDialogBuilder.setTitle("Internet connection");
 
        // set dialog message
        alertDialogBuilder.setMessage("Enable your internet connection to proceed.");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("Settings",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                dialog.cancel();
            }
        });
 
        alertDialogBuilder.create().show();
    }

    private void createDialogLatest() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 
        alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
        alertDialogBuilder.setTitle("LATEST CHAPTERS");
        alertDialogBuilder.setView(createFloorShareView());
        alertDialogBuilder.setPositiveButton("OK", new OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mEditText1 != null) {
                    Intent intent = new Intent(MainActivity.this, LatestListActivity.class);
                    intent.putExtra(LatestListActivity.QTD_TAG, Integer.parseInt(mEditText1.getText().toString()));
                    startActivity(intent);
                    finish();
                }
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", null);
        alertDialogBuilder.create().show();
    }

    private EditText mEditText1 = null;
    private View createFloorShareView() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog, null);
        mEditText1 = (EditText)view.findViewById(R.id.dialog_input);
        return view;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
        case R.id.main_chapter:
            startProgress();
            updateScreen(Application.getChapterModel().getChapter("root"));
            if (HttpRequest.isOnline()) {
                Application.getClient().getChapter("root", null);
            }
           break;
        case R.id.unread_chapters:
            intent = new Intent(this, UnreadListActivity.class);
            startActivity(intent);
            finish();
            break;
        case R.id.latest_chapters:
            createDialogLatest();
            break;
        }
        return true;
    }

    private void startProgress() {
        endProgress();
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Loading...");
        mProgress.show();
    }

    private void endProgress() {
        if (mProgress != null) {
            mProgress.dismiss();
            mProgress = null;
        }

        if (mContentScroll != null) {
            mContentScroll.fullScroll(View.FOCUS_UP);
        }
        mChapterList.smoothScrollToPosition(0);
    }

    private void updateScreen(Chapter chapter) {
        Chapter nextChapter = chapter;
        if (chapter.mTitle.equals("BACK")) {
            nextChapter = Application.getChapterModel().getChapter(chapter.mPage);
        }

        Application.getAccountModel().setLastChapter(nextChapter.mPage);
        getLoaderManager().restartLoader(CHAPTER_HEADER_LOADER_ID, null, MainActivity.this);
        getLoaderManager().restartLoader(CHAPTER_CONTENT_LOADER_ID, null, MainActivity.this);
        getLoaderManager().restartLoader(CHAPTER_CHILD_LOADER_ID, null, MainActivity.this);
    }

    @Override
    public void onBackPressed() {
        if (mProgress != null) {
            return;
        }

        String currentPage = Application.getAccountModel().getLastChapter();
        if (currentPage.equals("root")) {
            super.onBackPressed();
        }

        startProgress();

        Chapter chapter = Application.getChapterModel().getChapter(currentPage);
        Chapter parentChapter = Application.getChapterModel().getChapter(chapter.mParent);
        if ((parentChapter == null) || (parentChapter.mRead == 0)) {
            if (HttpRequest.isOnline()) {
                mProcessHttpRequest = true;
                if (parentChapter == null) {
                    Application.getClient().getChapter(chapter.mParent, MainActivity.this);
                } else {
                    Application.getClient().getChapter(parentChapter.mPage, MainActivity.this);
                }
            } else {
                createDialogInternetConnection();
            }
       } else {
           updateScreen(parentChapter);
           if (HttpRequest.isOnline()) {
               Application.getClient().getChapter(parentChapter.mPage, null);
           }
        }
    }
}