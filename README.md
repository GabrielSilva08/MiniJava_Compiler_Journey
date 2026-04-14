# 🛠️ Minijava Compiler

Projeto da disciplina **Construção de Compiladores** - Universidade Federal do Ceará (UFC)

Este projeto implementa um compilador completo da linguagem **Minijava** para a linguagem de máquina **MISP**, cobrindo todas as principais etapas de um processo moderno de compilação.

---

## 👨‍💻 Integrantes

- **Denis da Silva Victor**
- **Gabriel Silva Ribeiro**
- **Pedro Henrique de Oliveira Gomes**

---

## 📦 Etapas do Compilador

| Etapa                                    | Status         | Subetapas                                                  |
|-----------------------------------------|----------------|-------------------------------------------------------------|
| 1. Analisador Léxico e Sintático        | 🟢 Finalizado | - ✅ Analisador Léxico<br>- ✅ Analisador Sintático         |
| 2. AST e Análise Semântica              | 🟢 Finalizado | - ✅ Construção da AST<br>- ✅ Verificações Semânticas           |
| 3. Tradução para Código Intermediário   | 🟢 Finalizado | - ✅ Registros de Ativação<br>- ✅ Construção da IRTree<br>- ✅ Blocos básicos e Traços                   |
| 4. Seleção de Instruções                | 🟢 Finalizado | - ✅ Algoritmo de Ladrilhamento (Maximal Munch)<br>- ✅ Mapeamento para instruções MISP |
| 5. Análise de Longevidade e Alocação de Registradores            | 🟢 Finalizado | - ✅ Geração do grafo de fluxo<br>- ✅ Geração do grafo de interferência<br>- ✅ Mapeamento de variáveis para registradores               |
| 6. Integração e Geração do Código Final | 🟢 Finalizado | - ✅ Montagem final do código MISP<br>- ✅ Integração e testes   |

---

## 🧰 Tecnologias Utilizadas

- Java 17+
- JavaCC
- GitLab para versionamento e colaboração

---

## 📁 Estrutura do Projeto

```bash
MinijavaCompiler/
├── .vscode/                            # Arquivos de depuração
├── docs/                               # Documentação do projeto
│   └── compilation_process.txt         # Anotações rápidas do processo de compilação
├── src/
│   ├── parser/                         # Analise léxica e sintática
│   ├── syntaxtree/                     # Árvore sintática abstrata
│   ├── semantic/                       # Análise semântica
│   │   └── SymbolTable/                # Parte do gerador da Tabela de Símbolos
│   ├── Temp/                           # Temporários (Registradores)
│   ├── frame/                          # Registros de ativação 
│   ├── mips/                           # Interface própria para o MIPS
│   ├── Tree/                           # Classes da IRTree
│   ├── Translate/                      # Tradução para IR (Código intermediário)
│   ├── canon/                          # Canonização da IR
│   ├── Assem/                          # Interface para seleção de instruções
│   ├── Graph/                          # Classe de grafo abstrata
│   ├── FlowGraph/                      # Criação do grafo de fluxo 
│   ├── RegAlloc/                       # Criação do grafo de interferência
│   ├── visitor/                        # Implementação de visitors
│   ├── util/                           # Utilitários diversos
│   └── Main.java            
├── input/                              # Arquivos fontes de entrada
├── scripts/                            # Scripts auxiliares (build, exec, etc.)
│   ├── build.bat                       # Script de compilação do compilador
│   ├── run.bat                         # Script de execução do compilador
│   └── test_all.bat                    # Script de teste do compilador
├── build/                              # Saída de compilação
├── .gitignore
└── README.md
```

---

## 🚀 Como Executar

1. Para compilar o compilador basta executar na raiz do projeto,

```bash
scripts\build.bat
```

2. Para executar o compilador para um certo arquivo de entrada, basta modificar o argumento no arquivo `run.bat` e executar na raiz do projeto,

```bash
scripts\run.bat
```

**OBS: Conforme os outros módulos do compilador vão sendo implementados, sempre manter esses arquivos `.bat` atualizados!**
