def let_benchmark(iterations):
    acc = 0
    for _ in range(iterations):
        # Python doesn't have let, so we use local variables
        x, y, z = 1, 2, 3
        a, b, c = x + y, y * z, x + z
        result = a + b + c
        acc += result
    return acc

let_benchmark(10000)