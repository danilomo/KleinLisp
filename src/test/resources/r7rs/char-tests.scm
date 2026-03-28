;; Character test cases

((char? #\a) #t)
((char? "a") #f)

((char=? #\a #\a) #t)
((char<? #\a #\b) #t)
((char-ci=? #\A #\a) #t)

((char-alphabetic? #\a) #t)
((char-numeric? #\5) #t)
((char-whitespace? #\space) #t)

((char->integer #\A) 65)
((integer->char 97) #\a)
((char-upcase #\a) #\A)
((char-downcase #\A) #\a)
