;; Library Tests - Extracted from Chicken Scheme library-tests.scm
;; Tests for rounding functions
;; Format: ((expression) expected-result)

((round -4.3) -4.0)
((round 3.5) 4.0)
((round 7) 7)
((round -0.5) 0.0)
((round -0.3) 0.0)
((round -0.6) -1.0)
((round 0.5) 0.0)
((round 0.3) 0.0)
((round 0.6) 1.0)
