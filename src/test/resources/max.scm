(define (iter lst max-so-far)
  (if (null? lst)
      max-so-far
      (iter (cdr lst) (max max-so-far (car lst)))))
