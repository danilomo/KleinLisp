(define (bar a b)
  (+ a b))

(define (foo a b)
  (display a)
  (display b)
  (bar a b))

(foo 1 'a)
