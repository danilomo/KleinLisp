def make_list(n)
  acc = []
  (1..n).each { |i| acc.unshift(i) }
  acc
end

def sum_list(lst)
  return 0 if lst.empty?
  lst.first + sum_list(lst[1..-1])
end

def reverse_list(lst)
  acc = []
  lst.each { |item| acc.unshift(item) }
  acc
end

def list_benchmark(n)
  lst = make_list(n)
  sum = sum_list(lst)
  rev = reverse_list(lst)
  rev.length
end

list_benchmark(1000)