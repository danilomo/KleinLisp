package net.sourceforge.kleinlisp.parser;

import net.sourceforge.kleinlisp.parser.java_cup.*;

%%
%unicode
%class SourceLexicalAnalyzer
%line
%column
%cup


inputchar	= [^\r\n]
lineterminator	= \r | \n | \r\n
simplecomment   = ";" {inputchar}* {lineterminator}
%{
private void error(){
    System.err.print("Sintax error on line " + (yyline+1));
    System.err.println(". Unrecognizable token: \"" + yytext() + "\"");
}

private Symbol symbol(int type) {
	return new Symbol(type, yyline, yycolumn);
}

private Symbol symbol(int type, Object value) {
	return new Symbol(type, yyline, yycolumn, value);
}
StringBuilder str = new StringBuilder();
int parCount = 0;
%}

%state SEXP
%state END

%%


<YYINITIAL> {
    {simplecomment}             {/* do nothing */}
    " "|\t|\n|{lineterminator}	{/* whitespace */}
    "("                         { parCount = 1; str.setLength(0); str.append('('); yybegin(SEXP); }
}

<SEXP> {
    "(" {
        parCount++;
        str.append( yytext() );
    }

    ")" {
        parCount--;
        str.append( yytext() );
        if(parCount == 0){
            yybegin(YYINITIAL);
            return symbol(sym.STRING_LITERAL, str.toString());            
        }
    }

    [^()]* { str.append( yytext() ); }
}

<END>{
	\n 	{}
	.	{}
}

