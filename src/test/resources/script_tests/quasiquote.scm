; Basic quasiquote
(println `(1 2 3))

; Unquote
(println `(1 ,(+ 1 1) 3))

; Unquote at different positions
(println `(,(+ 2 3) 2 3))
(println `(1 2 ,(* 2 3)))

; Unquote with variable
(define x 42)
(println `(a ,x b))

; Unquote-splicing
(println `(1 ,@(list 2 3) 4))
(println `(,@(list 1 2) 3 4))
(println `(1 2 ,@(list 3 4)))

; Empty unquote-splicing
(println `(1 ,@(list) 4))

; Mixed unquote and splicing
(println `(a ,(+ 1 2) ,@(list 4 5) b))

; Nested list with unquote
(println `(a (b ,(+ 1 2) c) d))

; Multiple unquotes
(println `(,(+ 0 1) ,(+ 1 1) ,(+ 1 2)))

; Quasiquote with let
(println (let ((y 10)) `(1 ,y 3)))

; Quasiquote in define
(define (make-adder n) `(lambda (x) (+ x ,n)))
(println (make-adder 5))

; Quasiquote with function call
(define (square z) (* z z))
(println `(result ,(square 4)))

; Multiple splicing
(println `(a ,@(list 1 2) b ,@(list 3 4) c))

; Deep nesting
(define w 5)
(println `((a ,w) (b ,w)))
