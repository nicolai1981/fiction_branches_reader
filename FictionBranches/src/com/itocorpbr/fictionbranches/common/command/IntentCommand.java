package com.itocorpbr.fictionbranches.common.command;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

public abstract class IntentCommand implements Parcelable {

    private static final String INTENT_EXTRA_COMMAND = "command";

    public IntentCommand() {
    }

    public void start(Context context) {
        Intent intent = createIntent(context);
        intent.putExtra(INTENT_EXTRA_COMMAND, this);
        context.startService(intent);
    }

    public static void executeFromIntent(Context context, Intent intent) {
        if (intent != null) {
            IntentCommand command = intent.getParcelableExtra(INTENT_EXTRA_COMMAND);
            if (command != null) {
                command.execute(context);
            }
        }
    }

    protected abstract void execute(Context context);

    protected abstract Intent createIntent(Context context);

    @Override
    public int describeContents() {
        return 0;
    }
}
