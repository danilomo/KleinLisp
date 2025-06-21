(define double (lambda (x) (* x 2)))
(println (double 5))

(define add (lambda (x y) (+ x y)))
(println (add 3 7))

(println ((lambda (x) (+ x 1)) 9))