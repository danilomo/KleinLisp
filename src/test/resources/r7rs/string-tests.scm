;; String test cases

((string-length "hello") 5)
((string-append "hello" " " "world") "hello world")
((substring "hello" 1 4) "ell")
((string-ref "hello" 0) #\h)

((string->list "abc") (#\a #\b #\c))
((list->string (quote (#\h #\i))) "hi")

((string=? "abc" "abc") #t)
((string<? "abc" "abd") #t)
((string-ci=? "ABC" "abc") #t)
