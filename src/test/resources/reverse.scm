(define (iter lst acc)
  (if (null? lst)
      acc
      (iter (cdr lst) (cons (car lst) acc))))
