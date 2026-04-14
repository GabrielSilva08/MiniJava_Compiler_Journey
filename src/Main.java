import RegAlloc.Liveness;
import RegAlloc.RegisterAllocator;
import Translate.Exp;
import Tree.*;
import canon.BasicBlocks;
import canon.Canon;
import canon.TraceSchedule;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.Map;

import parser.*;
import semantic.*;
import semantic.SymbolTable.Symbol;
import semantic.SymbolTable.Table;
import syntaxtree.*;
import util.ErrorLogger;
import visitor.*;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java Main <arquivo.minijava>");
            return;
        }

        try {
            FileInputStream in = new FileInputStream(args[0]);
            MinijavaParser parser = new MinijavaParser(in);
            
            System.out.println("=== COMPILAÇÃO: " + args[0] + " ===");
            
            // 1. Análise léxica + análise sintática
            Program prog = parser.parse(); // retorna a AST
            System.out.println("+ Parsing concluído com sucesso.");

            // Visitor para imprimir a AST
            //prog.accept(new PrettyPrintVisitor());

            // 2. Análise semântica
            System.out.println("\n--- Análise Semântica ---");
            BuildSymbolTableVisitor symbolBuilder = new BuildSymbolTableVisitor();
            prog.accept(symbolBuilder);
            Table symbolTable = symbolBuilder.getSymbolTable();
            System.out.println("+ Tabela de símbolos construída com sucesso.");

            TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable);
            prog.accept(typeChecker);
            
            // Checagem da presença de algum erro de tipo
            if (ErrorLogger.hasErrors()) {
                System.out.println("- ERROS DE TIPO ENCONTRADOS:");
                ErrorLogger.printSummary();
                System.exit(1);
            }
            System.out.println("+ Verificação de tipos concluída.");

            // 3. Tradução - criar TranslateVisitor
            System.out.println("\n--- Geração de IR ---");
            TranslateVisitor translateVisitor = new TranslateVisitor();

            // Aplicar o visitor para gerar IR
            prog.accept(translateVisitor);

            // Pegar a expressão traduzida (Exemplo: última expressão do programa)
            Exp resultExp = translateVisitor.exp;

            if (resultExp != null) {
                System.out.println("+ Expressão intermediária gerada:");
                System.out.println("   Tipo: " + resultExp.getClass().getSimpleName());
                System.out.println("   Valor: " + resultExp.unEx().toString());
            } else {
                System.out.println("!  Nenhuma expressão intermediária foi gerada.");
            }

            // 4. Canonização - criar ir canonica
            System.out.println("\n--- Geração de IR CANONICA---");
            Stm stm = resultExp.unNx();
            StmList canonicalList = Canon.linearize(stm);
            if (canonicalList != null){
                System.out.println("Stm da arvore canonica gerada. ");
                // Percorrer e imprimir
                for (StmList l = canonicalList; l != null; l = l.tail) {
                    System.out.println(l.head);
                }
            }


            // 4.1 Criar blocos básicos
            BasicBlocks blocks = new BasicBlocks(canonicalList);
            System.out.println("+ Blocos Básicos Criados.");

            // 4.2 Criar Trace Schedule
            TraceSchedule schedule = new TraceSchedule(blocks);
            System.out.println("+ Trace Schedule Finalizado.");

            // 4.3 Percorrer a lista final
            System.out.println("\n--- IR Final com Traços ---");
            StmList finalList = schedule.stms;
            for (StmList l = finalList; l != null; l = l.tail) {
                System.out.println(l.head);
            }

            // 5. Seleção de instruções

           System.out.println("\n--- Seleção de Instruções (MIPS) ---");

           frame.Frame frame = new mips.MipsFrame(Symbol.symbol("main"), new LinkedList<>());

            // Junta os Stm da StmList em uma única SEQ (SEQ(s1, SEQ(s2, ...)))
            Stm fullBody = null;
            for (StmList l = finalList; l != null; l = l.tail) {
                fullBody = (fullBody == null) ? l.head : new SEQ(fullBody, l.head);
            }

            // Gera instruções usando o algoritmo de tiling, Maximal Munch
            Assem.InstrList instrs = new mips.Codegen(frame).codegen(fullBody);

            // Aplica prólogo e epílogo (parte da abstração da máquina)
            java.util.List<Assem.Instr> instrList = new LinkedList<>();
            for (Assem.InstrList l = instrs; l != null; l = l.tail) {
                instrList.add(l.head);
            }

            frame.procEntryExit1(null); // não modifica a lista, pois `body` é local
            frame.procEntryExit2(instrList);
            frame.procEntryExit3(instrList);
            

            // Imprime instruções formatadas
            Temp.TempMap tempMap = new Temp.DefaultMap();
            System.out.println("\n--- MIPS Assembly ---");
            for (Assem.Instr instr : instrList) {
                System.out.print(instr.format(tempMap));
            }

            // 6. Análise de longevidade

            // 6.1 Criação do grafo de fluxo de controle do programa

            FlowGraph.AssemFlowGraph flowGraph = new FlowGraph.AssemFlowGraph(instrs);
            System.out.println("\n+ Grafo de fluxo de controle criado com sucesso.");

            // Impressão do grafo direcionado

            System.out.println("\n--- Grafo de Fluxo de Controle ---");
            flowGraph.show(System.out);

            // 6.2 Criação do grafo de interferência
            Liveness liveness = new Liveness(flowGraph);
            System.out.println("\n+ Grafo de interferência criado com sucesso.");

            // Impressão do grafo de interferência

            System.out.println("\n--- Grafo de Interferência ---");
            liveness.show(System.out); // Exibe o grafo de interferência no console
            System.out.println();
            liveness.printMatrix(); // Exibe a matriz de adjacência do grafo de interferência

            System.out.println("\n+ Análise de longevidade concluída.");

             // 7. Alocação de registradores
            System.out.println("\n--- Alocação de Registradores ---");

            // Aloca registradores com 32 cores disponíveis
            RegisterAllocator allocator = new RegisterAllocator(liveness, 32);
            allocator.allocate();

            // Mapeia os temps para registradores físicos
            Map<Temp.Temp, Integer> colorMap = allocator.getColoring();

            // Mapeia os nomes reais (para impressão final)
            Temp.TempMap registerMap = new Temp.DefaultMap() {
                @Override
                public String tempMap(Temp.Temp temp) {
                    if (colorMap.containsKey(temp)) {
                        return "$r" + colorMap.get(temp); // substitui por registrador real
                    }
                    return super.tempMap(temp); // fallback para temporários não alocados
                }
            };

            // Reimpressão do código com registradores reais
            System.out.println("\n--- MIPS Assembly Final (com registradores reais) ---");
            for (Assem.Instr instr : instrList) {
                System.out.print(instr.format(registerMap));
            }

            System.out.println("\n--- Alocação de registradores concluída. ---");

            System.out.println("\n=== COMPILAÇÃO CONCLUÍDA ===");

        } catch (parser.ParseException e) {
            System.err.println("- ERRO DE SINTAXE:");
            System.err.println("   Arquivo: " + args[0]);
            System.err.println("   Linha: " + e.currentToken.beginLine);
            System.err.println("   Coluna: " + e.currentToken.beginColumn);
            System.err.println("   Token: " + e.currentToken.image);
            System.err.println("   Mensagem: " + e.getMessage());
            System.exit(1);
        } catch (parser.TokenMgrError e) {
            System.err.println("- ERRO LÉXICO:");
            System.err.println("   Arquivo: " + args[0]);
            System.err.println("   Mensagem: " + e.getMessage());
            System.exit(1);
        } catch (java.io.FileNotFoundException e) {
            System.err.println("- ARQUIVO NÃO ENCONTRADO:");
            System.err.println("   Arquivo: " + args[0]);
            System.err.println("   Mensagem: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("- ERRO INESPERADO:");
            System.err.println("   Arquivo: " + args[0]);
            System.err.println("   Tipo: " + e.getClass().getSimpleName());
            System.err.println("   Mensagem: " + e.getMessage());
            System.err.println("   Stack trace:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
