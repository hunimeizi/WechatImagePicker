# wechatImagePicker 仿微信选择图片

#### 使用方法
1.使用gradle
```js
repositories {
  google()
  mavenCentral()
}

dependencies {
  implementation 'io.github.hunimeizi:haolinPicturePicker:1.0.0'
}
```


2.先获取读写手机存储权限，此处不再赘述

3.设置ImageLoader
```js
    ImagePicker.getInstance().imageLoader(CoilIVLoader())
```
4.进行参数设置
```js
 ImagePicker.getInstance()
            .multiMode(false) //多选
            .showCamera(true) //                .selectLimit(9)//最多选几张
            .crop(true) // 是否裁剪
            .outPutY((DensityUtil.getScreenWidth(this) * 0.8f).toInt()) // 裁剪图片宽
            .outPutX((DensityUtil.getScreenWidth(this) * 0.8f).toInt()) // 裁剪图片高
            .focusWidth((DensityUtil.getScreenWidth(this) * 0.8f).toInt()) //裁剪框 宽
            .focusHeight((DensityUtil.getScreenWidth(this) * 0.8f).toInt()) // 裁剪框 高
            .style(CropImageView.Style.RECTANGLE) //裁剪样式 圆形 矩形
            .selectedListener { items: List<ImageItem> ->
                Toast.makeText(this, "图片地址：${items[0].imageUrl}", Toast.LENGTH_SHORT).show()
            }
            .startImagePicker(this)
```
5.回调
```js
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ImagePicker.getInstance().onActivityResult(requestCode, resultCode, data)
    }
```

#### 内嵌上传 Maven Central
详细请看教程
[JCenter已经提桶跑路，是时候学会上传到Maven Central了](https://mp.weixin.qq.com/s/CrfYc1KsugJKPy_0rDZ49Q)