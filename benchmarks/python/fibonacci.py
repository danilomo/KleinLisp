def fib(n):
    if n <= 1:
        return n
    else:
        return fib(n - 1) + fib(n - 2)

def benchmark_fib(n, iterations):
    for _ in range(iterations):
        fib(n)

benchmark_fib(30, 10)