(define (factorial n)
  (if (= n 0)
      1
      (* n (factorial (- n 1)))))

(println (factorial 0))
(println (factorial 1))
(println (factorial 5))