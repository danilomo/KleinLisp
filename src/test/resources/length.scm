(define (iter lst acc)
  (if (null? lst)
      acc
      (iter (cdr lst) (+ acc 1))))

'(1 2 3)

(iter '(1 2 3) 0)

(define a 1)

(set! a 3333)

a
