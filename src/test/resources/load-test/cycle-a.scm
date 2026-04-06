; File A that loads File B, which in turn loads File A (cycle)
(define cycle-a-before-load 1)
(load-relative "cycle-b.scm")
(define cycle-a-after-load 2)
