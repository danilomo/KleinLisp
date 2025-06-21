(define (fib n)
  (if (<= n 1)
      n
      (+ (fib (- n 1)) (fib (- n 2)))))

(define (benchmark-fib n iterations)
  (define (loop i)
    (if (> i 0)
        (begin
          (fib n)
          (loop (- i 1)))))
  (loop iterations))

(benchmark-fib 30 10)