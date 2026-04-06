package net.sourceforge.kleinlisp.parser;

import java_cup.runtime.*;
import net.sourceforge.kleinlisp.*;
import net.sourceforge.kleinlisp.objects.CharObject;

%%
%unicode
%class LexicalAnalyzer
%line
%column
%cup

/*number*/
double_literal          = [-]?([1-9][0-9]*|0)([.][0-9]*)([eE]([+]|[-])?[0-9]+)?
integer_literal         = [-]?([1-9][0-9]*|0)
digit		= [0-9]
symbol          = 
                    [+] | [%] | [/] |
                    [*] | [$] | [_] | [:] | [>] | [<] | [=] |
                    [!] | [\?] | [.]
minus           = [-]

letter		= {lowercase} | {uppercase}
lowercase	= [a-z]
uppercase	= [A-Z]

lineterminator	= \r | \n | \r\n

identifier	= ({letter}|{symbol}) ({letter}|{digit}|{symbol}|{minus})*
keyword         = "#:" ({letter}|{symbol}) ({letter}|{digit}|{symbol}|{minus})*

%{
private void error(){    
    throw new SyntaxError(String.format("Unrecognizable token: '%s' at line %d, column %d", yytext(), yyline, yycolumn));
}

private Symbol symbol(int type) {
	return new Symbol(type, yyline, yycolumn);
}

private Symbol symbol(int type, Object value) {
	return new Symbol(type, yyline, yycolumn, value);
}
StringBuilder str = new StringBuilder();
StringBuilder barredSymbol = new StringBuilder();
%}

%state STRING
%state BARRED_SYMBOL
%state END

%%

<YYINITIAL> {
    "\""                            {str.setLength(0); yybegin(STRING);}
    "|"                             {barredSymbol.setLength(0); yybegin(BARRED_SYMBOL);}
    "'"                             { return symbol( sym.QUOTE ); }
    "`"                             { return symbol( sym.QUASIQUOTE ); }
    ",@"                            { return symbol( sym.UNQUOTE_SPLICING ); }
    ","                             { return symbol( sym.UNQUOTE ); }
    "#("                            { return symbol( sym.VECTOR_OPEN ); }
    "("                             { return symbol( sym.OPEN_PAR ); }
    ")"                             { return symbol( sym.CLOSE_PAR ); }
    "["                             { return symbol( sym.OPEN_BRACKET ); }
    "]"                             { return symbol( sym.CLOSE_BRACKET ); }
    "."                             { return symbol( sym.DOT ); }
    "-"                             { return symbol( sym.ATOM, "-" ); }
    "#t"                            { return symbol( sym.BOOL_LITERAL, true ); }
    "#f"                            { return symbol( sym.BOOL_LITERAL, false ); }

    // Character literals - named characters (must come before single char pattern)
    "#\\space"                      { return symbol( sym.CHAR_LITERAL, new CharObject(' ') ); }
    "#\\newline"                    { return symbol( sym.CHAR_LITERAL, new CharObject('\n') ); }
    "#\\tab"                        { return symbol( sym.CHAR_LITERAL, new CharObject('\t') ); }
    "#\\return"                     { return symbol( sym.CHAR_LITERAL, new CharObject('\r') ); }
    "#\\null"                       { return symbol( sym.CHAR_LITERAL, new CharObject('\0') ); }
    "#\\alarm"                      { return symbol( sym.CHAR_LITERAL, new CharObject('\007') ); }
    "#\\backspace"                  { return symbol( sym.CHAR_LITERAL, new CharObject('\b') ); }
    "#\\escape"                     { return symbol( sym.CHAR_LITERAL, new CharObject('\033') ); }
    "#\\delete"                     { return symbol( sym.CHAR_LITERAL, new CharObject('\177') ); }

    // Hex character literal #\xNN
    "#\\x"[0-9a-fA-F]+              {
        String hex = yytext().substring(3);
        int codepoint = Integer.parseInt(hex, 16);
        return symbol( sym.CHAR_LITERAL, new CharObject((char) codepoint) );
    }

    // Single character literal #\c (must come after named characters)
    "#\\"[^\s]                      { return symbol( sym.CHAR_LITERAL, new CharObject(yytext().charAt(2)) ); }

    {keyword}                       { return symbol( sym.KEYWORD, yytext().substring(2) ); }
    {identifier}                    { return symbol( sym.ATOM,  yytext() ); }
    {integer_literal}               { return symbol( sym.INT_LITERAL,  Integer.parseInt(yytext()) ); }
    {double_literal}                { return symbol( sym.DOUBLE_LITERAL,  Double.parseDouble(yytext()) ); }
    " "|\t|\n|	{lineterminator}    {/* whitespace */}
    ";"[^\r\n]*                     {/* single-line comment */}
}

<STRING> {
	\"              { yybegin(YYINITIAL); return symbol(sym.STRING_LITERAL, str.toString()); }
	\\t             { str.append('\t'); }
	\\n             { str.append('\n'); }
	\\r             { str.append('\r'); }
	\\\"            { str.append('\"'); }
	\\\\            { str.append('\\'); }
	\\[']			{ str.append('\''); }
	\\[0-9][0-9][0-9]
	{
		String s = yytext().substring(1);
		s = "" + ((char) Integer.parseInt(s));
		str.append( s );
	}
	[^\n\r\"\\\t]+    { str.append( yytext() ); }
	.               { /* Malformed string */}
}

<BARRED_SYMBOL> {
	"|"             { yybegin(YYINITIAL); return symbol(sym.ATOM, barredSymbol.toString()); }
	"\\|"           { barredSymbol.append('|'); }
	"\\\\"          { barredSymbol.append('\\'); }
	"\\n"           { barredSymbol.append('\n'); }
	"\\t"           { barredSymbol.append('\t'); }
	"\\r"           { barredSymbol.append('\r'); }
	[^|\\]+         { barredSymbol.append(yytext()); }
	.               { barredSymbol.append(yytext()); }
}

<END>{
	\n 	{}
	.	{}
}

. { error(); }