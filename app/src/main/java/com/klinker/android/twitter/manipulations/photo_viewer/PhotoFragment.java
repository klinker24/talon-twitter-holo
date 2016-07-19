package com.klinker.android.twitter.manipulations.photo_viewer;

import android.app.Activity;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.manipulations.widgets.NetworkedCacheableImageView;
import com.klinker.android.twitter.utils.IOUtils;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;
import uk.co.senab.photoview.PhotoViewAttacher;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class PhotoFragment extends Fragment {
    
    private Activity activity;

    public static PhotoFragment getInstance(String s) {
        Bundle b = new Bundle();
        b.putString("url", s);

        PhotoFragment fragment = new PhotoFragment();
        fragment.setArguments(b);

        return fragment;
    }

    String url;
    NetworkedCacheableImageView picture;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        activity = getActivity();

        Bundle args = getArguments();
        url = args.getString("url");

        final View root = inflater.inflate(R.layout.photo_dialog_layout, container, false);

        picture = (NetworkedCacheableImageView) root.findViewById(R.id.picture);
        PhotoViewAttacher mAttacher = new PhotoViewAttacher(picture);

        picture.loadImage(url, false, new NetworkedCacheableImageView.OnImageLoadedListener() {
            @Override
            public void onImageLoaded(CacheableBitmapDrawable result) {
                LinearLayout spinner = (LinearLayout) root.findViewById(R.id.list_progress);
                spinner.setVisibility(View.GONE);
            }
        }, 0, true); // no transform

        mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                try {
                    activity.finish();
                } catch (Exception e) {
                    // activity is null
                }
            }
        });

        return root;
    }

    public void saveImage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                try {
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(activity)
                                    .setSmallIcon(R.drawable.ic_stat_icon)
                                    .setTicker(getResources().getString(R.string.downloading) + "...")
                                    .setContentTitle(getResources().getString(R.string.app_name))
                                    .setContentText(getResources().getString(R.string.saving_picture) + "...")
                                    .setProgress(100, 100, true)
                                    .setLargeIcon(BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_action_save));

                    NotificationManager mNotificationManager =
                            (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(6, mBuilder.build());

                    URL mUrl = new URL(url);

                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    InputStream is = new BufferedInputStream(conn.getInputStream());

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = false;

                    Bitmap bitmap = decodeSampledBitmapFromResourceMemOpt(is, 600, 600);

                    Random generator = new Random();
                    int n = 1000000;
                    n = generator.nextInt(n);
                    String fname = "Image-" + n;

                    Uri uri = IOUtils.saveImage(bitmap, fname, activity);
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "image/*");

                    PendingIntent pending = PendingIntent.getActivity(activity, 91, intent, 0);

                    mBuilder =
                            new NotificationCompat.Builder(activity)
                                    .setContentIntent(pending)
                                    .setSmallIcon(R.drawable.ic_stat_icon)
                                    .setTicker(getResources().getString(R.string.saved_picture) + "...")
                                    .setContentTitle(getResources().getString(R.string.app_name))
                                    .setContentText(getResources().getString(R.string.saved_picture) + "!")
                                    .setLargeIcon(BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_action_save));

                    mNotificationManager.notify(6, mBuilder.build());
                } catch (Exception e) {
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(activity)
                                    .setSmallIcon(R.drawable.ic_stat_icon)
                                    .setTicker(getResources().getString(R.string.error) + "...")
                                    .setContentTitle(getResources().getString(R.string.app_name))
                                    .setContentText(getResources().getString(R.string.error) + "...")
                                    .setProgress(100, 100, true)
                                    .setLargeIcon(BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_action_save));

                    NotificationManager mNotificationManager =
                            (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(6, mBuilder.build());
                }
            }
        }).start();
    }

    public void shareImage() {
        if (picture == null) {
            return;
        }

        Bitmap bitmap = ((BitmapDrawable)picture.getDrawable()).getBitmap();

        // create the intent
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        sharingIntent.setType("image/*");

        // add the bitmap uri to the intent
        Uri uri = getImageUri(activity, bitmap);
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);

        // start the chooser
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.menu_share) + ": "));
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {

        File camera = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera");
        camera.mkdirs();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "talon-share-image", null);
        try {
            return Uri.parse(path);
        } catch (Exception e) {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public Bitmap decodeSampledBitmapFromResourceMemOpt(
            InputStream inputStream, int reqWidth, int reqHeight) {

        byte[] byteArr = new byte[0];
        byte[] buffer = new byte[1024];
        int len;
        int count = 0;

        try {
            while ((len = inputStream.read(buffer)) > -1) {
                if (len != 0) {
                    if (count + len > byteArr.length) {
                        byte[] newbuf = new byte[(count + len) * 2];
                        System.arraycopy(byteArr, 0, newbuf, 0, count);
                        byteArr = newbuf;
                    }

                    System.arraycopy(buffer, 0, byteArr, count, len);
                    count += len;
                }
            }

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(byteArr, 0, count, options);

            options.inSampleSize = calculateInSampleSize(options, reqWidth,
                    reqHeight);
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            return BitmapFactory.decodeByteArray(byteArr, 0, count, options);

        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options opt, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = opt.outHeight;
        final int width = opt.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
