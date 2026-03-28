; Test 1: Simple counter
(display (do ((i 0 (+ i 1)))
             ((= i 5) i)))
(newline)

; Test 2: Sum using do loop
(display (do ((i 0 (+ i 1))
              (sum 0 (+ sum i)))
             ((= i 5) sum)))
(newline)

; Test 3: Factorial using do loop
(display (do ((n 5 (- n 1))
              (result 1 (* result n)))
             ((= n 0) result)))
(newline)

; Test 4: Reverse a list
(display (do ((lst '(1 2 3 4 5) (cdr lst))
              (result '() (cons (car lst) result)))
             ((null? lst) result)))
(newline)

; Test 5: No step expression (variable keeps its value)
(display (do ((x 42)
              (i 0 (+ i 1)))
             ((= i 3) x)))
(newline)

; Test 6: Immediate exit (test is true from start)
(display (do ((i 0))
             (#t 'immediate)))
(newline)

; Test 7: Parallel step evaluation (swap x and y)
(display (do ((x 0 y)
              (y 1 x)
              (i 0 (+ i 1)))
             ((= i 3) (list x y))))
(newline)

; Test 8: Build a list using body side effects
(define result '())
(do ((i 0 (+ i 1)))
    ((= i 5))
  (set! result (cons i result)))
(display result)
(newline)

; Test 9: Count down
(display (do ((i 10 (- i 1)))
             ((= i 0) 'done)))
(newline)

; Test 10: Multiple result expressions (returns last)
(display (do ((i 0 (+ i 1)))
             ((= i 3)
              'first
              'second
              'third)))
(newline)
