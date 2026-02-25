def factorial(n):
    acc = 1
    while n > 1:
        acc *= n
        n -= 1
    return acc

def countdown(n):
    while n > 0:
        n -= 1
    return 0

def tail_rec_benchmark():
    factorial(1000)
    countdown(10000)

tail_rec_benchmark()