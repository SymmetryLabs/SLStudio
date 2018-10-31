cmake_minimum_required(VERSION 3.7 FATAL_ERROR)
cmake_policy(VERSION 3.7)
project(slimgui VERSION 0.1.0 LANGUAGES CXX)

find_package(JNI REQUIRED)
find_package(glfw3 REQUIRED)

set(CMAKE_CXX_FLAGS "-Wall -Werror -DIMGUI_IMPL_OPENGL_LOADER_GLEW")

include_directories(${JNI_INCLUDE_DIRS} src/)

add_library(slimgui SHARED
  src/slimgui.cpp
  src/imgui.cpp
  src/imgui_demo.cpp
  src/imgui_draw.cpp
  src/imgui_widgets.cpp
  src/examples/imgui_impl_glfw.cpp
  src/examples/imgui_impl_opengl2.cpp
  )
set_target_properties(slimgui PROPERTIES
    VERSION ${PROJECT_VERSION}
    SOVERSION 1)
target_link_libraries(slimgui glfw)
