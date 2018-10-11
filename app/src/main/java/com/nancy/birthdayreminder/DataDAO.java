package com.nancy.birthdayreminder;

import android.app.Activity;
import android.database.Cursor;

import java.util.List;

public class DataDAO implements DataAccessor{


    private Activity mActivity;

    public DataDAO(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void requestDetailItems(final DataReceiver receiver) {

        DataLoaderCallbacks callbacks = new DataLoaderCallbacks(mActivity);

        callbacks.setCursorHandler(new DataLoaderCallbacks.CursorHandler() {
            @Override
            public void handleCursor(ContactDetails contact) {
                // call a helper class to iterate through the cursor and
                // build a collection of JSONObjects that represent database records
                ContactDetails itemList = contact;
                receiver.receiveDetailItems(itemList);
            }
        });

        // derive an identifier from the LoaderCallbacks class name
        int callbacksId = callbacks.getClass().getSimpleName().hashCode();

        // start the loading sequence
        mActivity.getLoaderManager().initLoader(1, null, callbacks);
    }
}
