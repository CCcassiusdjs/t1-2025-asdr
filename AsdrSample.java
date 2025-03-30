import java.io.*;

public class AsdrSample {

  private static final int BASE_TOKEN_NUM = 301;
  
  // Definição dos tokens – ajuste os valores conforme necessário.
  public static final int IDENT   = 301;
  public static final int NUM     = 302;
  public static final int WHILE   = 303;
  public static final int IF      = 304;
  public static final int FI      = 305;
  public static final int ELSE    = 306;
  public static final int INT     = 307;
  public static final int FUNC    = 308;
  public static final int VOID    = 309;
  public static final int DOUBLE  = 310;
  public static final int BOOLEAN = 311;
  
  public static final String tokenList[] = {
      "IDENT", "NUM", "WHILE", "IF", "FI", "ELSE",
      "int", "FUNC", "VOID", "double", "boolean"
  };
  
  /* Referência ao objeto scanner gerado pelo JFlex */
  private Yylex lexer;
  public ParserVal yylval;
  
  // Variável global para armazenar o token lookahead.
  private static int laToken;
  private boolean debug;
  
  // Construtor
  public AsdrSample(Reader r) {
      lexer = new Yylex(r, this);
  }
  
  /*==================================================
   * Produções da Gramática C--
   * 
   * Prog         --> ListaDecl
   * ListaDecl    --> DeclVar ListaDecl
   *                | DeclFun ListaDecl
   *                | /* vazio *
   * DeclVar      --> Tipo ListaIdent ';'
   * Tipo         --> int | double | boolean
   * ListaIdent   --> IDENT (',' ListaIdent)?
   * DeclFun      --> FUNC TipoOuVoid IDENT '(' FormalPar ')' '{'
   *                    DeclVar ListaCmd '}'
   * TipoOuVoid   --> Tipo | VOID
   * FormalPar    --> paramList | /* vazio *
   * paramList    --> Tipo IDENT (',' paramList)?
   * Bloco        --> '{' ListaCmd '}'
   * ListaCmd     --> Cmd ListaCmd | /* vazio *
   * Cmd          --> Bloco
   *                | while '(' E ')' Cmd
   *                | IDENT '=' E ';'
   *                | if '(' E ')' Cmd RestoIf
   * RestoIf      --> else Cmd | /* vazio *
   * E            --> T { ('+'|'-') T }
   * T            --> F { ('*'|'/') F }
   * F            --> IDENT | NUM | '(' E ')'
   *==================================================*/
  
  // Prog --> ListaDecl
  private void Prog() {
      ListaDecl();
  }
  
  // ListaDecl --> DeclVar ListaDecl | DeclFun ListaDecl | /* vazio */
  private void ListaDecl() {
      while (laToken == INT || laToken == DOUBLE || laToken == BOOLEAN || laToken == FUNC) {
          if (laToken == FUNC)
              DeclFun();
          else
              DeclVar();
      }
  }
  
  // DeclVar --> Tipo ListaIdent ';'
  private void DeclVar() {
      if (laToken == INT || laToken == DOUBLE || laToken == BOOLEAN) {
          Tipo();
          ListaIdent();
          match(';');
      }
      // Se não estiver no FIRST de Tipo, a produção é vazia.
  }
  
  // Tipo --> int | double | boolean
  private void Tipo() {
      if (laToken == INT)
          match(INT);
      else if (laToken == DOUBLE)
          match(DOUBLE);
      else if (laToken == BOOLEAN)
          match(BOOLEAN);
      else
          error("Tipo esperado (int, double ou boolean) mas encontrado: " + tokenName(laToken));
  }
  
  // ListaIdent --> IDENT (',' ListaIdent)?
  private void ListaIdent() {
      match(IDENT);
      if (laToken == ',') {
          match(',');
          ListaIdent();
      }
  }
  
  // DeclFun --> FUNC TipoOuVoid IDENT '(' FormalPar ')' '{' DeclVar ListaCmd '}'
  private void DeclFun() {
      if (laToken == FUNC) {
          match(FUNC);
          TipoOuVoid();
          match(IDENT);
          match('(');
          FormalPar();
          match(')');
          match('{');
          // Dentro do corpo da função, podem vir declarações de variável (opcional)
          while (laToken == INT || laToken == DOUBLE || laToken == BOOLEAN) {
              DeclVar();
          }
          ListaCmd();
          match('}');
      }
  }
  
  // TipoOuVoid --> Tipo | VOID
  private void TipoOuVoid() {
      if (laToken == VOID)
          match(VOID);
      else
          Tipo();
  }
  
  // FormalPar --> paramList | /* vazio */
  private void FormalPar() {
      if (laToken == INT || laToken == DOUBLE || laToken == BOOLEAN)
          paramList();
      // Senão, produção vazia.
  }
  
  // paramList --> Tipo IDENT (',' paramList)?
  private void paramList() {
      Tipo();
      match(IDENT);
      if (laToken == ',') {
          match(',');
          paramList();
      }
  }
  
  // Bloco --> '{' ListaCmd '}'
  private void Bloco() {
      match('{');
      ListaCmd();
      match('}');
  }
  
  // ListaCmd --> Cmd ListaCmd | /* vazio */
  private void ListaCmd() {
      while (laToken == '{' || laToken == WHILE || laToken == IF || laToken == IDENT) {
          Cmd();
      }
  }
  
  // Cmd --> Bloco
  //       | while '(' E ')' Cmd
  //       | IDENT '=' E ';'
  //       | if '(' E ')' Cmd RestoIf
  private void Cmd() {
      if (laToken == '{') {
          Bloco();
      } else if (laToken == WHILE) {
          match(WHILE);
          match('(');
          E();
          match(')');
          Cmd();
      } else if (laToken == IDENT) {
          match(IDENT);
          match('=');
          E();
          match(';');
      } else if (laToken == IF) {
          match(IF);
          match('(');
          E();
          match(')');
          Cmd();
          RestoIf();
      } else {
          error("Comando inválido: " + tokenName(laToken));
      }
  }
  
  // RestoIf --> else Cmd | /* vazio */
  private void RestoIf() {
      if (laToken == ELSE) {
          match(ELSE);
          Cmd();
      }
  }
  
  // E --> T { ('+' | '-') T }
  private void E() {
      T();
      while (laToken == '+' || laToken == '-') {
          if (laToken == '+')
              match('+');
          else
              match('-');
          T();
      }
  }
  
  // T --> F { ('*' | '/') F }
  private void T() {
      F();
      while (laToken == '*' || laToken == '/') {
          if (laToken == '*')
              match('*');
          else
              match('/');
          F();
      }
  }
  
  // F --> IDENT | NUM | '(' E ')'
  private void F() {
      if (laToken == IDENT)
          match(IDENT);
      else if (laToken == NUM)
          match(NUM);
      else if (laToken == '(') {
          match('(');
          E();
          match(')');
      } else {
          error("F: token inesperado: " + tokenName(laToken));
      }
  }
  
  // Método match: consome o token esperado ou gera erro.
  private void match(int expected) {
      if (laToken == expected)
          nextToken();
      else
          error("Esperado " + tokenName(expected) + " mas encontrado " + tokenName(laToken));
  }
  
  // Método para obter o próximo token do lexer.
  private void nextToken() {
      try {
          yylval = new ParserVal();
          laToken = lexer.yylex();
      } catch (IOException e) {
          error("Erro de IO: " + e.getMessage());
      }
  }
  
  // Retorna o nome do token para mensagens de erro.
  private String tokenName(int token) {
      if (token < BASE_TOKEN_NUM)
          return Character.toString((char)token);
      else {
          int index = token - BASE_TOKEN_NUM;
          if (index >= 0 && index < tokenList.length)
              return tokenList[index];
          else
              return "token " + token;
      }
  }
  
  // Trata erros e encerra a execução.
  private void error(String msg) {
      System.err.println("Erro: " + msg);
      System.exit(1);
  }
  
  // Método main para testar o parser.
  public static void main(String[] args) {
      AsdrSample parser = null;
      try {
          Reader r;
          if (args.length == 0)
              r = new InputStreamReader(System.in);
          else
              r = new FileReader(args[0]);
          parser = new AsdrSample(r);
          parser.setDebug(false);
          laToken = parser.lexer.yylex();
          parser.Prog();
          if (laToken == Yylex.YYEOF)
              System.out.println("Sucesso na análise!");
          else
              System.out.println("Falhou - esperado EOF.");
      } catch (FileNotFoundException e) {
          System.out.println("File not found: " + args[0]);
      } catch (IOException e) {
          e.printStackTrace();
      }
  }
  
  public void setDebug(boolean d) {
      debug = d;
  }
}
