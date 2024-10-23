(define (iter lst1 lst2 acc)
  (if (null? lst1)
      (reverse acc)
      (iter (cdr lst1) lst2 (cons (car lst1) acc))))
