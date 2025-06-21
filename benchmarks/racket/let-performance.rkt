#lang racket

(define (let-benchmark iterations)
  (define (nested-lets n acc)
    (if (<= n 0)
        acc
        (let ((x 1) (y 2) (z 3))
          (let ((a (+ x y)) (b (* y z)) (c (+ x z)))
            (let ((result (+ a b c)))
              (nested-lets (- n 1) (+ acc result)))))))
  (nested-lets iterations 0))

(let-benchmark 10000)