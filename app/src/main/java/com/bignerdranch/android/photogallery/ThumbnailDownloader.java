package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by RBanks on 10/24/2016.
 */

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    private boolean m_hasQuit = false;
    private Handler m_requestHandler;
    //a thread safe version of hashmap...swag
    private ConcurrentMap<T, String> m_requestMap = new ConcurrentHashMap<>();
    private Handler m_responseHandler;
    private ThumbnailDownloadListener<T> m_thumbnailDownloadListener;
    private LruCache<String, Bitmap> cache;

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        m_thumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        m_responseHandler = responseHandler;
    }

    @Override
    protected void onLooperPrepared() {
        m_requestHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " + m_requestMap.get(target));
                    handleRequest(target);
                }
            }
        };
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() /1024);
        final int cacheSize = maxMemory / 6;
        cache = new LruCache<String, Bitmap>(cacheSize);
    }

    @Override
    public boolean quit() {
        m_hasQuit = true;
        return super.quit();
    }

    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "Got a URL: " + url);

        if (url == null) {
            m_requestMap.remove(target, url);
        } else {
            m_requestMap.put(target, url);
            m_requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    public void clearQueue() {
        m_requestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    private void handleRequest(final T target) {
        try {
            final String url = m_requestMap.get(target);

            if (url == null) {
                return;
            }
            Bitmap image = getBitmapFromCache(url);
            final Bitmap bitmap;

            if(image != null) {
                bitmap = image;
                Log.i(TAG, "Bitmap reloaded");
            } else {
                byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
                bitmap = BitmapFactory
                        .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                addBitmapToCache(url, bitmap);
                Log.i(TAG, "Bitmap created");
            }
                m_responseHandler.post(new Runnable() {
                    public void run() {
                        if (m_requestMap.get(target) != url ||
                                m_hasQuit) {
                            return;
                        }

                        m_requestMap.remove(target);
                        m_thumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                    }
                });

        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }

    public void addBitmapToCache(String key, Bitmap bitmap) {
        cache.put(key, bitmap);
    }

    public Bitmap getBitmapFromCache(String key) {
        return cache.get(key);
    }
}
