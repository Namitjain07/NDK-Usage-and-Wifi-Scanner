cmake_minimum_required(VERSION 3.22.1)
project(matrix_calculator)

add_library(matrix-lib SHARED
            MatrixOperations.cpp
            matrix_jni.cpp)

target_include_directories(matrix-lib PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})

find_library(log-lib log)
target_link_libraries(matrix-lib ${log-lib})
