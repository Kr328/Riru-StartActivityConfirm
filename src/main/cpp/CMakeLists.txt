cmake_minimum_required(VERSION 3.0)
project(riru_template)

include_directories(libriru_utils)
include_directories(libriru_utils/libriru)
include_directories(libriru_utils/libxhook)

add_subdirectory(libriru_utils)

add_library(riru_start_activity_confirm SHARED main.c hook.c)
target_link_libraries(riru_start_activity_confirm riru_utils log)

