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
    public static final SimpleDateFormat sFormatter = new SimpleDateFormat("MMM dd yyyy", Locale.US);

    /** The view to show the ad. */
    private AdView adView;
    private TextView mTitle;
    private TextView mSubTitle;
    private TextView mContent;
    private ScrollView mContentScroll;
    private ListView mChapterList;
    private MyCursorAdapter mAdapter = null;
    private ProgressDialog mProgress = null;
    private boolean mIsDownload = false;

    private static Chapter sChapter = null;
    private String mPage = "root";

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
        mAdapter = new MyCursorAdapter();
        mChapterList.setAdapter(mAdapter);
        mChapterList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*
                Chapter chapter = mAdapter.getItem(position);
                if ("BACK".equals(chapter.mTitle) && (chapter.mRead == 1)) {
                    chapter = Application.getChapterModel().getChapter(chapter.mPage);
                }
                if (chapter.mRead == 0) {
                    if (HttpRequest.isOnline()) {
                        mPage = chapter.mPage;
                        mIsDownload = true;
                        if ("BACK".equals(chapter.mTitle)) {
                            chapter.mRead = 1;
                            Application.getChapterModel().updateBackChapter(chapter);
                            Application.getClient().getChapter(chapter.mPage, null, MainActivity.this);
                        } else {
                            Application.getClient().getChapter(chapter.mPage, chapter.mParent, MainActivity.this);
                        }
                    } else {
                        createDialogInternetConnection();
                    }
                } else {
                    sChapter = chapter;
                    mTitle.setText(sChapter.mTitle);
                    mSubTitle.setText("Author: " + sChapter.mAuthor + " - " + sFormatter.format(new Date(sChapter.mDate)));
                    mContent.setText(sChapter.mContent);
                    mPage = sChapter.mPage;

                    if (HttpRequest.isOnline()) {
                        Application.getClient().getChapter(sChapter.mPage, sChapter.mParent, null);
                    }
                }

                if (mContentScroll != null) {
                    mContentScroll.fullScroll(View.FOCUS_UP);
                }
                mChapterList.smoothScrollToPosition(0);

                getLoaderManager().restartLoader(0, null, MainActivity.this);
                Application.getAccountModel().setLastChapter(mPage);
                */
            }
        });

        mPage = Application.getAccountModel().getLastChapter();

        sChapter = Application.getChapterModel().getChapter(mPage);
        if ((sChapter == null) || (sChapter.mRead == 0)) {
            mIsDownload = true;
            if (sChapter == null) {
                Application.getClient().getChapter(mPage, null, this);
            } else {
                Application.getClient().getChapter(mPage, sChapter.mParent, this);
            }
        } else {
            mTitle.setText(sChapter.mTitle);
            mSubTitle.setText("Author: " + sChapter.mAuthor + " - " + sFormatter.format(new Date(sChapter.mDate)));
            mContent.setText(sChapter.mContent);
        }

        getLoaderManager().initLoader(0, null, this);
    }

    /** Called before the activity is destroyed. */
    @Override
    public void onDestroy() {
        mAdapter = null;
        getLoaderManager().destroyLoader(0);

        // Destroy the AdView.
        if (adView != null) {
            adView.destroy();
        }

        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (mProgress != null) {
            mProgress.dismiss();
            mProgress = null;
        }
        super.onPause();
    }

    @Override
    public void onHttpRequestFinished(ClientResult result) {
        if (mProgress != null) {
            mProgress.dismiss();
            mProgress = null;
            mIsDownload = false;
        }

        if (result.mData == null) {
            return;
        }

        switch (result.mReqId) {
        case GET_CHAPTER:
            sChapter = (Chapter) result.mData;
            mTitle.setText(sChapter.mTitle);
            mSubTitle.setText("Author: " + sChapter.mAuthor + " - " + sFormatter.format(new Date(sChapter.mDate)));
            mContent.setText(sChapter.mContent);
            break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ((mProgress != null) && !mIsDownload) {
            mProgress.dismiss();
        }
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Loading...");
        mProgress.show();

        return new MyLoader(Application.getContext(), mPage);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> paramLoader, Cursor cursor) {
        if ((mProgress != null) && !mIsDownload) {
            mProgress.dismiss();
            mProgress = null;
        }

        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> paramLoader) {
        if (mAdapter != null) {
            mAdapter.swapCursor(null);
        }
    }

    private static class MyLoader extends CursorLoader {
        private String mText = null;

        public MyLoader(Context context, String text) {
            super(context);
            mText = text;
        }

        public Cursor doQuery() {
            return Application.getChapterModel().getChildList(mText);
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
            Application.getAccountModel().setLastChapter("root");
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
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
}