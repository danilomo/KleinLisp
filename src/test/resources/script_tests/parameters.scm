; R7RS Parameters Test Script
; Tests for make-parameter, parameterize, parameter?

; Basic parameter creation and access
(define radix (make-parameter 10))
(display (radix))
(newline)

; Parameter set
(radix 16)
(display (radix))
(newline)

; Reset for next test
(radix 10)

; Parameterize basic
(display (parameterize ((radix 2)) (radix)))
(newline)

; Verify restore after parameterize
(display (radix))
(newline)

; Nested parameterize
(display (parameterize ((radix 16))
  (parameterize ((radix 2))
    (radix))))
(newline)

; Multiple parameters
(define a (make-parameter 1))
(define b (make-parameter 2))
(display (parameterize ((a 10) (b 20))
  (+ (a) (b))))
(newline)

; Converter function
(define doubled (make-parameter 5 (lambda (x) (* x 2))))
(display (doubled))
(newline)

; Converter on parameterize
(display (parameterize ((doubled 10)) (doubled)))
(newline)

; Empty parameterize
(display (parameterize () 42))
(newline)

; parameter? predicate
(display (parameter? radix))
(newline)

(display (parameter? 42))
(newline)

; Dynamic scope test
(define p (make-parameter 1))
(define (inner) (p))
(define (outer) (parameterize ((p 2)) (inner)))
(display (outer))
(newline)
(display (p))
(newline)
