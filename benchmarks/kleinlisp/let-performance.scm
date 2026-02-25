(define (let-benchmark iterations)
  (define (let-benchmark-loop n acc)
    (if (<= n 0)
        acc
        (let ((x 1) (y 2) (z 3))
          (let ((a (+ x y)) (b (* y z)) (c (+ x z)))
            (let ((result (+ a b c)))
              (let-benchmark-loop (- n 1) (+ acc result)))))))
  (let-benchmark-loop iterations 0))

(let-benchmark 1000)