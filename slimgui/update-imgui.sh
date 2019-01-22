#!/bin/bash

git clone -b docking --depth=1 https://github.com/ocornut/imgui /tmp/imgui
cp /tmp/imgui/*.cpp src/main/cpp/
cp /tmp/imgui/*.h src/main/cpp/
cp /tmp/imgui/examples/imgui_impl_glfw.cpp src/main/cpp/
cp /tmp/imgui/examples/imgui_impl_glfw.h src/main/cpp/
cp /tmp/imgui/examples/imgui_impl_opengl3.cpp src/main/cpp/
cp /tmp/imgui/examples/imgui_impl_opengl3.h src/main/cpp/
