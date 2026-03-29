;;; kleinlisp-mode.el --- Simple KleinLisp REPL integration -*- lexical-binding: t; -*-

;;; Commentary:
;; A simple comint-based REPL for KleinLisp without Geiser complexity.
;;
;; Usage:
;;   (load-file "~/.emacs.d/kleinlisp-mode.el")
;;   M-x run-kleinlisp
;;
;; In a .scm buffer:
;;   C-x C-e   - Evaluate expression before point
;;   C-c C-c   - Evaluate current top-level form
;;   C-c C-r   - Evaluate region
;;   C-c C-b   - Evaluate buffer
;;   C-c C-z   - Switch to REPL

;;; Code:

(require 'comint)
(require 'scheme)

(defgroup kleinlisp nil
  "KleinLisp REPL integration."
  :group 'languages)

(defcustom kleinlisp-program "/home/danilo/github/KleinLispWorkspace/KleinLisp/bin/kleinlisp"
  "Path to KleinLisp executable."
  :type 'string
  :group 'kleinlisp)

(defcustom kleinlisp-arguments '("--repl")
  "Arguments to pass to KleinLisp."
  :type '(repeat string)
  :group 'kleinlisp)

(defvar kleinlisp-buffer-name "*KleinLisp*"
  "Name of the KleinLisp REPL buffer.")

(defvar kleinlisp-prompt-regexp "^kleinlisp> "
  "Regexp matching the KleinLisp prompt.")

(define-derived-mode kleinlisp-repl-mode comint-mode "KleinLisp"
  "Major mode for KleinLisp REPL."
  (setq comint-prompt-regexp kleinlisp-prompt-regexp)
  (setq comint-prompt-read-only t)
  (setq mode-line-process '(":%s"))
  (scheme-mode-variables))

;;;###autoload
(defun run-kleinlisp ()
  "Start KleinLisp REPL."
  (interactive)
  (let ((buffer (get-buffer-create kleinlisp-buffer-name)))
    (unless (comint-check-proc buffer)
      (apply #'make-comint-in-buffer "KleinLisp" buffer kleinlisp-program nil kleinlisp-arguments)
      (with-current-buffer buffer
        (kleinlisp-repl-mode)))
    (pop-to-buffer buffer)))

(defun kleinlisp-get-process ()
  "Return the KleinLisp process, starting one if needed."
  (let ((proc (get-buffer-process kleinlisp-buffer-name)))
    (unless proc
      (run-kleinlisp)
      (setq proc (get-buffer-process kleinlisp-buffer-name))
      ;; Wait for REPL to start
      (sleep-for 0.5))
    proc))

(defun kleinlisp-send-string (string)
  "Send STRING to the KleinLisp REPL."
  (let ((proc (kleinlisp-get-process)))
    (comint-send-string proc string)
    (comint-send-string proc "\n")))

(defun kleinlisp-eval-last-sexp ()
  "Evaluate the expression before point."
  (interactive)
  (let ((end (point))
        (start (save-excursion
                 (backward-sexp)
                 (point))))
    (kleinlisp-send-string (buffer-substring-no-properties start end))
    (kleinlisp-show-repl)))

(defun kleinlisp-eval-defun ()
  "Evaluate the current top-level form."
  (interactive)
  (save-excursion
    (end-of-defun)
    (let ((end (point)))
      (beginning-of-defun)
      (kleinlisp-send-string (buffer-substring-no-properties (point) end))))
  (kleinlisp-show-repl))

(defun kleinlisp-eval-region (start end)
  "Evaluate the region from START to END."
  (interactive "r")
  (kleinlisp-send-string (buffer-substring-no-properties start end))
  (kleinlisp-show-repl))

(defun kleinlisp-eval-buffer ()
  "Evaluate the entire buffer."
  (interactive)
  (kleinlisp-send-string (buffer-substring-no-properties (point-min) (point-max)))
  (kleinlisp-show-repl))

(defun kleinlisp-show-repl ()
  "Show the REPL buffer without switching to it."
  (display-buffer kleinlisp-buffer-name))

(defun kleinlisp-switch-to-repl ()
  "Switch to the KleinLisp REPL buffer."
  (interactive)
  (pop-to-buffer (kleinlisp-get-process)))

;; Keybindings for scheme-mode
(defvar kleinlisp-minor-mode-map
  (let ((map (make-sparse-keymap)))
    (define-key map (kbd "C-x C-e") #'kleinlisp-eval-last-sexp)
    (define-key map (kbd "C-c C-c") #'kleinlisp-eval-defun)
    (define-key map (kbd "C-c C-r") #'kleinlisp-eval-region)
    (define-key map (kbd "C-c C-b") #'kleinlisp-eval-buffer)
    (define-key map (kbd "C-c C-z") #'kleinlisp-switch-to-repl)
    map)
  "Keymap for kleinlisp-minor-mode.")

;;;###autoload
(define-minor-mode kleinlisp-minor-mode
  "Minor mode for KleinLisp integration in Scheme buffers."
  :lighter " KL"
  :keymap kleinlisp-minor-mode-map)

;;;###autoload
(add-hook 'scheme-mode-hook #'kleinlisp-minor-mode)

(provide 'kleinlisp-mode)

;;; kleinlisp-mode.el ends here
