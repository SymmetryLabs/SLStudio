#!/bin/sh

set -e

echo "Installing Homebrew (this will ask you for your password)"
/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"

echo "Installing JDK8"
brew tap caskroom/versions
brew cask install java8

echo "Installing SLStudio dependencies"
brew install glfw glew
