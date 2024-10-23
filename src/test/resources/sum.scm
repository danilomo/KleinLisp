(define (iter lst acc)
  (if (null? lst)
      acc
      (iter (cdr lst) (+ (car lst) acc))))
