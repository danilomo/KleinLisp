(define (fibonacci n)
  (if (<= n 1)
      n
      (+ (fibonacci (- n 1)) (fibonacci (- n 2)))))

(println (fibonacci 0))
(println (fibonacci 1))
(println (fibonacci 7))