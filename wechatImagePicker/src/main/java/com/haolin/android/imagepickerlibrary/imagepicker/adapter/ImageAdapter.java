package com.haolin.android.imagepickerlibrary.imagepicker.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.haolin.android.imagepickerlibrary.R;
import com.haolin.android.imagepickerlibrary.imagepicker.ImagePicker;
import com.haolin.android.imagepickerlibrary.imagepicker.ui.ImageViewerActivity;
import com.haolin.android.imagepickerlibrary.imagepicker.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * 单独提供的适配器
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    int mImageSize;
    List<String> data = new ArrayList<>();
    int interval;
    int marginLeft;
    int marginRight;
    int column;
    RecyclerView.LayoutManager manager;
    Activity activity;
    int exitPosition;
    int enterPosition;
    RecyclerView recyclerView;

    @SuppressLint("NewApi")
    public ImageAdapter(Activity activity) {
        this.activity = activity;
    }

    public RecyclerView.LayoutManager getManager() {
        return manager;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageViewHolder(new AppCompatImageView(activity));
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, final int position) {
        ImagePicker.getInstance().getImageLoader().displayUserImage(holder.imageView, data.get(position));
        holder.imageView.setOnClickListener(v -> {
            enterPosition = position;
            Intent intent = new Intent(activity, ImagePicker.getInstance().getImageLoader().displayFullImageClass() == null ? ImageViewerActivity.class : ImagePicker.getInstance().getImageLoader().displayFullImageClass());
            intent.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position);
            ImagePicker.getInstance().viewerItem(data);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && ImagePicker.getInstance().isShareView()) {
                ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, Pair.create(v, v.getTransitionName()));
                ActivityCompat.startActivity(activity, intent, compat.toBundle());
                setExitSharedElementCallback();
            } else {
                activity.startActivity(intent);
            }
        });

        String name = holder.imageView.getContext().getResources().getString(R.string.share_view_photo) + position;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            holder.imageView.setTransitionName(name);
    }

    @Override
    public int getItemCount() {
        return data.size() > 9 ? 9 : data.size();
    }


    public void setImageSize(int interval, int marginLeft, int marginRight) {
        this.interval = interval;
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;
    }

    public void bindData(List<String> list) {
        if (ImagePicker.getInstance().getImageLoader() == null) {
            throw new RuntimeException("ImageLoader is null !");
        }
        if (list != null) {
            computeImageSize(activity, interval, marginLeft, marginRight, list.size());
            data.clear();
            data.addAll(list);
            resetLayoutManager();
            notifyDataSetChanged();
        }
    }

    private void computeImageSize(Activity activity, int interval, int marginLeft, int marginRight, int size) {
        if (size == 0) return;
        int widthPixels = Utils.getScreenPix(activity).widthPixels;
        if (size == 1) {
            column = 1;
            manager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false) {
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };
        } else if (size <= 4) {
            column = 2;
            manager = new GridLayoutManager(activity, 2) {
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };
        } else {
            column = 3;
            manager = new GridLayoutManager(activity, 3) {
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };
        }
        int width = widthPixels - marginLeft - marginRight - (column) * interval;
        mImageSize = width / column;

    }

    public void onActivityReenter(int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            exitPosition = data.getIntExtra(ImagePicker.EXTRA_EXIT_POSITION, enterPosition);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setExitSharedElementCallback() {

        activity.setExitSharedElementCallback(new android.app.SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                if (exitPosition != enterPosition && names.size() > 0 && exitPosition < data.size()) {
                    names.clear();
                    sharedElements.clear();
                    View view = manager.findViewByPosition(exitPosition);
                    names.add(view.getTransitionName());
                    sharedElements.put(view.getTransitionName(), view);
                }
                activity.setExitSharedElementCallback(null);
            }
        });
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;

    }

    private void resetLayoutManager() {
        recyclerView.setLayoutManager(getManager());
        recyclerView.setAdapter(this);
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;

            LinearLayout.MarginLayoutParams layoutParams;
            if (column == 1)
                layoutParams = new LinearLayout.MarginLayoutParams((int) (mImageSize * (2 / 3.0f)), (int) (mImageSize * (2 / 3.0f)));
            else
                layoutParams = new LinearLayout.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mImageSize);
            layoutParams.setMargins(interval / 2, interval / 2, interval / 2, interval / 2);
            itemView.setLayoutParams(layoutParams);
            if (column == 1)
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            else
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }
}
