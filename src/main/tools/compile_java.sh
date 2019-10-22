#!/bin/sh

if [ $# -lt 3 ]; then
    echo "too less arguments"
    exit 1
fi

# Thrift version
thrift_version=$1 
tre_path=$(dirname $0)/../env/$thrift_version
if [ ! -d "$tre_path" ]; then
    echo "Unsupported Thrift version: $thrift_version"
    exit 1
fi
tre_path=$(cd $tre_path; pwd)
if [ $(find $tre_path -name "*.jar" | wc -l) == 0 ]; then
    echo "Unsupported Thrift version: $thrift_version"
    exit 1
fi

# java src dir
java_src_dir=$2
if [ ! -d "$java_src_dir" ]; then
    echo "Invalid java src dir: \"$java_src_dir\""
    exit 1
fi
java_src_dir=$(cd $java_src_dir; pwd)
java_src_cnt=$(find $java_src_dir -name "*.java" | wc -l | awk '{print $1}')
if [ $java_src_cnt == 0 ]; then
    echo "Empty java src dir: \"$java_src_dir\", files(*.java) not found"
    exit 1
fi

# output dir
out_dir=$3
if [ ! -d "$out_dir" ]; then
    mkdir -p $out_dir
fi
out_dir=$(cd $out_dir; pwd)

# java tmp dir, copy src to tmp dir
java_tmp_dir=$out_dir/tmp_src
rm -rf $java_tmp_dir
mkdir $java_tmp_dir
find $java_src_dir -name "*.java" | xargs cp -t $java_tmp_dir

java -version

echo "Thrift Runtime Environment: \"$tre_path\""
echo "Java source: \"$java_src_dir\", files(*.java) found: $java_src_cnt"
echo "Output dir: \"$out_dir\""

classes_dir=$out_dir/classes
rm -rf $classes_dir
mkdir $classes_dir
javac -cp :$tre_path/* -d $classes_dir $java_tmp_dir/*.java
rm -rf $java_tmp_dir

## No need to compile as a jar
# jar_path=$out_dir/classes.jar
# rm -rf $jar_path
# cd $classes_dir
# jar -cvf $jar_path ./*
