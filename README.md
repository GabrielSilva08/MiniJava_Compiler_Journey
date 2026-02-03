# Compilador MiniJava → MIPS

Este repositório tem como objetivo **servir como um guia prático e incremental para o estudo de compiladores**, combinando implementação real em Java com a teoria apresentada no livro _Modern Compiler Implementation in Java_ (Andrew W. Appel).

Ao invés de apresentar apenas um compilador finalizado, o projeto é organizado em **branches incrementais**, onde cada branch corresponde a uma etapa clássica do processo de compilação, construindo-se sempre sobre a etapa anterior.

A linguagem fonte utilizada é **MiniJava**, um subconjunto simples (mas não trivial) da linguagem Java, e a linguagem alvo é **MIPS assembly**.

---

## 🧱 Organização do Repositório

- A branch `main` funciona como um ponto de entrada e documentação geral do projeto.
- Cada etapa do compilador é implementada em **uma branch própria**.
- As branches são **incrementais**: cada uma parte diretamente da anterior.
- Cada branch é acompanhada por notas de aulas que visam explicar melhor a teoria por trás da etapa referente na compilação.

📌 **Importante**:  
Não é necessário entender todo o compilador para acompanhar o projeto. Cada branch é autocontida do ponto de vista de entendimento.

---

## 🌿 Etapas do Compilador (Branches)

| Etapa | Descrição | Branch | Status |
|------|----------|--------|--------|
| 1 | Análise Léxica | `step-01-lexer` | ⏳ Em andamento |
| 2 | Análise Sintática | `step-02-parser` | ❌ Não iniciado |
| 3 | Análise Semântica | `step-03-semantic-analysis` | ❌ Não iniciado |
| 4 | Tradução para Código Intermediário (IR) | `step-04-ir-translation` | ❌ Não iniciado |
| 5 | Seleção de Instruções | `step-05-instruction-selection` | ❌ Não iniciado |
| 6 | Alocação de Registradores | `step-06-register-allocation` | ❌ Não iniciado |
| 7 | **Compilador Completo** | `step-07-complete-compiler` | ❌ Não iniciado |

Cada branch possui:
- Código-fonte (devidamente comemtado) correspondente à etapa
- Notas de aula explicando a teoria envolvida
- Exemplos e instruções de execução

---

## 📚 Referência Teórica

Este projeto é fortemente baseado no livro:

> **Andrew W. Appel, Modern Compiler Implementation in Java**

A estrutura do compilador, os nomes das fases e várias decisões arquiteturais seguem o livro o mais fielmente possível, com adaptações quando necessário.

---

## 🚀 Como usar este repositório

1. Clone o repositório:
   
   ```bash
   git clone https://github.com/seu-usuario/MiniJava_Compiler_Journey.git
