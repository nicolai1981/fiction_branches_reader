package com.itocorpbr.fictionbranches.common.command;

import android.content.Context;
import android.os.AsyncTask;


public abstract class AsyncCommand {

    protected final Context mContext;
    private CommandListener mListener;
    protected Object mData = null;

    public AsyncCommand(Context context, CommandListener listener) {
        mContext = context.getApplicationContext();
        mListener = listener;
    }

    public final void start() {
        new MyRequest().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void)null);
    }

    protected void onPostExecute(boolean result) {
        if (mListener != null) {
            mListener.onCommandFinished(result, mData);
        }
    }

    public void setListener(CommandListener listener) {
        mListener = listener;
    }

    protected boolean execute() {
        return true;
    }

    private class MyRequest extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return AsyncCommand.this.execute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            AsyncCommand.this.onPostExecute(result);
        }
    }

    public interface CommandListener {
        void onCommandFinished(boolean result, Object data);
    }
}
