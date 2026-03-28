;; R7RS Comparison operator test cases
;; Format: (expression expected-result)

;; Numeric comparisons with integers - variadic
((< 1 2) #t)
((< 2 1) #f)
((< 1 2 3 4) #t)
((< 1 2 2 4) #f)
((< 1 3 2 4) #f)

((> 2 1) #t)
((> 1 2) #f)
((> 4 3 2 1) #t)
((> 4 3 3 1) #f)

((<= 1 2) #t)
((<= 2 2) #t)
((<= 3 2) #f)
((<= 1 2 2 3) #t)
((<= 1 2 3 2) #f)

((>= 2 1) #t)
((>= 2 2) #t)
((>= 1 2) #f)
((>= 3 2 2 1) #t)
((>= 3 2 1 2) #f)

((= 5 5) #t)
((= 5 6) #f)
((= 5 5 5) #t)
((= 5 5 6) #f)

;; Numeric comparisons with floats
((< 1.0 2.0) #t)
((< 1.0 2.0 3.0) #t)
((> 3.5 2.5 1.5) #t)
((<= 1.5 1.5 2.5) #t)
((>= 2.5 2.5 1.5) #t)
((= 2.5 2.5) #t)

;; Mixed int and float comparisons
((< 1 2.5 3) #t)
((< 1.5 2 3.5) #t)
((= 2.0 2) #t)
((= 2 2.0 2) #t)
((> 3.0 2 1.0) #t)

;; String comparisons - variadic
((string=? "foo" "foo") #t)
((string=? "foo" "bar") #f)
((string=? "foo" "foo" "foo") #t)
((string=? "foo" "foo" "bar") #f)

((string<? "a" "b") #t)
((string<? "b" "a") #f)
((string<? "a" "b" "c") #t)
((string<? "a" "c" "b") #f)

((string>? "b" "a") #t)
((string>? "a" "b") #f)
((string>? "c" "b" "a") #t)

((string<=? "a" "a") #t)
((string<=? "a" "b") #t)
((string<=? "a" "a" "b") #t)

((string>=? "a" "a") #t)
((string>=? "b" "a") #t)
((string>=? "b" "b" "a") #t)

;; Case-insensitive string comparisons
((string-ci=? "FOO" "foo") #t)
((string-ci=? "foo" "FOO" "Foo") #t)
((string-ci<? "A" "b") #t)
((string-ci<? "a" "B" "c") #t)
((string-ci>? "B" "a") #t)
((string-ci<=? "A" "a" "B") #t)
((string-ci>=? "B" "b" "A") #t)
