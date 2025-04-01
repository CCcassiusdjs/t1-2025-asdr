/* Alunos Ana Carolina Xavier, Cássio Dhein e Érico Bis */
/* arquivo JFlex para o analisador léxico da linguagem C-- */

%%

%class Yylex
%unicode
%line
%column
%public

%{
  private AsdrSample yyparser;

  public Yylex(java.io.Reader r, AsdrSample yyparser) {
    this(r);
    this.yyparser = yyparser;
  }
%}

%integer

WHITE_SPACE_CHAR = [ \t\n\r\f]+
IDENTIFIER       = [:jletter:][:jletterdigit:]*
NUMBER           = [0-9]+(\.[0-9]+)?

%%

"$TRACE_ON"   { yyparser.setDebug(true); }
"$TRACE_OFF"  { yyparser.setDebug(false); }

"func"       { return AsdrSample.FUNC; }
"int"        { return AsdrSample.INT; }
"double"     { return AsdrSample.DOUBLE; }
"boolean"    { return AsdrSample.BOOLEAN; }
"void"       { return AsdrSample.VOID; }
"while"      { return AsdrSample.WHILE; }
"if"         { return AsdrSample.IF; }
"else"       { return AsdrSample.ELSE; }

{IDENTIFIER}  { return AsdrSample.IDENT; }
{NUMBER}      { return AsdrSample.NUM; }

"{"          { return '{'; }
"}"          { return '}'; }
";"          { return ';'; }
"("          { return '('; }
")"          { return ')'; }
","          { return ','; }
"+"          { return '+'; }
"-"          { return '-'; }
"*"          { return '*'; }
"/"          { return '/'; }
"="          { return '='; }

{WHITE_SPACE_CHAR} { /* ignora espaços em branco */ }

.            { System.err.println("Erro léxico: caractere inválido '" + yytext() + "' na linha " + yyline); }
