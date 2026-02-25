def make_list(n):
    acc = []
    for i in range(n, 0, -1):
        acc.insert(0, i)
    return acc

def sum_list(lst):
    total = 0
    for item in lst:
        total += item
    return total

def reverse_list(lst):
    acc = []
    for item in lst:
        acc.insert(0, item)
    return acc

def list_benchmark(n):
    lst = make_list(n)
    total = sum_list(lst)
    rev = reverse_list(lst)
    return len(rev)

list_benchmark(1000)