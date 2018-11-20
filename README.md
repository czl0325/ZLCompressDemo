# ZLCompressDemo
图片压缩的方法，质量压缩，尺寸压缩，采样率压缩，哈夫曼编码压缩

根据ricky老师的demo来的，他是用eclipse开发，我用android studio开发。中间遇到了不少坑。记录下来。<br>

### 1.新建工程

创建一个非ndk的工程，取名叫ZLCompressDemo

![](https://github.com/czl0325/ZLCompressDemo/blob/master/screenspot/demo1.png?raw=true)

### 2.添加ndk的module

file->new->new module，新建一个module，取名叫ZLCompress

![](https://github.com/czl0325/ZLCompressDemo/blob/master/screenspot/demo2.png?raw=true)

点击完成.

### 3.将module添加到工程依赖

把ZLCompress依赖添加到工程

![](https://github.com/czl0325/ZLCompressDemo/blob/master/screenspot/demo3.png?raw=true)

### 4.在ZLCompress库中添加ZLCompress.java类

<img src="https://github.com/czl0325/ZLCompressDemo/blob/master/screenspot/demo4.png?raw=true" width="280"/>

### 5.导入so库

在ZLCompress目录下的libs文件夹中，把编译好的两个so库导入进来。如图

<img src="https://github.com/czl0325/ZLCompressDemo/blob/master/screenspot/demo5.png?raw=true" width="280"/>

### 6.编写native方法

在ZLCompress.java文件中添加so库的导入，并编写一个native方法。

![](https://github.com/czl0325/ZLCompressDemo/blob/master/screenspot/demo6.png?raw=true)

新建一个cpp文件夹，把jpeg相关的头文件导入，如图

<img src="https://github.com/czl0325/ZLCompressDemo/blob/master/screenspot/demo8.png?raw=true" width="280"/>

cd到src/main/java的文件夹下，使用javah命令生成头文件

```JAVA
javah -classpath . -jni com.github.zlcompress.ZLCompress
```

生成后如图：

<img src="https://github.com/czl0325/ZLCompressDemo/blob/master/screenspot/demo9.png?raw=true" width="320"/>

并且新建一个native-compress.cpp文件

### 7.CMakeLists文件编写

在与src同级的目录下创建一个CMakeLists.txt文件

![](https://github.com/czl0325/ZLCompressDemo/blob/master/screenspot/demo7.png?raw=true)

```CMake
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

* 出现错误
```
Error:error: '../../../../src/main/jniLibs/mips64/...', needed by '../../../../build/intermediates/cmake/debug/obj/mips64/...', missing and no known rule to make it

```
这既有可能是你CMakeLists里面的set_target_properties路径配置错误了，相对路径计算很麻烦，一不小心就错了，配置成绝对路径就安全了很多。<br>
比如我改成了${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libjpegbither.so，编译通过。

### 8.配置gradle

在module的build.gradle下添加配置

```JAVA
android {
    compileSdkVersion 28

    defaultConfig {
        minSdkVersion 19
        targetSdkVersion 28
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                cppFlags " -frtti -fexceptions -std=c++11  "
                abiFilters 'armeabi-v7a'
            }
        }
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
    sourceSets.main {
        jniLibs.srcDirs = ['libs']
        jni.srcDirs = []
    }
    externalNativeBuild {
        cmake {
            path "CMakeLists.txt"
        }
    }
    /**
     * Failure [INSTALL_FAILED_NO_MATCHING_ABIS: Failed to extract native libraries, res=-113]
     * Error while Installing APK
     */
    splits {
        abi {
            enable true
            reset()
            include 'armeabi-v7a'
            universalApk true
        }
    }
}
```

### 9. 编写native-compress.cpp的代码

这里就不写了，具体看demo

### 10. 运行

* 如果出现AndroidBitmap_lockPixels函数找不到的话，注意CMakeLists文件要添加jnigraphics的依赖
* 模拟器无法运行，这是因为我们只有armeabi-v7a的库，模拟器不是armeabi-v7a结构的，所以不能运行，真机可以
* 错误
```JAVA
java.lang.UnsatisfiedLinkError: No implementation found for int com.github.zlcompress.ZLCompress.compressBitmap(java.lang.Object, int, int, int, byte[], boolean) (tried Java_com_github_zlcompress_ZLCompress_compressBitmap and Java_com_github_zlcompress_ZLCompress_compressBitmap__Ljava_lang_Object_2III_3BZ)
```

