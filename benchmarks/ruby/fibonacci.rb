def fib(n)
  if n <= 1
    n
  else
    fib(n - 1) + fib(n - 2)
  end
end

def benchmark_fib(n, iterations)
  iterations.times do
    fib(n)
  end
end

benchmark_fib(30, 10)