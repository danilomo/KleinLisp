;;; R7RS Time Functions Tests (scheme.time)
;;;
;;; Tests for current-second, current-jiffy, and jiffies-per-second
;;; Note: Test parser has limitations with nested function calls,
;;; so we use simple literal comparisons where possible

;; jiffies-per-second should return exactly 1 billion (nanoseconds per second)
((jiffies-per-second) 1000000000)
