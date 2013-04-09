
package com.yyxu.download.services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.yyxu.download.error.DownloadThrowable;
import com.yyxu.download.error.ErrorInfo;
import com.yyxu.download.error.FileAlreadyExistException;
import com.yyxu.download.error.NoMemoryException;
import com.yyxu.download.http.AndroidHttpClient;
import com.yyxu.download.utils.NetworkUtils;
import com.yyxu.download.utils.StorageUtils;

public class DownloadTask extends AsyncTask<Void, Integer, Long> {

    public final static int TIME_OUT = 30000;
    private final static int BUFFER_SIZE = 1024 * 8;

    private static final String TAG = "DownloadTask";
    private static final boolean DEBUG = true;
    private static final String TEMP_SUFFIX = StorageUtils.TEMP_SUFFIX;
	public static final String ERROR_BLOCK_INTERNET = "1000";
	public static final String ERROR_UNKOWN_HOST = "1001";
	public static final String ERROR_UNKONW = "1002";
	public static final String ERROR_FILE_EXIST = "1003";
	public static final String ERROR_SD_NO_MEMORY = "1004";
	public static final String ERROR_TIME_OUT = "1005";
	private static final String ERROR_IOE = "1006";

    private URL URL;
    private File file;
    private File tempFile;
    private String url;
    private RandomAccessFile outputStream;
    private DownloadTaskListener listener;
    private Context context;

    private long downloadSize;
    private long previousFileSize;
    private long totalSize;
    private long downloadPercent;
    private long networkSpeed;
    private long previousTime;
    private long totalTime;
    private DownloadThrowable error = null;
    private boolean interrupt = false;

    private final class ProgressReportingRandomAccessFile extends RandomAccessFile {
        private int progress = 0;

        public ProgressReportingRandomAccessFile(File file, String mode)
                throws FileNotFoundException {

            super(file, mode);
        }

        @Override
        public void write(byte[] buffer, int offset, int count) throws IOException {

            super.write(buffer, offset, count);
            progress += count;
            publishProgress(progress);
        }
    }

    public DownloadTask(Context context, String url, String path) throws MalformedURLException {

        this(context, url, path, null);
    }

    public DownloadTask(Context context, String url, String path, DownloadTaskListener listener)
            throws MalformedURLException {

        super();
        this.url = url;
        this.URL = new URL(url);
        this.listener = listener;
        String fileName = new File(URL.getFile()).getName();
        this.file = new File(path, fileName);
        this.tempFile = new File(path, fileName + TEMP_SUFFIX);
        this.context = context;
    }

    public String getUrl() {

        return url;
    }

    public boolean isInterrupt() {

        return interrupt;
    }

    public long getDownloadPercent() {

        return downloadPercent;
    }

    public long getDownloadSize() {

        return downloadSize + previousFileSize;
    }

    public long getTotalSize() {

        return totalSize;
    }

    public long getDownloadSpeed() {

        return this.networkSpeed;
    }

    public long getTotalTime() {

        return this.totalTime;
    }
    
    public File getFile(){
		return file;
    }

    public DownloadTaskListener getListener() {

        return this.listener;
    }

    @Override
    protected void onPreExecute() {

        previousTime = System.currentTimeMillis();
        if (listener != null)
            listener.preDownload(this);
    }

    @Override
    protected Long doInBackground(Void... params) {

        long result = -1;
        try {
            result = download();
        } catch (NetworkErrorException e) {
            error = new DownloadThrowable(new ErrorInfo(ERROR_BLOCK_INTERNET, "NetworkErrorException", e.getMessage()));
        }catch (ConnectTimeoutException e) {
			// TODO: handle exception
        	error = new DownloadThrowable(new ErrorInfo(ERROR_TIME_OUT, "ConnectTimeoutException", e.getMessage()));;
		} 
        catch (FileAlreadyExistException e) {
            error = new DownloadThrowable(new ErrorInfo(ERROR_FILE_EXIST, "FileAlreadyExistException", e.getMessage()));;
        } catch (NoMemoryException e) {
            error = new DownloadThrowable(new ErrorInfo(ERROR_SD_NO_MEMORY, "NoMemoryException", e.getMessage()));;
        } catch (IOException e) {
            error = new DownloadThrowable(new ErrorInfo(ERROR_IOE, "IOException", e.getMessage()));;
        } finally {
            if (client != null) {
                client.close();
            }
        }

        return result;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {

        if (progress.length > 1) {
            totalSize = progress[1];
            if (totalSize == -1) {
                if (listener != null)
                    listener.errorDownload(this, error);
            } else {

            }
        } else {
            totalTime = System.currentTimeMillis() - previousTime;
            downloadSize = progress[0];
            downloadPercent = (downloadSize + previousFileSize) * 100 / totalSize;
            networkSpeed = downloadSize / totalTime;
            if (listener != null)
                listener.updateProcess(this);
        }
    }

    @Override
    protected void onPostExecute(Long result) {

        if (result == -1 || interrupt || error != null) {
            if (DEBUG && error != null) {
                Log.v(TAG, "Download failed." + error.getMessage());
            }
            if (listener != null) {
                listener.errorDownload(this, error);
            }
            return;
        }
        // finish download
        tempFile.renameTo(file);
        if (listener != null)
            listener.finishDownload(this);
    }

    @Override
    public void onCancelled() {

        super.onCancelled();
        interrupt = true;
    }

    private AndroidHttpClient client;
    private HttpGet httpGet;
    private HttpResponse response;

    private long download() throws NetworkErrorException,ConnectTimeoutException, IOException, FileAlreadyExistException,
            NoMemoryException {

        if (DEBUG) {
            Log.v(TAG, "totalSize: " + totalSize);
        }

        /*
         * check net work
         */
        if (!NetworkUtils.isNetworkAvailable(context)) {
            throw new NetworkErrorException("Network blocked.");
        }

        /*
         * check file length
         */
        client = AndroidHttpClient.newInstance("DownloadTask");
        httpGet = new HttpGet(url);
        response = client.execute(httpGet);
        totalSize = response.getEntity().getContentLength();

        if (file.exists() && totalSize == file.length()) {
            if (DEBUG) {
                Log.v(null, "Output file already exists. Skipping download.");
            }

            throw new FileAlreadyExistException("Output file already exists. Skipping download.");
        } else if (tempFile.exists()) {
            httpGet.addHeader("Range", "bytes=" + tempFile.length() + "-");
            previousFileSize = tempFile.length();

            client.close();
            client = AndroidHttpClient.newInstance("DownloadTask");
            response = client.execute(httpGet);

            if (DEBUG) {
                Log.v(TAG, "File is not complete, download now.");
                Log.v(TAG, "File length:" + tempFile.length() + " totalSize:" + totalSize);
            }
        }

        /*
         * check memory
         */
        long storage = StorageUtils.getAvailableStorage();
        if (DEBUG) {
            Log.i(null, "storage:" + storage + " totalSize:" + totalSize);
        }

        if (totalSize - tempFile.length() > storage) {
            throw new NoMemoryException("SD card no memory.");
        }

        /*
         * start download
         */
        outputStream = new ProgressReportingRandomAccessFile(tempFile, "rw");

        publishProgress(0, (int) totalSize);

        InputStream input = response.getEntity().getContent();
        int bytesCopied = copy(input, outputStream);

        if ((previousFileSize + bytesCopied) != totalSize && totalSize != -1 && !interrupt) {
            throw new IOException("Download incomplete: " + bytesCopied + " != " + totalSize);
        }

        if (DEBUG) {
            Log.v(TAG, "Download completed successfully.");
        }

        return bytesCopied;

    }

    public int copy(InputStream input, RandomAccessFile out) throws ConnectTimeoutException,
            NetworkErrorException {

        if (input == null || out == null) {
            return -1;
        }

        byte[] buffer = new byte[BUFFER_SIZE];

        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        if (DEBUG) {
            try {
				Log.v(TAG, "length" + out.length());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        int count = 0, n = 0;
        long errorBlockTimePreviousTime = -1, expireTime = 0;

        try {

            try {
				out.seek(out.length());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            while (!interrupt) {
                try {
					n = in.read(buffer, 0, BUFFER_SIZE);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                if (n == -1) {
                    break;
                }
                try {
					out.write(buffer, 0, n);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                count += n;

                /*
                 * check network
                 */
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    throw new NetworkErrorException("Network blocked.");
                }

                if (networkSpeed == 0) {
                    if (errorBlockTimePreviousTime > 0) {
                        expireTime = System.currentTimeMillis() - errorBlockTimePreviousTime;
                        if (expireTime > TIME_OUT) {
                            throw new ConnectTimeoutException("connection time out.");
                        }
                    } else {
                        errorBlockTimePreviousTime = System.currentTimeMillis();
                    }
                } else {
                    expireTime = 0;
                    errorBlockTimePreviousTime = -1;
                }
            }
        } finally {
            client.close(); // must close client first
            client = null;
            try {
				out.close();
				in.close();
				input.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        return count;

    }

//	public static int getErrorCode(Throwable error2) {
//		// TODO Auto-generated method stub
//		String count=ERROR_UNKONW ;
//		if (error2 instanceof FileAlreadyExistException) {
//			count = ERROR_FILE_EXIST;
//		}else if (error2 instanceof NoMemoryException) {
//			count = ERROR_SD_NO_MEMORY;
//			
//		}else if (error2 instanceof NetworkErrorException) {
//			count = ERROR_BLOCK_INTERNET;
//
//		}if (error2 instanceof ConnectTimeoutException) {
//			count = ERROR_TIME_OUT;
//		}else if (error2 instanceof IOException) {
//			count = ERROR_IOE;
//			
//		}else {
//			count = ERROR_UNKONW;
//		}
//		Log.d(" count ", msg)
//		
//		return count;
//	}

//	public static String getErrorInfo(Throwable error2) {
//		// TODO Auto-generated method stub
//		if (error2==null) {
//			return null;
//		}
//		return error2.getMessage();
//	}



}
