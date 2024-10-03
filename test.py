a = ("(define (bar a b)\n"
     + " (+ a b))\n"
     + "\n"
     + "(define (foo a b)\n"
     + "  (println a)\n"
     + "  (println b)\n"
     + "  (bar a b))\n"
     + "\n"
     + "(foo 1 2)\n")

def bar(a, b):
    return a + b

def foo(a, b):
    print(a)
    print(b)
    return bar(a,b)

foo(1, "2")
