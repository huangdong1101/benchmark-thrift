#!/bin/sh

if [ $# -lt 2 ]; then
    echo "too less arguments"
    exit 1
fi

# IDL src dir
src_dir=$1
if [ ! -d "$src_dir" ]; then
    echo "Invalid IDL src dir: \"$src_dir\""
    exit 1
fi
src_dir=$(cd $1; pwd)
src_cnt=$(find $src_dir -name "*.thrift" | wc -l | awk '{print $1}')
if [ $src_cnt == 0 ]; then
    echo "Empty IDL src dir: \"$src_dir\", files(*.thrift) not found"
    exit 1
fi

# output dir
out_dir=$2
if [ ! -d "$out_dir" ]; then
    mkdir -p $out_dir
fi
out_dir=$(cd $out_dir; pwd)

thrift -version
echo "IDL source: \"$src_dir\", files(*.thrift) found: $src_cnt"
find $src_dir -name "*.thrift" | xargs -i thrift -r -gen java -o $out_dir {}

thrift_version=$(thrift -version | awk '{print $3}')
sh $(cd "$(dirname $0)"; pwd)/compile_java.sh $thrift_version $out_dir/gen-java $out_dir
