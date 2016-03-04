package com.klinker.android.twitter.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.klinker.android.simple_videoview.SimpleVideoView;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.data.App;
import com.klinker.android.twitter.utils.ImageUtils;
import com.klinker.android.twitter.utils.api_helper.GiffyHelper;

import java.util.List;

import uk.co.senab.bitmapcache.BitmapLruCache;

public class GifSearchAdapter extends SectionedRecyclerViewAdapter<GifSearchAdapter.ViewHolder> {

    public interface Callback {
        void onClick(int item);
    }

    protected List<GiffyHelper.Gif> gifs;
    protected Callback callback;
    protected SimpleVideoView currentlyPlaying;

    private BitmapLruCache cache;

    public GifSearchAdapter(Context context, List<GiffyHelper.Gif> gifs, Callback callback) {
        this.gifs = gifs;
        this.callback = callback;
    }

    public void releaseVideo() {
        if (currentlyPlaying != null) {
            currentlyPlaying.release();
            currentlyPlaying.setVisibility(View.GONE);
        }
    }

    @Override
    public int getSectionCount() {
        return 1;
    }

    @Override
    public int getItemCount(int section) {
        return gifs.size();
    }

    @Override
    public void onBindHeaderViewHolder(ViewHolder holder, int section) {

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int section, final int relativePosition, final int absolutePosition) {

        Context context = holder.previewImage.getContext();

        if (cache == null) {
            cache = ((App)context.getApplicationContext()).getBitmapCache();
        }

        ImageUtils.loadImage(context, holder.previewImage, gifs.get(relativePosition).previewImage, cache);

        holder.previewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                releaseVideo();

                holder.video.setVisibility(View.VISIBLE);
                holder.video.start(gifs.get(relativePosition).mp4Url);
                currentlyPlaying = holder.video;
            }
        });
    }

    /**
     * Create the view holder object for the item
     *
     * @param parent   the recycler view parent
     * @param viewType VIEW_TYPE_HEADER or VIEW_TYPE_ITEM
     * @return ViewHolder to be used
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(viewType == VIEW_TYPE_HEADER ?
                        R.layout.adapter_item_gif_header :
                        R.layout.adapter_item_gif, parent, false);
        return new ViewHolder(v);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView previewImage;
        public SimpleVideoView video;

        public ViewHolder(View itemView) {
            super(itemView);
            previewImage = (ImageView) itemView.findViewById(R.id.image);
            video = (SimpleVideoView) itemView.findViewById(R.id.video);
        }
    }
}