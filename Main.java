import java.io.FileInputStream;

public class Main {
    public static void main(String[] args) {
        // Verificação se foi passado algum arquivo fonte  
        if (args.length != 1) {
            System.out.println("Uso: java Main <arquivo.minijava>");
            return;
        }

        String filePath = args[0];

        try(FileInputStream fis = new FileInputStream(filePath)){
            // Criação do fluxo de caracteres
            SimpleCharStream stream = new SimpleCharStream(fis);
            
            // Criação do analisador léxico (Token Manager)
            MiniJavaLexerTokenManager scanner = new MiniJavaLexerTokenManager(stream);
            Token token;

            System.out.println("Iniciando análise léxica...\n");
            
            // Consumo do tokens até EOF
            while (true) { 
                token = scanner.getNextToken();

                if(token.kind == MiniJavaLexerConstants.EOF){
                    break;
                }

                // Impressão do token
                System.out.printf(
                    "Linha %-3d | Coluna %-3d | %-20s | \"%s\"%n",
                    token.beginLine,
                    token.beginColumn,
                    MiniJavaLexerConstants.tokenImage[token.kind],
                    token.image
                );
            }
            // Chegado aqui, não foi encontrado erro léxico!
            System.out.println("\nAnálise léxica concluída com sucesso!");      
        } catch (TokenMgrError e) {
            System.err.println("Erro léxico detectado:");
            System.err.println("   Arquivo: " + args[0]);
            System.err.println("   Mensagem: " + e.getMessage());
            System.exit(1);
        } catch (java.io.FileNotFoundException e) {
            System.err.println("Arquivo não encontrado:");
            System.err.println("   Arquivo: " + args[0]);
            System.err.println("   Mensagem: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Erro inesperado detectado:");
            System.err.println("   Arquivo: " + args[0]);
            System.err.println("   Tipo: " + e.getClass().getSimpleName());
            System.err.println("   Mensagem: " + e.getMessage());
            System.err.println("   Stack trace:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
