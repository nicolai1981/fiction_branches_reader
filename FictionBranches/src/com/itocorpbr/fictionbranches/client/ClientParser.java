package com.itocorpbr.fictionbranches.client;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.text.Html;
import android.text.Spanned;

import com.itocorpbr.fictionbranches.Application;
import com.itocorpbr.fictionbranches.model.Chapter;
import com.itocorpbr.fictionbranches.model.ChapterModel.ChapterBatchOperation;
import com.itocorpbr.fictionbranches.model.LatestChapterModel.LatestChapterBatchOperation;

public class ClientParser {
    public static final String WEB_SERVER = "http://fb.countd.eu/cgi-bin/";
    public static final SimpleDateFormat sLatestDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    public static final SimpleDateFormat sChapterDateFormatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US);

    public static boolean parseChapterRequestResult(String page, String parent, String html, ClientResult result) {
        if (html == null) {
            return false;
        }
        ChapterBatchOperation batch = Application.getChapterModel().getBatchOperation();

        Chapter chapter = new Chapter();
        result.mData = chapter;

        int currentIndex = 0;
        int endIndex = 0;

        chapter.mPage = page;
        chapter.mParent = parent;

        // Get title
        currentIndex = html.indexOf("<H1>", endIndex) + 4;
        endIndex = html.indexOf("</H1>", currentIndex);
        chapter.mTitle = html.substring(currentIndex, endIndex);

        // Get subtitle
        currentIndex = html.indexOf("<br>", endIndex) + 4;
        endIndex = html.indexOf("<HR>", currentIndex);
        String subTitle = html.substring(currentIndex, endIndex);
        if (subTitle.length() > 0) {
            int startDate = subTitle.indexOf(" on ") + 4;
            int endDate = subTitle.indexOf("<br>", startDate);
            try {
                chapter.mDate = sChapterDateFormatter.parse(subTitle.substring(startDate, endDate)).getTime();
            } catch (ParseException e) {
                chapter.mDate = 0; 
            }

            int start = subTitle.indexOf("<A");
            int end = subTitle.indexOf("</A>");
            if ((start > 0) && (start < startDate)) {
                start = subTitle.indexOf(">", start);
                chapter.mAuthor = subTitle.substring(start + 1, end);
            } else {
                start = subTitle.indexOf("submitted by ") + 13;
                end = subTitle.indexOf(" on ", start);
                if (start > 0) {
                    chapter.mAuthor = subTitle.substring(start, end);
                }
            }

            // Get parent
            if ((parent == null) && !"root".equals(page)) {
                start = subTitle.indexOf("?page=", start) + 6;
                end = subTitle.indexOf("&", start);
                if ((start >0) && (end > 0)) {
                    chapter.mParent = subTitle.substring(start, end);

                    // Insert back button
                    Chapter back = new Chapter();
                    back.mPage = chapter.mParent;
                    back.mParent = page;
                    back.mTitle = "BACK";
                    back.mAuthor = " ";
                    back.mDate = -1;
                    if (Application.getChapterModel().getChapter(chapter.mParent) == null) {
                        back.mRead = 0;
                    } else {
                        back.mRead = 1;
                    }
                    batch.insertChapter(back);
                }
            }
        }

        // Get content
        currentIndex = endIndex + 4;
        endIndex = html.indexOf("<HR>", currentIndex);
        Spanned content = Html.fromHtml(html.substring(currentIndex, endIndex));
        chapter.mContent = content.toString().replace("\n\n", "\n\t").replace("\n\n", "\n").replace("\n\n", "\n");
        if ((chapter.mContent.length() > 0) && (chapter.mContent.charAt(0) == '\n')) {
            chapter.mContent = chapter.mContent.substring(1);
        }
        chapter.mRead = 1;

        if (Application.getChapterModel().getChapter(page) == null) {
            Application.getChapterModel().insertChapter(chapter);
        } else {
            Application.getChapterModel().updateChapter(chapter);
        }

        result.mData = chapter;

        // Get links
        while ((html.indexOf("<A href=\"storypage.pl?page=", endIndex) > 0) || (html.indexOf("<A href=\"fbstorypage.pl?page=", endIndex) > 0)) {
            Chapter child = new Chapter();
            currentIndex = html.indexOf("<A href=\"storypage.pl?page=", endIndex);
            if (currentIndex < 0) {
                currentIndex = html.indexOf("<A href=\"fbstorypage.pl?page=", endIndex) + 29;
            } else {
                currentIndex += 27;
            }
            endIndex = html.indexOf("\">", currentIndex);
            child.mPage = html.substring(currentIndex, endIndex);
            child.mParent = page;
            child.mRead = 0;

            currentIndex = endIndex + 2;
            endIndex = html.indexOf("</A>", currentIndex);
            child.mTitle = html.substring(currentIndex, endIndex);

            if (Application.getChapterModel().getChapter(child.mPage) == null) {
                batch.insertChapter(child);

                if (Application.getChapterModel().getBackChapter(child.mParent) == null) {
                    // Insert back button
                    Chapter back = new Chapter();
                    back.mPage = child.mParent;
                    back.mParent = child.mPage;
                    back.mTitle = "BACK";
                    back.mAuthor = " ";
                    back.mDate = -1;
                    back.mRead = 1;
                    batch.insertChapter(back);
                }
            }
        }
        batch.flush();

        return true;
    }

    public static boolean parseLatestChapterRequestResult(String html, ClientResult result) {
        if (html == null) {
            return false;
        }
        Chapter chapter = new Chapter();
        result.mData = chapter;

        int currentIndex = html.indexOf("</thead><tbody>");
        int endIndex = currentIndex + 15;

        // Get links
        LatestChapterBatchOperation batch = Application.getLatestChapterModel().getBatchOperation();

        while (html.indexOf("<tr>", endIndex) > 0) {
            currentIndex = html.indexOf("<tr>", endIndex) + 4;
            endIndex = html.indexOf("</tr>", currentIndex);

            String itemText = html.substring(currentIndex, endIndex);
            int itemCurrentIndex = 0;
            int itemEndIndex = 0;

            Chapter child = new Chapter();
            child.mRead = 0;

            // Get page
            itemCurrentIndex = itemText.indexOf("?page=", itemEndIndex) + 6;
            itemEndIndex = itemText.indexOf("&", itemCurrentIndex);
            child.mPage = itemText.substring(itemCurrentIndex, itemEndIndex);

            // Get title
            itemCurrentIndex = itemText.indexOf("\">", itemEndIndex) + 2;
            itemEndIndex = itemText.indexOf("</A>", itemCurrentIndex);
            child.mTitle = itemText.substring(itemCurrentIndex, itemEndIndex);

            // Get date
            itemCurrentIndex = itemText.indexOf("<font size=-1>", itemEndIndex) + 14;
            itemEndIndex = itemText.indexOf("</font>", itemCurrentIndex);
            try {
                child.mDate = sLatestDateFormatter.parse(itemText.substring(itemCurrentIndex, itemEndIndex)).getTime();
            } catch (ParseException e) {
                child.mDate = 0;
            }

            // Get author
            itemCurrentIndex = itemText.indexOf("<td>", itemEndIndex) + 4;
            itemEndIndex = itemText.indexOf("</td>", itemCurrentIndex);
            child.mAuthor = itemText.substring(itemCurrentIndex, itemEndIndex);

            batch.insertChapter(child);
        }
        batch.flush();

        return true;
    }
}
