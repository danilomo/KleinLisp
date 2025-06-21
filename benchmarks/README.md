# KleinLisp Performance Benchmarks

This directory contains performance benchmarks comparing KleinLisp against other languages and interpreters.

## Benchmarks

### 1. Fibonacci (`fibonacci.*`)
- **Purpose**: Tests recursive function call performance
- **Workload**: Calculates fibonacci(30) 10 times
- **Key metric**: Raw computational performance

### 2. Let Performance (`let-performance.*`)
- **Purpose**: Tests the optimized let form implementation
- **Workload**: 10,000 nested let bindings with arithmetic operations
- **Key metric**: Variable binding and scoping performance
- **Note**: This specifically tests the optimization implemented in KleinLisp's LetForm

### 3. List Operations (`list-operations.*`)
- **Purpose**: Tests list manipulation performance
- **Workload**: Create list of 1,000 elements, sum all elements, reverse the list
- **Key metric**: Memory allocation and list traversal performance

### 4. Tail Recursion (`tail-recursion.*`)
- **Purpose**: Tests tail call optimization (TCO) implementation
- **Workload**: Factorial(1000) + countdown(10000)
- **Key metric**: TCO effectiveness and stack management
- **Note**: Languages without TCO may hit stack overflow limits

## Running Benchmarks

### Quick Start
```bash
./benchmarks/run_benchmarks.sh
```

### Prerequisites
Install the interpreters you want to compare:
- **Guile**: `sudo apt install guile-3.0` (Ubuntu/Debian)
- **Racket**: `sudo apt install racket` (Ubuntu/Debian)
- **Ruby**: `sudo apt install ruby` (Ubuntu/Debian)  
- **Python**: `python3` (usually pre-installed)

### Individual Benchmarks
```bash
# KleinLisp
java -jar target/KleinLisp-0.0.1.jar benchmarks/kleinlisp/fibonacci.scm

# Guile
guile benchmarks/guile/fibonacci.scm

# Racket
racket benchmarks/racket/fibonacci.rkt

# Ruby
ruby benchmarks/ruby/fibonacci.rb

# Python
python3 benchmarks/python/fibonacci.py
```

## Expected Results

### Performance Expectations
1. **Fibonacci**: Racket and Guile should be fastest (compiled/optimized)
2. **Let Performance**: KleinLisp should perform competitively due to optimized let form
3. **List Operations**: Varies by garbage collection and memory management
4. **Tail Recursion**: KleinLisp, Guile, and Racket should handle large recursions; Ruby/Python may hit limits

### Key Comparisons
- **KleinLisp vs Guile**: Similar Scheme semantics, compare interpreter efficiency
- **KleinLisp vs Racket**: Racket has sophisticated compiler optimizations
- **KleinLisp vs Ruby/Python**: Different paradigms, but good baseline for relative performance

## Benchmark Limitations

1. **Single-threaded**: All benchmarks are single-threaded
2. **Synthetic**: Artificial workloads may not reflect real-world usage
3. **Cold start**: Includes JVM startup time for KleinLisp
4. **Memory**: Doesn't measure memory usage, only execution time
5. **Language differences**: Ruby/Python equivalents approximate Lisp semantics

## Implementation Notes

### KleinLisp Optimizations Tested
- **Direct let binding**: Eliminates lambda transformation overhead
- **Tail call optimization**: Prevents stack overflow in recursive functions
- **Cons pair support**: Efficient list operations

### Language-Specific Adaptations
- **Ruby/Python**: Use closest equivalent constructs (no native let/cons)
- **Racket**: Uses `#lang racket` for full optimization
- **Guile**: Uses `--no-auto-compile` for consistent interpretation mode