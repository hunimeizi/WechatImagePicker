package com.haolin.android.imagepickerlibrary.imagepicker.loader;

import android.widget.ImageView;

public interface ImageLoader {


    void displayFileImage(ImageView imageView, String path);

    void displayUserImage(ImageView imageView, String path);

    void displayFileVideo(String path);


    Class<?> displayFullImageClass();
}
