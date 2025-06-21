def factorial(n)
  def fact_iter(n, acc)
    if n <= 1
      acc
    else
      fact_iter(n - 1, n * acc)
    end
  end
  fact_iter(n, 1)
end

def countdown(n)
  if n <= 0
    0
  else
    countdown(n - 1)
  end
end

def tail_rec_benchmark
  factorial(1000)
  countdown(10000)
end

tail_rec_benchmark