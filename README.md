# wechatImagePicker 仿微信选择图片

#### 使用方法
1.使用gradle
```js
repositories {
  google()
  mavenCentral()
}

dependencies {
  implementation 'io.github.hunimeizi:haolinPicturePicker:1.0.5'
  //以下务必全部依赖
  implementation 'io.github.hunimeizi:haolinActivityResultLauncher:1.0.0'
  implementation 'io.coil-kt:coil:1.4.0'
}
```

4.进行参数设置
```js
private val startActivityLauncher = StartActivityLauncher(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btnChooseImage).setOnClickListener {
            chooseImage()
        }
    }

    private fun chooseImage(){
        ImagePicker.instance
            .activityResultCaller(startActivityLauncher)
            .multiMode(false) //多选
            .showCamera(true) //                .selectLimit(9)//最多选几张
            .crop(true) // 是否裁剪
            .outPutY((DensityUtil.getScreenWidth(this) * 0.8f).toInt()) // 裁剪图片宽
            .outPutX((DensityUtil.getScreenWidth(this) * 0.8f).toInt()) // 裁剪图片高
            .focusWidth((DensityUtil.getScreenWidth(this) * 0.8f).toInt()) //裁剪框 宽
            .focusHeight((DensityUtil.getScreenWidth(this) * 0.8f).toInt()) // 裁剪框 高
            .style(CropImageView.Style.RECTANGLE) //裁剪样式 圆形 矩形
                .selectedListener(object : ImagePicker.OnSelectedListener{
                    override fun onImageSelected(items: List<ImageItem?>?) {
                        if (items == null) return
                        Toast.makeText(this@MainActivity, "图片地址：${items[0]?.imageUrl}", Toast.LENGTH_SHORT).show()
                    }
                })
            .startImagePicker()
    }
```

#### 内嵌上传 Maven Central
详细请看教程
[JCenter已经提桶跑路，是时候学会上传到Maven Central了](https://mp.weixin.qq.com/s/CrfYc1KsugJKPy_0rDZ49Q)