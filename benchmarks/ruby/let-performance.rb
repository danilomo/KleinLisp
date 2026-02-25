def let_benchmark(iterations)
  acc = 0
  iterations.times do
    # Ruby doesn't have let, so we use local variables and blocks
    x, y, z = 1, 2, 3
    a, b, c = x + y, y * z, x + z
    result = a + b + c
    acc += result
  end
  acc
end

let_benchmark(10000)