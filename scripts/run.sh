#!/bin/sh

if [ -z $1 ]
then
    echo "Usage: $0 [BENCHMARK_NAME]"
    exit 1
fi

name=$1
testDir="benchmark/$name/TEST_test_trace"

if [ ! -d $testDir ]
then
    echo "$testDir directory does not exists"
    exit 1
fi

format="rr"
traceFile="TEST_test_trace.rr"
tracePath="$testDir/$traceFile"

cpfile="bin:lib/*"

echo "Running benchmark for $name"

echo "Running DAG patterns..."

for patternFile in $(find $testDir -name 'dag_pattern*' | sort)
do
    echo "Running pattern $(basename $patternFile)"

    java -cp "$cpfile" OptimizedVCPatternProperties -f $format -p $tracePath -m $patternFile

    echo ""
done


echo "Running Trie patterns..."

for patternFile in $(find $testDir -name 'trie_pattern*' | sort)
do
    echo "Running pattern $(basename $patternFile)"

    java -cp "$cpfile" OptimizedVCPatternProperties -f $format -p $tracePath -m $patternFile

    echo ""
done

for patternFile in $(find $testDir -name 'normal_pattern*' | sort)
do
    echo "Running pattern $(basename $patternFile)"

    java -cp "$cpfile" VCPatternProperties -f $format -p $tracePath -m $patternFile

    echo ""
done

