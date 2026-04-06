;;; geiser-kleinlisp.el --- Geiser backend for KleinLisp -*- lexical-binding: t; -*-

;; Copyright (c) 2018 Danilo Oliveira

;; Author: Danilo Oliveira
;; Maintainer: Danilo Oliveira
;; Keywords: languages, lisp, scheme
;; Homepage: https://github.com/danilomo/KleinLisp
;; Package-Requires: ((emacs "25.1") (geiser "0.28"))
;; Version: 0.1.0

;; This file is NOT part of GNU Emacs.

;; MIT License

;;; Commentary:

;; Geiser backend for KleinLisp, a Scheme-like Lisp implementation in Java.
;;
;; Setup:
;;   (add-to-list 'load-path "/path/to/kleinlisp/emacs")
;;   (require 'geiser-kleinlisp)
;;
;; Then use M-x run-kleinlisp to start a REPL, or M-x geiser to select KleinLisp.

;;; Code:

(require 'geiser-impl)
(require 'geiser-custom)
(require 'geiser-base)
(require 'geiser-syntax)
(require 'geiser)

(require 'compile)

;;; Customization

(defgroup geiser-kleinlisp nil
  "Customization for Geiser's KleinLisp backend."
  :group 'geiser)

(geiser-custom--defcustom geiser-kleinlisp-binary "kleinlisp"
  "Path to KleinLisp executable or wrapper script."
  :type 'string
  :group 'geiser-kleinlisp)

(geiser-custom--defcustom geiser-kleinlisp-default-port 37146
  "Default port for connecting to KleinLisp REPL server."
  :type 'integer
  :group 'geiser-kleinlisp)

;;; Implementation

(defconst geiser-kleinlisp--prompt-regexp "^kleinlisp> ")

(defun geiser-kleinlisp--binary ()
  "Return the path to the KleinLisp binary."
  (if (listp geiser-kleinlisp-binary)
      (car geiser-kleinlisp-binary)
    geiser-kleinlisp-binary))

(defun geiser-kleinlisp--parameters ()
  "Return command-line parameters for starting KleinLisp."
  (let ((binary geiser-kleinlisp-binary))
    (if (listp binary)
        (append (cdr binary) '("--repl"))
      '("--repl"))))

(defun geiser-kleinlisp--geiser-procedure (proc &rest args)
  "Transform PROC and ARGS into a KleinLisp procedure call."
  (cl-case proc
    ((eval compile)
     (let ((form (cadr args)))
       ;; Use geiser:eval which returns structured result
       (format "(geiser:eval '%s)" form)))
    ((load-file compile-file)
     (format "(geiser:load-file %S)" (car args)))
    ((completions)
     (format "(geiser:completions %S)" (car args)))
    ((autodoc)
     "(geiser:autodoc)")
    ((no-values)
     "(geiser:no-values)")
    (t
     "'()")))

(defun geiser-kleinlisp--get-module (&optional module)
  "Return MODULE or nil (KleinLisp has no module system)."
  nil)

(defun geiser-kleinlisp--symbol-begin (module)
  "Return the beginning of the symbol at point."
  (save-excursion
    (skip-syntax-backward "w_")
    (point)))

(defun geiser-kleinlisp--exit-command ()
  "Return the command to exit KleinLisp."
  "(exit)")

(defun geiser-kleinlisp--startup (_remote)
  "Perform startup actions for the KleinLisp REPL.
The geiser support code is auto-loaded by KleinLisp at REPL startup."
  t)

(defun geiser-kleinlisp--version (binary)
  "Return the version of BINARY."
  "0.1.0")

(defun geiser-kleinlisp--keywords ()
  "Return KleinLisp-specific keywords for syntax highlighting."
  '(("define" "lambda" "let" "let*" "letrec" "letrec*"
     "if" "cond" "case" "when" "unless" "else"
     "begin" "do" "and" "or" "not"
     "quote" "quasiquote" "unquote" "unquote-splicing"
     "set!" "define-syntax" "let-syntax" "letrec-syntax"
     "syntax-rules" "guard" "delay" "force"
     "parameterize" "define-values" "let-values" "let*-values"
     "values" "call-with-values")
    ("lambda" "define" "let" "let*" "letrec" "letrec*"
     "let-values" "let*-values" "define-values")))

(defun geiser-kleinlisp--case-sensitive ()
  "Return t if KleinLisp is case-sensitive."
  t)

(defun geiser-kleinlisp--display-error (_module key msg)
  "Display error with KEY and MSG."
  (when (stringp msg)
    (newline)
    (insert msg))
  (and (not key) msg (not (zerop (length msg)))))

;;; REPL integration

(defun run-kleinlisp ()
  "Start a Geiser KleinLisp REPL."
  (interactive)
  (run-geiser 'kleinlisp))

(defun connect-to-kleinlisp (&optional host port)
  "Connect to a running KleinLisp REPL server.

Start the server with: java -jar KleinLisp.jar --listen [port]

HOST defaults to localhost.
PORT defaults to `geiser-kleinlisp-default-port' (37146)."
  (interactive
   (list (read-string "Host (default localhost): " nil nil "localhost")
         (read-number "Port: " geiser-kleinlisp-default-port)))
  (let ((host (or host "localhost"))
        (port (or port geiser-kleinlisp-default-port)))
    (geiser-connect 'kleinlisp host port)))

;;; Register the implementation

(define-geiser-implementation kleinlisp
  (binary geiser-kleinlisp--binary)
  (arglist geiser-kleinlisp--parameters)
  (version-command geiser-kleinlisp--version)
  (repl-startup geiser-kleinlisp--startup)
  (prompt-regexp geiser-kleinlisp--prompt-regexp)
  (debugger-prompt-regexp nil)
  (marshall-procedure geiser-kleinlisp--geiser-procedure)
  (find-module geiser-kleinlisp--get-module)
  (enter-command nil)
  (exit-command geiser-kleinlisp--exit-command)
  (import-command nil)
  (find-symbol-begin geiser-kleinlisp--symbol-begin)
  (display-error geiser-kleinlisp--display-error)
  (external-help nil)
  (check-buffer nil)
  (keywords geiser-kleinlisp--keywords)
  (case-sensitive geiser-kleinlisp--case-sensitive))

(geiser-impl--add-to-alist 'regexp "\\.scm$" 'kleinlisp t)
(geiser-impl--add-to-alist 'regexp "\\.ss$" 'kleinlisp t)

(provide 'geiser-kleinlisp)

;;; geiser-kleinlisp.el ends here
