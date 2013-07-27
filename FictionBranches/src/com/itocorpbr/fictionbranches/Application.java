package com.itocorpbr.fictionbranches;

import android.content.Context;

import com.itocorpbr.fictionbranches.client.Client;
import com.itocorpbr.fictionbranches.model.AccountModel;
import com.itocorpbr.fictionbranches.model.ChapterModel;
import com.itocorpbr.fictionbranches.model.LatestChapterModel;
import com.itocorpbr.fictionbranches.model.Model;

public class Application extends android.app.Application {

    /**
     * Stores the Application instance.
     */
    private static Application sAppContext;

    /**
     * Reference to ApplicationModel.
     */
    private Model mModel;

    /**
     * Reference to ClientAPI.
     */
    private Client mClient;

    /**
     * Application instance constructor.
     */
    public Application() {
        sAppContext = this;
    }

    public static Context getContext() {
        return sAppContext;
    }

    /**
     * Returns the Application Model.
     * 
     * @return the Application Model.
     */
    private static synchronized Model getModel() {
        if (sAppContext.mModel == null) {
            sAppContext.mModel = new Model(sAppContext);
        }

        return sAppContext.mModel;
    }

    /**
     * Returns the Client.
     * 
     * @return the Client.
     */
    public static synchronized Client getClient() {
        if (sAppContext.mClient == null) {
            sAppContext.mClient = new Client(sAppContext);
        }
        return sAppContext.mClient;
    }

    public static ChapterModel getChapterModel() {
        return getModel().getChapterModel();
    }

    public static LatestChapterModel getLatestChapterModel() {
        return getModel().getLatestChapterModel();
    }

    public static AccountModel getAccountModel() {
        return getModel().getAccountModel();
    }
}
