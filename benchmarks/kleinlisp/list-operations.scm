(define (make-list n)
  (define (make-list-loop i acc)
    (if (<= i 0)
        acc
        (make-list-loop (- i 1) (cons i acc))))
  (make-list-loop n '()))

(define (sum-list lst)
  (if (null? lst)
      0
      (+ (car lst) (sum-list (cdr lst)))))

(define (reverse-list lst)
  (define (reverse-list-loop lst acc)
    (if (null? lst)
        acc
        (reverse-list-loop (cdr lst) (cons (car lst) acc))))
  (reverse-list-loop lst '()))

(define (list-benchmark n)
  (let ((lst (make-list n)))
    (let ((sum (sum-list lst)))
      (let ((rev (reverse-list lst)))
        (length rev)))))

(list-benchmark 1000)