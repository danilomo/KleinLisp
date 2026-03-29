;;; Geiser protocol procedures for KleinLisp
;;; This file is auto-loaded when the REPL starts.

;; Convert any value to string representation
(define (geiser:any->string x)
  (cond
    ((and (not x) (not (boolean? x))) "#<unspecified>")  ; void
    ((null? x) "()")
    ((string? x) (string-append "\"" x "\""))
    ((number? x) (number->string x))
    ((boolean? x) (if x "#t" "#f"))
    ((symbol? x) (symbol->string x))
    ((pair? x) (string-append "(" (geiser:pair->string x) ")"))
    (else "#<object>")))

(define (geiser:pair->string p)
  (if (null? (cdr p))
      (geiser:any->string (car p))
      (if (pair? (cdr p))
          (string-append (geiser:any->string (car p)) " " (geiser:pair->string (cdr p)))
          (string-append (geiser:any->string (car p)) " . " (geiser:any->string (cdr p))))))

;; Helper to format result for Geiser protocol
;; Returns ((result "string-value") (output . ""))
(define (geiser:make-retort r)
  (list (list (string->symbol "result") (geiser:any->string r))
        (cons (string->symbol "output") "")))

;; Evaluation (required by Geiser)
;; Returns result in Geiser protocol format
(define (geiser:eval form . rest)
  (geiser:make-retort (eval form)))

(define (geiser:load-file file)
  (load file)
  (geiser:make-retort 'done))

(define (geiser:newline)
  (newline))

(define (geiser:no-values)
  (geiser:make-retort '()))

;; Completions
(define (geiser:completions prefix)
  (filter (lambda (s) (string-prefix? s prefix))
          (environment-symbols)))

(define (geiser:module-completions prefix)
  ;; No module system
  '())

;; Autodoc - returns ((name args...)) or ()
;; We don't have detailed arity information, so return empty
(define (geiser:autodoc . rest)
  (geiser:make-retort '()))

;; Symbol documentation - returns formatted documentation or #f
(define (geiser:symbol-documentation id)
  #f)

;; Module information stubs (no module system)
(define (geiser:module-exports mod)
  '())

(define (geiser:module-location mod)
  '())

(define (geiser:find-file file)
  #f)

(define (geiser:add-to-load-path path)
  #f)

;; Macroexpand - useful for debugging
(define (geiser:macroexpand form)
  form)  ; TODO: implement if macro expansion is exposed

;; Symbol location - returns (file . line) or #f
(define (geiser:symbol-location id)
  #f)
