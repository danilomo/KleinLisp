(define (fib n)
  (if (<= n 1)
      n
      (+ (fib (- n 1)) (fib (- n 2)))))

(define (benchmark-fib n iterations)
  (define (benchmark-fib-loop n i)
    (if (> i 0)
        (begin
          (fib n)
          (benchmark-fib-loop n (- i 1)))))
  (benchmark-fib-loop n iterations))

(benchmark-fib 30 10)