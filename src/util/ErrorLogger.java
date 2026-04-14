package util;

import java.util.ArrayList;
import java.util.List;

public class ErrorLogger {
    private static List<Error> errors = new ArrayList<>();
    private static List<Warning> warnings = new ArrayList<>();
    
    public static class Error {
        public String message;
        public String file;
        public int line;
        public int column;
        public String type;
        
        public Error(String type, String message, String file, int line, int column) {
            this.type = type;
            this.message = message;
            this.file = file;
            this.line = line;
            this.column = column;
        }
        
        @Override
        public String toString() {
            return String.format("❌ %s: %s (linha %d, coluna %d)", type, message, line, column);
        }
    }
    
    public static class Warning {
        public String message;
        public String file;
        public int line;
        
        public Warning(String message, String file, int line) {
            this.message = message;
            this.file = file;
            this.line = line;
        }
        
        @Override
        public String toString() {
            return String.format("⚠️  %s (linha %d)", message, line);
        }
    }
    
    public static void logError(String type, String message, String file, int line, int column) {
        errors.add(new Error(type, message, file, line, column));
        System.err.println(String.format("❌ %s: %s (linha %d, coluna %d)", type, message, line, column));
    }
    
    public static void logError(String type, String message) {
        errors.add(new Error(type, message, "unknown", 0, 0));
        System.err.println("❌ " + type + ": " + message);
    }
    
    public static void logWarning(String message, String file, int line) {
        warnings.add(new Warning(message, file, line));
        System.err.println("⚠️  " + message + " (linha " + line + ")");
    }
    
    public static void logWarning(String message) {
        warnings.add(new Warning(message, "unknown", 0));
        System.err.println("⚠️  " + message);
    }
    
    public static boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public static boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    public static int getErrorCount() {
        return errors.size();
    }
    
    public static int getWarningCount() {
        return warnings.size();
    }
    
    public static void printSummary() {
        System.err.println("\n=== RESUMO DE ERROS E AVISOS ===");
        System.err.println("Erros: " + getErrorCount());
        System.err.println("Avisos: " + getWarningCount());
        
        if (hasErrors()) {
            System.err.println("\n--- ERROS ---");
            for (Error error : errors) {
                System.err.println(error.toString());
            }
        }
        
        if (hasWarnings()) {
            System.err.println("\n--- AVISOS ---");
            for (Warning warning : warnings) {
                System.err.println(warning.toString());
            }
        }
    }
    
    public static void clear() {
        errors.clear();
        warnings.clear();
    }
} 