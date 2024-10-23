(define (iter prev lst)
  (cond ((null? lst) #t)
        ((< prev (car lst)) (iter (car lst) (cdr lst)))
        (else #f)))
