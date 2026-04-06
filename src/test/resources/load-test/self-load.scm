; File that tries to load itself (direct cycle)
(define self-value 1)
(load "self-load.scm")
