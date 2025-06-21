(define (make-list n)
  (define (loop i acc)
    (if (<= i 0)
        acc
        (loop (- i 1) (cons i acc))))
  (loop n '()))

(define (sum-list lst)
  (if (null? lst)
      0
      (+ (car lst) (sum-list (cdr lst)))))

(define (reverse-list lst)
  (define (loop lst acc)
    (if (null? lst)
        acc
        (loop (cdr lst) (cons (car lst) acc))))
  (loop lst '()))

(define (list-benchmark n)
  (let ((lst (make-list n)))
    (let ((sum (sum-list lst)))
      (let ((rev (reverse-list lst)))
        (length rev)))))

(list-benchmark 1000)