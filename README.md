# Análise Léxica (Lexer/Scanner)

Nesta etapa iniciamos a construção do compilador MiniJava implementando a análise léxica, primeira fase do frontend da pipeline de compilação. Responsável por transformar o código-fonte em uma sequência de strings especiais, chamados de **tokens**. Esses símbolos serão de grande importância na etapa seguinte, sendo essa a análise sintática.

---

## 🧠 O que é Análise Léxica?

O principal foco do frontend do compilador é verificar se o código escrito na linguagem fonte apresenta algum erro (seja ele léxico, sintático ou semântico) e preparar certas estruturas que serão muito úteis para as etapas do backend da compilação.

A análise léxica se propõem a verificar se as palavras escritas no programa a ser compilado, são palavras válidas dentro da linguagem de programação especificada. Sendo assim, se o programa apresentar uma palavra que não faz parte da linguagem (ou seja, não possui um token correspondente), teremos a ocorrência de um erro léxico. Em resumo, a análise léxica consiste em:

> Converter uma sequência de caracteres em uma sequência de tokens

Os tokens podem serem entendidos como sendo uma unidade que segue o padrão (classe, valor). Onde o parâmetro classe faz referência ao tipo de token tratado, e o valor como sendo o próprio elemento em questão, quando necessário.

Exemplo (ignore a funcionalidade da função):

```java
float match0 (char *s) /* find a zero */
{if (!strncmp(s, "0.0", 3))
   return 0.;
}
```

Ao rodar um analisador léxico sobre o programa acima, podemos obter como exemplo:

```
FLOAT ID(match0) LPAREN CHAR STAR ID(s) RPAREN
LBRACE  IF LPAREN BANG ID(strncmp) LPAREN ID(s)
COMMA STRING(0.0) COMMA NUM(3) RPAREN RPAREN
RETURN REAL(0.0) SEMI RBRACE EOF
```

Perceba que existem tokens especificados somente pelo seu tipo (como `FLOAT`, `IF`, `LPAREN` etc) e tokens especificados por um tipo e valor (como `ID(match0)`, `NUM(3)`, `STRING(0.0)` etc), sendo essa informação extra, também chamado de **valor semântico**, importante para as análises posteriores.

Você pode se perguntar:

> "Como construir um programa que consiga **reconhecer** quais palavras pertencem ou não a uma linguagem de programação?"

Ora, perceba que o problema está concentrando em construir um **reconhecedor de linguagem**, e isso nada mais é do que um Autômato Finito Determinístico (AFD)!

A ideia é construir vários autômatos (um pra cada tipo de token) e então juntá-los em um autômato (opcionalmente) mínimo, onde dada uma string de entrada, o autômato irá dizer a qual classe de token essa string pertence (isso se houver um caminho de aceitação).

Por fim, é importante entender que os *tipos de tokens* são caracterizados pelas **expressões regulares (ER)**, e os *programas* são implementados pelos AFDs. 

---

## 🎯 Objetivo desta etapa

O foco do compilador nesta etapa é:

- Ler um arquivo da linguagem MiniJava, cuja extensão é o `.java`;
- Reconhecer palavras-chave, identificadores, operadores e símbolos;
- Ignorar espaços em branco e comentários;
- Produzir e exibir uma sequência de tokens.

📌 **Importante**:  
Nesta etapa não há, por enquanto, análise sintática nem semântica.

---

## 🛠 Geradores de Scanner

Como bem sabemos da teoria de autômatos e linguagens formais, existem algoritmos que traduzem uma ER diretamente (ou por intermédio de um AFnD) em um AFD. Por se tratar de um processo mecânico, é comum se utilizar de um gerador de scanner (JavaCC, SableCC, Lex, Flex etc) para gerar automaticamente um analisador léxico, dado as especificações dos tokens escritos usando ERs. O gerador utilizado nesse repositório é [JavaCC](https://javacc.github.io/javacc/), que além de gerar automaticamente um analisador léxico, ele gera também um analisador sintático (parser) dado a especificação da gramática.

📌 **Importante**:  
Apenas o arquivo `.jj` deve ser editado manualmente. Ao exercutarmos o JavaCC sobre esse arquivo, ele irá automaticamente gerar as classes responsáveis por fazer o reconhecimento dos tokens (AFDs)!

## 📁 Organização do Código

```bash
MinijavaCompiler/
├── Main.java                           # Arquivo principal 
├── src/
│   ├── lexer/                          # Módulo responsável pela Analise Léxica
│   │   └── MinijavaLexer.jj            # Arquivo a qual contém a definição dos tokens
│   │   └── *.java                      # Código gerado pelo JavaCC  
├── tests/                              
│   ├── *.java                          # Arquivos de testes de entrada
├── build/                              # Saída de compilação do programa compilador
├── .gitignore
└── README.md
```

## ▶️ Como executar

Para uma melhor compreensão de como o JavaCC funciona, basta conferir esse material, em inglês, de [introdução ao JavaCC](https://www.engr.mun.ca/~theo/JavaCC-Tutorial/javacc-tutorial.pdf) (ainda incompleto). Estando no mesmo diretório do arquivo `minijavalexer.jj`, basta executar o comando:

```bash
javacc minijavalexer.jj
```

A qual irá automaticamente gerar os seguintes 7 arquivos:

* `TokenMgrError.java`: Criada com o propósito de tratar os erros que podem aparecer pelo analisador léxico.
* `Token.java`: Representa os tokens gerados pelo analisador léxico.
* `ParseException.java`: Criada com o propósito de tratar os erros que podem aparecer pelo analisador sintático.
* `SimpleCharStream.java`: Representa a cadeia de caracteres de entrada. Classe adaptadora responsável por entregar os caracteres ao analisador léxico.
* `MiniJavaLexerConstants.java`: Interface que realiza a associação de padrões de tokens à nomes simbólicos.
* `MiniJavaLexerTokenManager.java`: O próprio analisador léxico.
* `MiniJavaLexer.java`: O próprio analisador sintático.

Para compilar o programa, estando na raíz do projeto, basta executar:

```bash
javac -d out Main.java src\lexer\*.java
```

Para executar o analisador léxico sobre um arquivo de teste, basta especificar seu caminho da seguinte maneira:

```bash
java -cp out Main <arquivo_de_teste.java>
```

### Exemplo

Dentro da pasta `tests` temos uma coleção de programas escritos em java que simulam a linguagem MiniJava. Considerando o arquivo `Factorial.java`:

```java
class Factorial{
    public static void main(String[] a){
	System.out.println(new Fac().ComputeFac(10));
    }
}

class Fac {

    public int ComputeFac(int num){
	int num_aux ;
	if (num < 1)
	    num_aux = 1 ;
	else 
	    num_aux = num * (this.ComputeFac(num-1)) ;
	return num_aux ;
    }

}
```

Ao executar o analisador léxico sobre esse arquivo, através do código:

```bash
java -cp out Main tests\Factorial.java
```

Temos:

```bash
Iniciando análise léxica...

Linha 1   | Coluna 1   | "class"              | "class"
Linha 1   | Coluna 7   | <ID>                 | "Factorial"
Linha 1   | Coluna 16  | "{"                  | "{"
Linha 2   | Coluna 5   | "public"             | "public"
Linha 2   | Coluna 12  | "static"             | "static"
Linha 2   | Coluna 19  | "void"               | "void"
Linha 2   | Coluna 24  | "main"               | "main"
Linha 2   | Coluna 28  | "("                  | "("
Linha 2   | Coluna 29  | "String"             | "String"
Linha 2   | Coluna 35  | "["                  | "["
Linha 2   | Coluna 36  | "]"                  | "]"
Linha 2   | Coluna 38  | <ID>                 | "a"
Linha 2   | Coluna 39  | ")"                  | ")"
Linha 2   | Coluna 40  | "{"                  | "{"
Linha 3   | Coluna 9   | "System.out.println" | "System.out.println"
Linha 3   | Coluna 27  | "("                  | "("
Linha 3   | Coluna 28  | "new"                | "new"
Linha 3   | Coluna 32  | <ID>                 | "Fac"
Linha 3   | Coluna 35  | "("                  | "("
Linha 3   | Coluna 36  | ")"                  | ")"
Linha 3   | Coluna 37  | "."                  | "."
Linha 3   | Coluna 38  | <ID>                 | "ComputeFac"
Linha 3   | Coluna 48  | "("                  | "("
Linha 3   | Coluna 49  | <INTEGER_LITERAL>    | "10"
Linha 3   | Coluna 51  | ")"                  | ")"
Linha 3   | Coluna 52  | ")"                  | ")"
Linha 3   | Coluna 53  | ";"                  | ";"
Linha 4   | Coluna 5   | "}"                  | "}"
Linha 5   | Coluna 1   | "}"                  | "}"
Linha 7   | Coluna 1   | "class"              | "class"
Linha 7   | Coluna 7   | <ID>                 | "Fac"
Linha 7   | Coluna 11  | "{"                  | "{"
Linha 9   | Coluna 5   | "public"             | "public"
Linha 9   | Coluna 12  | "int"                | "int"
Linha 9   | Coluna 16  | <ID>                 | "ComputeFac"
Linha 9   | Coluna 26  | "("                  | "("
Linha 9   | Coluna 27  | "int"                | "int"
Linha 9   | Coluna 31  | <ID>                 | "num"
Linha 9   | Coluna 34  | ")"                  | ")"
Linha 9   | Coluna 35  | "{"                  | "{"
Linha 10  | Coluna 9   | "int"                | "int"
Linha 10  | Coluna 13  | <ID>                 | "num_aux"
Linha 10  | Coluna 21  | ";"                  | ";"
Linha 11  | Coluna 9   | "if"                 | "if"
Linha 11  | Coluna 12  | "("                  | "("
Linha 11  | Coluna 13  | <ID>                 | "num"
Linha 11  | Coluna 17  | "<"                  | "<"
Linha 11  | Coluna 19  | <INTEGER_LITERAL>    | "1"
Linha 11  | Coluna 20  | ")"                  | ")"
Linha 12  | Coluna 13  | <ID>                 | "num_aux"
Linha 12  | Coluna 21  | "="                  | "="
Linha 12  | Coluna 23  | <INTEGER_LITERAL>    | "1"
Linha 12  | Coluna 25  | ";"                  | ";"
Linha 13  | Coluna 9   | "else"               | "else"
Linha 14  | Coluna 13  | <ID>                 | "num_aux"
Linha 14  | Coluna 21  | "="                  | "="
Linha 14  | Coluna 23  | <ID>                 | "num"
Linha 14  | Coluna 27  | "*"                  | "*"
Linha 14  | Coluna 29  | "("                  | "("
Linha 14  | Coluna 30  | "this"               | "this"
Linha 14  | Coluna 34  | "."                  | "."
Linha 14  | Coluna 35  | <ID>                 | "ComputeFac"
Linha 14  | Coluna 45  | "("                  | "("
Linha 14  | Coluna 46  | <ID>                 | "num"
Linha 14  | Coluna 49  | "-"                  | "-"
Linha 14  | Coluna 50  | <INTEGER_LITERAL>    | "1"
Linha 14  | Coluna 51  | ")"                  | ")"
Linha 14  | Coluna 52  | ")"                  | ")"
Linha 14  | Coluna 54  | ";"                  | ";"
Linha 15  | Coluna 9   | "return"             | "return"
Linha 15  | Coluna 16  | <ID>                 | "num_aux"
Linha 15  | Coluna 24  | ";"                  | ";"
Linha 16  | Coluna 5   | "}"                  | "}"
Linha 18  | Coluna 1   | "}"                  | "}"

Análise léxica concluída com sucesso!
```

## 📚 Referência

* Fundamentado no Capítulo 2 (*Lexical Analysis*) do livro *Modern Compiler Implementation in Java*, de Andrew W. Appel.
* Baseado também no tutorial oficial do JavaCC: https://javacc.github.io/javacc/tutorials/