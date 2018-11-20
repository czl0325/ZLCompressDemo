//
// Created by zhaoliang chen on 2018/11/19.
//
#include "../../java/com_github_zlcompress_ZLCompress.h"
#include <string.h>
#include <android/bitmap.h>
#include <android/log.h>
#include <stdio.h>
#include <setjmp.h>
#include <math.h>
#include <stdint.h>
#include <time.h>

//统一编译方式
extern "C" {
    #include "jpeg/jpeglib.h"
    #include "jpeg/cdjpeg.h"		/* Common decls for cjpeg/djpeg applications */
    #include "jpeg/jversion.h"		/* for version message */
    #include "jpeg/android/config.h"
}

#define LOG_TAG "jni"
#define LOGW(...)  __android_log_write(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

typedef uint8_t BYTE;

char *error;
struct my_error_mgr {
    struct jpeg_error_mgr pub;
    jmp_buf setjmp_buffer;
};

typedef struct my_error_mgr * my_error_ptr;

METHODDEF(void)
my_error_exit (j_common_ptr cinfo) {
    my_error_ptr myerr = (my_error_ptr) cinfo->err;
    (*cinfo->err->output_message) (cinfo);
    error=(char*)myerr->pub.jpeg_message_table[myerr->pub.msg_code];
    LOGE("jpeg_message_table[%d]:%s", myerr->pub.msg_code,myerr->pub.jpeg_message_table[myerr->pub.msg_code]);
    longjmp(myerr->setjmp_buffer, 1);
}

int generateJPEG(BYTE* data, int w, int h, int quality,
                 const char* outfilename, jboolean optimize) {
    //jpeg的结构体，保存的比如宽、高、位深、图片格式等信息，相当于java的类
    struct jpeg_compress_struct jcs;

    //当读完整个文件的时候就会回调my_error_exit这个退出方法。setjmp是一个系统级函数，是一个回调。
    struct my_error_mgr jem;
    jcs.err = jpeg_std_error(&jem.pub);
    jem.pub.error_exit = my_error_exit;
    if (setjmp(jem.setjmp_buffer)) {
        return 0;
    }

    //初始化jsc结构体
    jpeg_create_compress(&jcs);
    //打开输出文件 wb:可写byte
    FILE* f = fopen(outfilename, "wb");
    if (f == NULL) {
        return 0;
    }
    //设置结构体的文件路径
    jpeg_stdio_dest(&jcs, f);
    jcs.image_width = w;//设置宽高
    jcs.image_height = h;

    //看源码注释，设置哈夫曼编码：/* TRUE=arithmetic coding, FALSE=Huffman */
    jcs.arith_code = false;
    int nComponent = 3;
    /* 颜色的组成 rgb，三个 # of color components in input image */
    jcs.input_components = nComponent;
    //设置结构体的颜色空间为rgb
    jcs.in_color_space = JCS_RGB;
//	if (nComponent == 1)
//		jcs.in_color_space = JCS_GRAYSCALE;
//	else
//		jcs.in_color_space = JCS_RGB;

    //全部设置默认参数/* Default parameter setup for compression */
    jpeg_set_defaults(&jcs);
    //是否采用哈弗曼表数据计算 品质相差5-10倍
    jcs.optimize_coding = optimize;
    //设置质量
    jpeg_set_quality(&jcs, quality, true);
    //开始压缩，(是否写入全部像素)
    jpeg_start_compress(&jcs, TRUE);

    JSAMPROW row_pointer[1];
    int row_stride;
    //一行的rgb数量
    row_stride = jcs.image_width * nComponent;
    //一行一行遍历
    while (jcs.next_scanline < jcs.image_height) {
        //得到一行的首地址
        row_pointer[0] = &data[jcs.next_scanline * row_stride];

        //此方法会将jcs.next_scanline加1
        jpeg_write_scanlines(&jcs, row_pointer, 1);//row_pointer就是一行的首地址，1：写入的行数
    }
    jpeg_finish_compress(&jcs);//结束
    jpeg_destroy_compress(&jcs);//销毁 回收内存
    fclose(f);//关闭文件

    return 1;
}

/**
 * byte数组转C的字符串
 */
char* jstrinTostring(JNIEnv* env, jbyteArray barr) {
    char* rtn = NULL;
    jsize alen = env->GetArrayLength( barr);
    jbyte* ba = env->GetByteArrayElements( barr, 0);
    if (alen > 0) {
        rtn = (char*) malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements( barr, ba, 0);
    return rtn;
}

JNIEXPORT jint JNICALL Java_com_github_zlcompress_ZLCompress_compressBitmap
        (JNIEnv *env, jclass jclz, jobject bitmap, jint width,
         jint height, jint quality, jbyteArray fileName, jboolean optimize) {
    BYTE *pixelscolor;
    //1.将bitmap里面的所有像素信息读取出来,并转换成RGB数据,保存到二维byte数组里面
    //处理bitmap图形信息方法1 锁定画布
    AndroidBitmap_lockPixels(env, bitmap, (void**)&pixelscolor);

    //2.解析每一个像素点里面的rgb值(去掉alpha值)，保存到一维数组data里面
    BYTE *data;
    BYTE r,g,b;
    data = (BYTE*)malloc(width*height*3);
    BYTE *tmpdata = data;
    int color;
    for (int i = 0; i < height; ++i) {
        for (int j = 0; j < width; ++j) {
            //解决掉alpha
            //获取二维数组的每一个像素信息(四个部分a/r/g/b)的首地址
            color = *((int*)pixelscolor);
            //0~255：
//			a = ((color & 0xFF000000) >> 24);
            r = ((color & 0x00FF0000) >> 16);
            g = ((color & 0x0000FF00) >> 8);
            b = ((color & 0x000000FF));
            //改值！！！----保存到data数据里面
            *data = b;
            *(data+1) = g;
            *(data+2) = r;
            data = data + 3;
            //一个像素包括argb四个值，每+4就是取下一个像素点
            pixelscolor += 4;
        }
    }
    //处理bitmap图形信息方法2 解锁
    AndroidBitmap_unlockPixels(env, bitmap);
    char* cFileName = jstrinTostring(env, fileName);
    //调用libjpeg核心方法实现压缩
    int resultCode = generateJPEG(tmpdata,width,height,quality,cFileName,optimize);
    if(resultCode ==0){
        return -1;
    }
    return 1;
}

