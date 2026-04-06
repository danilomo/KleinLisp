; File B that loads File A, creating a cycle
(define cycle-b-before-load 10)
(load "cycle-a.scm")
(define cycle-b-after-load 20)
