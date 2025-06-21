def let_benchmark(iterations)
  def nested_lets(n, acc)
    if n <= 0
      acc
    else
      # Ruby doesn't have let, so we use local variables and blocks
      x, y, z = 1, 2, 3
      a, b, c = x + y, y * z, x + z
      result = a + b + c
      nested_lets(n - 1, acc + result)
    end
  end
  nested_lets(iterations, 0)
end

let_benchmark(10000)