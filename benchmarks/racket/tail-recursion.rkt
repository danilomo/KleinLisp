#lang racket

(define (factorial n)
  (define (fact-iter n acc)
    (if (<= n 1)
        acc
        (fact-iter (- n 1) (* n acc))))
  (fact-iter n 1))

(define (countdown n)
  (if (<= n 0)
      0
      (countdown (- n 1))))

(define (tail-rec-benchmark)
  (begin
    (factorial 1000)
    (countdown 10000)))

(tail-rec-benchmark)