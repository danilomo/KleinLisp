; Test 1: Simple binding
(display (letrec* ((x 1)) x))
(newline)

; Test 2: Sequential dependency - b references a
(display (letrec* ((a 1)
                   (b (+ a 1))
                   (c (+ b 1)))
           c))
(newline)

; Test 3: Mutual recursion - even?/odd?
(display (letrec* ((even? (lambda (n) (if (= n 0) #t (odd? (- n 1)))))
                   (odd? (lambda (n) (if (= n 0) #f (even? (- n 1))))))
           (even? 10)))
(newline)

; Test 4: Mutual recursion - odd? test
(display (letrec* ((even? (lambda (n) (if (= n 0) #t (odd? (- n 1)))))
                   (odd? (lambda (n) (if (= n 0) #f (even? (- n 1))))))
           (odd? 7)))
(newline)

; Test 5: Recursive factorial
(display (letrec* ((fact (lambda (n)
                           (if (= n 0)
                               1
                               (* n (fact (- n 1)))))))
           (fact 5)))
(newline)

; Test 6: Lambda capturing earlier bindings
(display (letrec* ((x 10)
                   (f (lambda (y) (+ x y))))
           (f 5)))
(newline)

; Test 7: Function applied to earlier binding
(display (letrec* ((double (lambda (x) (* x 2)))
                   (five 5)
                   (ten (double five)))
           ten))
(newline)

; Test 8: Empty bindings
(display (letrec* () 42))
(newline)

; Test 9: Chained dependencies
(display (letrec* ((a 1)
                   (b (+ a 2))
                   (c (+ b 3))
                   (d (+ c 4)))
           d))
(newline)

; Test 10: Building a list from bindings
(display (letrec* ((a 1)
                   (b 2)
                   (c 3)
                   (lst (list a b c)))
           lst))
(newline)

; Test 11: Nested letrec*
(display (letrec* ((x 1))
           (letrec* ((y (+ x 1))
                     (z (+ y 1)))
             (+ x y z))))
(newline)

; Test 12: Sum using fold with letrec*
(display (letrec* ((fold (lambda (f init lst)
                           (if (null? lst)
                               init
                               (fold f (f init (car lst)) (cdr lst)))))
                   (sum (lambda (lst) (fold + 0 lst))))
           (sum '(1 2 3 4 5))))
(newline)
