# ZLCompressDemo
图片压缩的方法，质量压缩，尺寸压缩，采样率压缩，哈夫曼编码压缩

根据ricky老师的demo来的，他是用eclipse开发，我用android studio开发。中间遇到了不少坑。记录下来。<br>

### 1.新建工程

创建一个非ndk的工程，取名叫ZLCompressDemo

<img src="https://github.com/czl0325/ZLCompressDemo/blob/master/screenspot/demo1.png?raw=true" width="375"/>

### 2.添加ndk的module

file->new->new module，新建一个module，取名叫ZLCompress

<img src="https://github.com/czl0325/ZLCompressDemo/blob/master/screenspot/demo2.png?raw=true" width="320"/>

点击完成.

### 3.将module添加到工程依赖

把ZLCompress依赖添加到工程

<img src="https://github.com/czl0325/ZLCompressDemo/blob/master/screenspot/demo3.png?raw=true" width="320"/>

### 4.在ZLCompress库中添加ZLCompress.java类

<img src="https://github.com/czl0325/ZLCompressDemo/blob/master/screenspot/demo4.png?raw=true" width="280"/>

### 5.导入so库

在ZLCompress目录下的libs文件夹中，把编译好的两个so库导入进来。如图

<img src="https://github.com/czl0325/ZLCompressDemo/blob/master/screenspot/demo5.png?raw=true" width="280"/>

### 6.编写native方法

在ZLCompress.java文件中添加so库的导入，并编写一个native方法。

<img src="https://github.com/czl0325/ZLCompressDemo/blob/master/screenspot/demo6.png?raw=true" width="375"/>


### 7.CMakeLists文件编写

在与src同级的目录下创建一个CMakeLists.txt文件

<img src="https://github.com/czl0325/ZLCompressDemo/blob/master/screenspot/demo7.png?raw=true" width="280"/>

```JAVA
cmake_minimum_required(VERSION 3.4.1)

# 把系统的log库导入进来
find_library( log-lib
              log )

#set(distribution_DIR libs)

add_library( bitherjni
             SHARED
             IMPORTED)
set_target_properties(  bitherjni
                        PROPERTIES IMPORTED_LOCATION
                        ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libbitherjni.so)

add_library( jpegbither
             SHARED
             IMPORTED)
set_target_properties(  jpegbither
                        PROPERTIES IMPORTED_LOCATION
                        ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libjpegbither.so)

add_library(    native-compress
                SHARED
                ${CMAKE_SOURCE_DIR}/src/main/cpp/native-compress.cpp )

include_directories(src/main/cpp/jpeg)

target_link_libraries(  native-compress
                        bitherjni
                        jpegbither
                        jnigraphics     #一定要加这句话，关键，否则bitmap.h的东西用不了！
                        ${log-lib} )
```

* 这边值得注意的是：

* 
