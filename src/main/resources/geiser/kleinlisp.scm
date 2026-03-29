;;; Geiser protocol procedures for KleinLisp
;;; This file is auto-loaded when the REPL starts.

;; Evaluation (required by Geiser)
(define (geiser:eval module form . rest)
  ;; module ignored - KleinLisp has no module system
  (eval form))

(define (geiser:load-file file)
  (load file))

(define (geiser:newline)
  (newline))

(define (geiser:no-values)
  '())

;; Completions
(define (geiser:completions prefix)
  (filter (lambda (s) (string-prefix? s prefix))
          (environment-symbols)))

(define (geiser:module-completions prefix)
  ;; No module system
  '())

;; Autodoc - returns ((name args...)) or ()
;; We don't have detailed arity information, so return empty
(define (geiser:autodoc ids)
  '())

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
