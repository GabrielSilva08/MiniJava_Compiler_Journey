@echo off
setlocal enabledelayedexpansion

echo ========================================
echo    MINIJAVA COMPILER - TEST SUITE
echo ========================================
echo.

REM Compile the project
echo Compilando o projeto...
javac -d build -cp src src\Main.java src\semantic\*.java src\semantic\SymbolTable\*.java src\syntaxtree\*.java src\visitor\*.java src\parser\*.java src\Tree\*.java src\Temp\*.java src\Translate\*.java src\frame\*.java src\mips\*.java src\util\*.java
if %errorlevel% neq 0 (
    echo ❌ Erro na compilação do projeto!
    pause
    exit /b 1
)
echo ✅ Projeto compilado com sucesso.
echo.

REM Initialize counters
set /a total=0
set /a passed=0
set /a failed=0
set /a errors=0

echo ========================================
echo    EXECUTANDO TESTES
echo ========================================
echo.

REM Loop through all .java files in input
for %%f in (input\*.java) do (
    set /a total+=1
    echo ----------------------------------------
    echo Teste !total!: %%~nxf
    echo ----------------------------------------
    
    REM Run the test and capture output
    java -cp build Main %%f > temp_output.txt 2>&1
    set exit_code=!errorlevel!
    
    if !exit_code! equ 0 (
        echo ✅ SUCESSO
        set /a passed+=1
        type temp_output.txt
    ) else (
        echo ❌ FALHOU (código de saída: !exit_code!)
        set /a failed+=1
        type temp_output.txt
    )
    echo.
)

REM Clean up temp file
del temp_output.txt 2>nul

echo ========================================
echo    RESUMO DOS TESTES
echo ========================================
echo.
echo Total de arquivos testados: %total%
echo ✅ Sucessos: %passed%
echo ❌ Falhas: %failed%
echo.
if %failed% equ 0 (
    echo 🎉 TODOS OS TESTES PASSARAM!
) else (
    echo ⚠️  %failed% teste(s) falharam.
)
echo.
pause
