#!/bin/bash

# Performance Benchmark Runner for KleinLisp vs Other Languages
# Compares KleinLisp against Guile, Racket, Ruby, and Python

set -e

BENCHMARKS=("fibonacci" "let-performance" "list-operations" "tail-recursion")
LANGUAGES=("kleinlisp" "guile" "racket" "ruby" "python")

echo "Building KleinLisp..."
mvn package -q

echo "=== Performance Benchmark Results ==="
echo "Date: $(date)"
echo "System: $(uname -a)"
echo ""

run_benchmark() {
    local lang=$1
    local benchmark=$2
    local file=""
    local cmd=""
    
    case $lang in
        "kleinlisp")
            file="benchmarks/kleinlisp/${benchmark}.scm"
            cmd="java -jar target/KleinLisp-0.0.1.jar $file"
            ;;
        "guile")
            file="benchmarks/guile/${benchmark}.scm"
            cmd="guile --no-auto-compile $file"
            ;;
        "racket")
            file="benchmarks/racket/${benchmark}.rkt"
            cmd="racket $file"
            ;;
        "ruby")
            file="benchmarks/ruby/${benchmark}.rb"
            cmd="ruby $file"
            ;;
        "python")
            file="benchmarks/python/${benchmark}.py"
            cmd="python3 $file"
            ;;
    esac
    
    if command -v ${cmd%% *} &> /dev/null; then
        echo -n "  $lang: "
        local start_time=$(date +%s.%N)
        timeout 30s $cmd > /dev/null 2>&1
        local exit_code=$?
        local end_time=$(date +%s.%N)
        
        if [ $exit_code -eq 0 ]; then
            local duration=$(echo "$end_time - $start_time" | bc -l)
            printf "%.3f seconds\n" $duration
        elif [ $exit_code -eq 124 ]; then
            echo "TIMEOUT (>30s)"
        else
            echo "ERROR"
        fi
    else
        echo "  $lang: NOT INSTALLED"
    fi
}

for benchmark in "${BENCHMARKS[@]}"; do
    echo "Benchmark: $benchmark"
    for lang in "${LANGUAGES[@]}"; do
        run_benchmark $lang $benchmark
    done
    echo ""
done

echo "=== System Information ==="
echo "Java version:"
java -version 2>&1 | head -1
echo ""

if command -v guile &> /dev/null; then
    echo "Guile version:"
    guile --version | head -1
fi

if command -v racket &> /dev/null; then
    echo "Racket version:"
    racket --version
fi

if command -v ruby &> /dev/null; then
    echo "Ruby version:"
    ruby --version
fi

if command -v python3 &> /dev/null; then
    echo "Python version:"
    python3 --version
fi

echo ""
echo "=== Notes ==="
echo "- fibonacci: Recursive Fibonacci calculation (fib(30) x 10 iterations)"
echo "- let-performance: Nested let bindings stress test (10,000 iterations)"
echo "- list-operations: List creation, sum, and reversal (1,000 elements)"
echo "- tail-recursion: Tail call optimization test (factorial + countdown)"
echo ""
echo "KleinLisp implements tail call optimization and optimized let forms."
echo "Compare especially the let-performance and tail-recursion benchmarks."