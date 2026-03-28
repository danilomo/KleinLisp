;; Numeric test cases
;; Format: (expression expected-result)

((+ 1 2 3) 6)
((- 10 3 2) 5)
((* 2 3 4) 24)
((/ 24 4 2) 3)

((floor 3.7) 3.0)
((ceiling 3.2) 4.0)
((truncate -3.7) -3.0)
((round 2.5) 2.0)

((quotient 13 4) 3)
((remainder 13 4) 1)
((modulo -13 4) 3)

((gcd 12 18) 6)
((lcm 4 6) 12)

((sqrt 16) 4.0)
((expt 2 10) 1024)

((exact? 5) #t)
((inexact? 5.0) #t)

((integer? 5) #t)
((integer? 5.0) #t)
((integer? 5.5) #f)
