package de.trac.spherical;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

public class BroadcastHelper {

    public enum BroadcastType {
        PROGRESS_START,
        PROGRESS_FINISHED
    }

    private static final String INTENT_ACTION = "de.spherical.internal";
    private static final String INTENT_KEY_NAME = "broadcast_type";
    public static final IntentFilter INTENT_FILTER = new IntentFilter(INTENT_ACTION); // TODO: useful?

    static public void broadcast(Context context, BroadcastType type) {
        Intent intent = new Intent(INTENT_ACTION);
        intent.putExtra(INTENT_KEY_NAME, type.name());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    static public BroadcastType getBroadcastType(Intent intent) {
        if(!INTENT_ACTION.equals(intent.getAction()))
            throw new IllegalArgumentException("Not a valid intent");

        return (BroadcastType) intent.getSerializableExtra(INTENT_KEY_NAME);
    }
}
