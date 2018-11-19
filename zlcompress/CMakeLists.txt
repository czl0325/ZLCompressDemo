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