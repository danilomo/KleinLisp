package net.sourceforge.kleinlisp.parser;

import java_cup.runtime.*;
import net.sourceforge.kleinlisp.*;

%%
%unicode
%class LexicalAnalyzer
%line
%column
%cup

/*number*/
double_literal          = [-]?([1-9][0-9]*|0)([.][0-9]* )([eE]([+]|[-])?[0-9]+)?
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
%}

%state STRING
%state END

%%

<YYINITIAL> {
    "\""                            {str.setLength(0); yybegin(STRING);}
    "'"                             { return symbol( sym.QUOTE ); }
    "("                             { return symbol( sym.OPEN_PAR ); }
    ")"                             { return symbol( sym.CLOSE_PAR ); }
    "["                             { return symbol( sym.OPEN_BRACKET ); }
    "]"                             { return symbol( sym.CLOSE_BRACKET ); }
    "."                             { return symbol( sym.DOT ); }
    "-"                             { return symbol( sym.ATOM, "-" ); }

    {identifier}                    { return symbol( sym.ATOM,  yytext() ); }
    {integer_literal}               { return symbol( sym.INT_LITERAL,  Integer.parseInt(yytext()) ); }
    {double_literal}                { return symbol( sym.DOUBLE_LITERAL,  Double.parseDouble(yytext()) ); }
    " "|\t|\n|	{lineterminator}    {/* whitespace */}
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

<END>{
	\n 	{}
	.	{}
}

. { error(); }