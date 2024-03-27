#!/bin/bash

if [ -z $1 ]
then
    echo "Usage: $0 [BENCHMARK_NAME] [WIDTH]? [NUM_DIAMONDS]?"
    exit 1
fi

name=$1
testDir="benchmark/$name/TEST_test_trace"

mkdir $testDir/pattern_pre

if [ ! -d $testDir ]
then
    echo "$testDir directory does not exists"
    exit 1
fi

format="rr"
traceFile="TEST_test_trace.rr"
tracePath="$testDir/$traceFile"

cpfile="$(pwd)/bin:$(pwd)/lib/*"


echo "Generating patterns..."

# java -cp "$cpfile" PatternGeneration -f $format -p $tracePath

echo "Constructing DAG and Trie..."

python3 scripts/dag.py $testDir/pattern_pre $2 $3
