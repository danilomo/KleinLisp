(define (iter n acc)
  (if (= n 0)
      acc
      (iter (- n 1) (* n acc))))
