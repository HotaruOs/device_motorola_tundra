#!/bin/bash
GCC_DIR="${PWD}/eva-gcc"

if ! [ -d "$GCC_DIR" ]; then
    echo "Eva GCC not found!! downloading..."
    mkdir -p "$GCC_DIR"

    wget -c https://github.com/mvaisakh/gcc-build/releases/download/20072025/eva-gcc-arm64-20072025.xz -O /tmp/eva-gcc-arm64.xz
    wget -c https://github.com/mvaisakh/gcc-build/releases/download/20072025/eva-gcc-arm-20072025.xz -O /tmp/eva-gcc-arm.xz

    if [ $? -ne 0 ]; then
        echo "ERROR: Eva GCC download failed!"
        exit 1
    fi

    tar -xf /tmp/eva-gcc-arm64.xz -C "$GCC_DIR"
    tar -xf /tmp/eva-gcc-arm.xz -C "$GCC_DIR"

    if [ $? -ne 0 ]; then
        echo "ERROR: Failed to Extract Eva GCC "
        exit 1
    fi

    echo "Eva GCC successfully installed on $GCC_DIR."
else
    echo "Eva GCC was already installed in $GCC_DIR."
fi
