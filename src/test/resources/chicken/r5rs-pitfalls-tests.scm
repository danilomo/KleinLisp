;; R5RS Pitfalls Tests - Extracted from Chicken Scheme r5rs_pitfalls.scm
;; Tests edge cases and subtle behavior in R5RS Scheme
;; Format: ((expression) expected-result)

;; Section 5: #f/() distinctness

;; Test 5.1
((eq? #f '()) #f)

;; Test 5.2
((eqv? #f '()) #f)

;; Test 5.3
((equal? #f '()) #f)

;; Section 6: string->symbol case sensitivity

;; Test 6.1
((eq? (string->symbol "f") (string->symbol "F")) #f)

;; Section 8: Miscellaneous

;; Test 8.2 - append non-mutation
((let ((ls (list 1 2 3 4))) (append ls ls '(5))) (1 2 3 4 1 2 3 4 5))
