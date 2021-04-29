package com.haolin.android.imagepickerlibrary.imagepicker.util;


import com.haolin.android.imagepickerlibrary.imagepicker.bean.ImageItem;

import java.util.ArrayList;
import java.util.List;

public class CollectionHelper {
    public static List<String> imageItem2String(ArrayList<ImageItem> mImageItems) {
        List<String> result = new ArrayList<>(mImageItems.size());
        int length = mImageItems.size();
        for (int i = 0; i < length; i++) {
            result.add(mImageItems.get(i).getImageUrl());
        }
        return result;
    }
}
