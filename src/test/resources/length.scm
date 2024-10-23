(define (iter lst acc)
  (if (null? lst)
      acc
      (iter (cdr lst) (+ acc 1))))
