package com.bignerdranch.android.photogallery;

import android.app.DownloadManager;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RBanks on 10/10/2016.
 */
public class PhotoGalleryFragment extends VisibleFragment {

    private RecyclerView m_photoRecyclerView;
    private static final String TAG = "PhotoGalleryFragment";
    private List<GalleryItem.GalleryList.Photo> m_items = new ArrayList<>();

    private ThumbnailDownloader<PhotoHolder> m_thumbnailDownloader;
    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    public PhotoGalleryFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        updateItems();
        setHasOptionsMenu(true);

        //by default the handler will attach itself to the looper
        Handler responseHandler = new Handler();
        m_thumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        m_thumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>(){
                    @Override
                    public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap bitmap) {
                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                        photoHolder.bindDrawable(drawable);
                    }
                }
        );
        m_thumbnailDownloader.start();
        m_thumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");
    }
    //https//api.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key=947f4a12ce7402a03e2c638539fd151a&format=json&nojsoncallback=1
    //947f4a12ce7402a03e2c638539fd151a

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_photo_gallery, container, false);

        m_photoRecyclerView = (RecyclerView) v
                .findViewById(R.id.fragment_photo_gallery_recycler_view);
        // we setup the recycler view with a grid of 3 columns...grid manager is made specifically for recycler view layouts
        m_photoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        setupAdapter();
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "QueryTextSubmit: " + query);
                QueryPreferences.setStoredQuery(getActivity(), query);
                updateItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "QueryTextChange: " + newText);
                return false;
            }
        });

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if(PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_clear:
                //sets the query to null
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                //this will tell the activity to update it's options menu
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(query).execute();

        hideSoftKeyboard();
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        View view = getActivity().getCurrentFocus();
        if (view == null)
            view = new View(getActivity());
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        m_thumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        m_thumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    private void setupAdapter() {
        //is added checks to see if the fragment is connected to an activity
        if (isAdded()) {
            m_photoRecyclerView.setAdapter(new PhotoAdapter(m_items));
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private ImageView m_photoView;
        private GalleryItem.GalleryList.Photo m_galleryItem;

        public PhotoHolder(View itemView) {
            super(itemView);
            m_photoView = (ImageView) itemView.findViewById(R.id.fragment_photo_gallery_image_view);
            itemView.setOnClickListener(this);
        }

        public void bindDrawable(Drawable drawable) {
            m_photoView.setImageDrawable(drawable);
        }

        public void bindGalleryItem(GalleryItem.GalleryList.Photo galleryItem) {
            m_galleryItem = galleryItem;
        }

        @Override
        public void onClick(View view) {
            Intent i = PhotoPageActivity.newIntent(getActivity(), m_galleryItem.getPhotoPageUri());
            startActivity(i);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<GalleryItem.GalleryList.Photo> m_galleryItems;
        public PhotoAdapter(List<GalleryItem.GalleryList.Photo> galleryItems) {
            m_galleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem.GalleryList.Photo galleryItem = m_galleryItems.get(position);

            if(galleryItem.getUrl() != null) {
                Log.i("URL", galleryItem.getUrl());
                holder.bindGalleryItem(galleryItem);
                Drawable placeHolder = getResources().getDrawable(R.drawable.hold);
                holder.bindDrawable(placeHolder);
                m_thumbnailDownloader.queueThumbnail(holder, galleryItem.getUrl());
            }
        }

        @Override
        public int getItemCount() {
            return m_galleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem.GalleryList.Photo>> {
        private String m_query;

        public FetchItemsTask(String query) {
            m_query = query;
        }

        @Override
        protected List<GalleryItem.GalleryList.Photo> doInBackground(Void... params) {

            if(m_query == null) {
                return new FlickrFetchr().fetchRecentPhotos();
            } else {
                return new FlickrFetchr().searchPhotos(m_query);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem.GalleryList.Photo> galleryItems) {
            m_items = galleryItems;
            setupAdapter();
        }
    }
}
