def factorial(n):
    def fact_iter(n, acc):
        if n <= 1:
            return acc
        else:
            return fact_iter(n - 1, n * acc)
    return fact_iter(n, 1)

def countdown(n):
    if n <= 0:
        return 0
    else:
        return countdown(n - 1)

def tail_rec_benchmark():
    factorial(1000)
    countdown(10000)

tail_rec_benchmark()