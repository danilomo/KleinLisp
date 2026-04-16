;;; R7RS Lazy Evaluation Tests
;;; Tests for delay-force special form

;; Basic delay-force
(assert (= 42 (force (delay-force (delay 42)))))

;; Nested promises - delay-force forces iteratively
(assert (= 42 (force (delay-force (delay (delay 42))))))
(assert (= 42 (force (delay-force (delay (delay (delay 42)))))))

;; Difference between delay and delay-force
(assert (promise? (force (delay (delay 42)))))  ; delay doesn't force nested
(assert (= 42 (force (delay-force (delay 42))))) ; delay-force does

;; Non-promise result
(assert (= 42 (force (delay-force 42))))
(assert (= 3 (force (delay-force (+ 1 2)))))

;; Caching
(define df-count 0)
(define df-p (delay-force (delay (begin (set! df-count (+ df-count 1)) df-count))))
(assert (= 1 (force df-p)))
(assert (= 1 (force df-p)))  ; cached
(assert (= 1 df-count))       ; only incremented once

;; Stream filter example from R7RS (properly captures let bindings)
(define (stream-filter p? s)
  (delay-force
    (if (null? (force s))
        (delay '())
        (let ((h (car (force s)))
              (t (cdr (force s))))
          (if (p? h)
              (delay (cons h (stream-filter p? t)))
              (stream-filter p? t))))))

(define (integers-from n)
  (delay (cons n (integers-from (+ n 1)))))

(define naturals (integers-from 0))
(define evens (stream-filter even? naturals))

(assert (= 0 (car (force evens))))
(assert (= 2 (car (force (cdr (force evens))))))
(assert (= 4 (car (force (cdr (force (cdr (force evens))))))))

;; Infinite stream of ones
(define ones (delay-force (delay (cons 1 ones))))
(assert (= 1 (car (force ones))))
(assert (= 1 (car (force (cdr (force ones))))))

;; Environment capture
(assert (= 10 (let ((x 10)) (force (delay-force (delay x))))))

;; Closure capture
(define (make-df-value x) (delay-force (delay (* x 2))))
(define df-closure-p (make-df-value 21))
(assert (= 42 (force df-closure-p)))

;; Long chain test
(define (make-chain n val)
  (if (= n 0)
      val
      (delay (make-chain (- n 1) val))))

(define long-chain (delay-force (make-chain 50 42)))
(assert (= 42 (force long-chain)))

;; Stream map
(define (stream-map f s)
  (delay-force
    (if (null? (force s))
        (delay '())
        (delay (cons (f (car (force s)))
                     (stream-map f (cdr (force s))))))))

(define naturals-from-1 (integers-from 1))
(define squares (stream-map (lambda (x) (* x x)) naturals-from-1))

(assert (= 1 (car (force squares))))
(assert (= 4 (car (force (cdr (force squares))))))
(assert (= 9 (car (force (cdr (force (cdr (force squares))))))))

;; R7RS specification example (section 4.2.5)
(define df-r7rs-count 0)
(define df-r7rs-p
  (delay (begin
           (set! df-r7rs-count (+ df-r7rs-count 1))
           (if (> df-r7rs-count df-r7rs-x)
               df-r7rs-count
               (force (delay-force df-r7rs-p))))))
(define df-r7rs-x 5)

(assert (= 6 (force df-r7rs-p)))
(assert (= 6 df-r7rs-count))
(assert (= 6 (force df-r7rs-p)))  ; cached
(assert (= 6 df-r7rs-count))      ; unchanged
