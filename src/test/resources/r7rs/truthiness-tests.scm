;; R7RS Truthiness Tests
;; Section 6.3: Booleans
;; In R7RS, only #f is false. Everything else is true, including:
;; - The empty list ()
;; - Zero (0)
;; - Empty string ("")
;; - Empty vector (#())
;; - All other values

;; Format: (expression expected-result)

;; Test 1: Empty list should be true (not evaluates to #f)
((not '()) #f)

;; Test 2: Empty list in if should take true branch
((if '() 'yes 'no) yes)

;; Test 3: Zero should be true (not false like in C)
((not 0) #f)

;; Test 4: Empty string should be true
((not "") #f)

;; Test 5: Empty vector should be true
((not #()) #f)

;; Test 6: #f should be false (not of #f is #t)
((not #f) #t)

;; Test 7: #t should be true (not of #t is #f)
((not #t) #f)

;; Test 8: Non-empty list should be true
((not '(1 2 3)) #f)

;; Test 9: Symbols should be true
((not 'foo) #f)

;; Test 10: and with empty list should return the empty list (not #t)
((and #t '()) ())

;; Test 11: or with empty list and #f should return empty list
((or #f '()) ())

;; Test 12: cond with empty list test should take that branch
((cond ('() 'yes) (else 'no)) yes)

;; Test 13: boolean? should only return #t for #t and #f
((boolean? '()) #f)

;; Test 14: double not of empty list should return #t
((not (not '())) #t)

;; Test 15: Positive number is true
((not 42) #f)

;; Test 16: Negative number is true
((not -1) #f)

;; Test 17: Pair is true
((not (cons 1 2)) #f)

;; Test 18: Procedure is true
((not (lambda (x) x)) #f)
