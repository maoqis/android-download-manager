
package com.yyxu.download.services;

import com.yyxu.download.error.DownloadThrowable;

public interface DownloadTaskListener {

    public void updateProcess(DownloadTask task);

    public void finishDownload(DownloadTask task);

    public void preDownload(DownloadTask task);

    public void errorDownload(DownloadTask task, DownloadThrowable error);
}
