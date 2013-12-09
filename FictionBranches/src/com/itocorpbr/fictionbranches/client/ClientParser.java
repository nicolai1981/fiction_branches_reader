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

    public static boolean parseChapterRequestResult(String page, String html, ClientResult result) {
        if (html == null) {
            return false;
        }
        ChapterBatchOperation batch = Application.getChapterModel().getBatchOperation();

        Chapter chapter = new Chapter();
        result.mData = chapter;

        int currentIndex = 0;
        int endIndex = 0;

        chapter.mPage = page;

        // Get chapter title
        currentIndex = html.indexOf("<H1>", endIndex) + 4;
        endIndex = html.indexOf("</H1>", currentIndex);
        chapter.mTitle = html.substring(currentIndex, endIndex);
        currentIndex = endIndex;

        // Get chapter information
        currentIndex = html.indexOf("<br>", currentIndex) + 4;
        endIndex = html.indexOf("<HR>", currentIndex);

        String subTitle = html.substring(currentIndex, endIndex);
        if (subTitle.length() > 0) {
            // Get author
            int startContent = subTitle.indexOf(" submitted by ") + 14;
            int endContent = 0;
            if ((subTitle.charAt(startContent) == '<') && (subTitle.charAt(startContent + 1) == 'A')) {
                startContent = subTitle.indexOf(">") + 1;
                endContent = subTitle.indexOf("</", startContent);
            } else {
                endContent = subTitle.indexOf(" on ", startContent);
            }
            chapter.mAuthor = subTitle.substring(startContent, endContent);

            // Get chapter date
            startContent = subTitle.indexOf(" on ", startContent) + 4;
            endContent = subTitle.indexOf("<br>", startContent);
            try {
                chapter.mDate = sChapterDateFormatter.parse(subTitle.substring(startContent, endContent)).getTime();
            } catch (ParseException e) {
                chapter.mDate = 0; 
            }

            // Get parent chapter
            startContent = subTitle.indexOf("page=", startContent);
            if (startContent > 0) {
                startContent += 5;
                endContent = subTitle.indexOf("&", startContent);
                if (endContent < 0) {
                    endContent = subTitle.indexOf("\"", startContent);
                }
                chapter.mParent = subTitle.substring(startContent, endContent);
            } else {
                chapter.mParent = null;
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

        // Insert / update current chapter
        Chapter oldChapter = Application.getChapterModel().getChapter(page);
        if (oldChapter == null) {
            Application.getChapterModel().insertChapter(chapter);

            // Add BACK
            if (!page.equals("root")) {
                Chapter back = new Chapter();
                back.mTitle = "BACK";
                back.mParent = chapter.mPage;
                back.mPage = chapter.mParent;
                if (Application.getChapterModel().getChapter(chapter.mParent) == null) {
                    back.mRead = 0;
                } else {
                    back.mRead = 1;
                }
                Application.getChapterModel().insertChapter(back);
            }
        } else {
            // Add BACK
            if (oldChapter.mRead == 0) {
                Chapter back = new Chapter();
                back.mTitle = "BACK";
                back.mParent = chapter.mPage;
                back.mPage = chapter.mParent;
                Chapter parent = Application.getChapterModel().getChapter(chapter.mParent);
                if (parent == null) {
                    back.mRead = 0;
                } else {
                    back.mRead = 1;
                    back.mAuthor = parent.mTitle;
                }
                Application.getChapterModel().insertChapter(back);
            }

            if ((oldChapter.mRead == 0) && !oldChapter.mTitle.equals(chapter.mTitle)) {
                chapter.mTitle = oldChapter.mTitle + " (" + chapter.mTitle + ")";
            } else {
                chapter.mTitle = oldChapter.mTitle;
            }
            Application.getChapterModel().updateChapter(chapter);
        }

        result.mData = chapter;

        // Get chapter child
        currentIndex = endIndex + 4;
        endIndex = html.indexOf("</P>", currentIndex);
        String linkList = html.substring(currentIndex, endIndex);
        currentIndex = linkList.indexOf("page=");
        while (currentIndex > 0) {
            Chapter child = new Chapter();
            child.mParent = chapter.mPage;
            child.mRead = 0;

            // Get child page
            currentIndex += 5;
            endIndex = linkList.indexOf("\">", currentIndex);
            int temp = linkList.indexOf("&", currentIndex);
            if ((temp > 0) && (temp < endIndex)) {
                endIndex = temp;
            }
            child.mPage = linkList.substring(currentIndex, endIndex);

            // Get child title
            currentIndex = linkList.indexOf(">", endIndex) + 1;
            endIndex = linkList.indexOf("</A", currentIndex);
            child.mTitle = linkList.substring(currentIndex, endIndex);

            batch.insertUpdateChapter(child);

            currentIndex = linkList.indexOf("page=", endIndex);
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
