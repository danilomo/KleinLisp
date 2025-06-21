def let_benchmark(iterations):
    def nested_lets(n, acc):
        if n <= 0:
            return acc
        else:
            # Python doesn't have let, so we use local variables
            x, y, z = 1, 2, 3
            a, b, c = x + y, y * z, x + z
            result = a + b + c
            return nested_lets(n - 1, acc + result)
    return nested_lets(iterations, 0)

let_benchmark(10000)