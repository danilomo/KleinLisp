(define (iter a b count)
  (if (= count 0)
      a
      (iter b (+ a b) (- count 1))))
