# 1. ENTRE NA PASTA CORRETA DO PROJETO (O PASSO MAIS IMPORTANTE)
cd ChessGame-main

# 2. (Opcional) Verifique se está no lugar certo. Este comando deve mostrar as pastas src e resources
ls

# 3. Agora, execute o script de compilação e execução que já tínhamos
# Limpa a pasta de saída anterior
Remove-Item -Recurse -Force out -ErrorAction SilentlyContinue

# Cria a pasta de saída novamente
New-Item -ItemType Directory -Force .\out | Out-Null

# Compila o projeto (AGORA VAI FUNCIONAR!)
javac -d out -sourcepath src src/view/ChessGUI.java

# Executa o jogo
java -cp "out;resources" view.ChessGUI


As instruções acima são para que quem queira testar consiga rodar sem problemas pelo powershell, assim você garante que não terá um erro de diretório errado e tudo será compilado da melhor maneira 
