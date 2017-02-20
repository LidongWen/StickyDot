# StickyDot
qq 红点取消控件

<img width="320" height="500" src="https://github.com/LidongWen/SimpleStickyDot/blob/master/art/stickyDot.gif"></img>

## 引用
```groovy
// 项目引用
dependencies {
    compile 'com.github.LidongWen:stickyDot:1.0.0'
}

// 根目录下引用

allprojects {
    repositories {
        jcenter()
        maven { url "https://www.jitpack.io" }
    }
}
```

## 代码使用
```java
hepler = new StickyDotHepler(mContext, view, dragView)
//      .setMaxDragDistance(DensityUtils.dip2px(mContext, 200))//设置拖拽距离
//      .setColor(Color.parseColor("#94D5EE"))//设置颜色
//      .setDraged(false)  //设置是否可以被拖拽
        .setOutListener(new StickyDotHepler.StickyListener() {  //监听
            @Override
            public void outRangeUp(PointF dragCanterPoint) {

            }
        });

//      hepler.dismiss();// 取消
```