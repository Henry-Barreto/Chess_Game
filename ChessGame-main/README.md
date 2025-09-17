
# ChessGame

ChessGame é um projeto de jogo de xadrez desenvolvido em Java, com interface gráfica baseada em Swing. O objetivo é proporcionar uma experiência completa de xadrez, permitindo partidas entre dois jogadores locais, com destaque visual para movimentos, histórico de jogadas e suporte às principais regras do jogo.

## Principais Funcionalidades

- **Tabuleiro interativo:** Interface gráfica que exibe o tabuleiro 8x8, com peças representadas por imagens.
- **Movimentação de peças:** Permite selecionar e mover peças conforme as regras do xadrez.
- **Validação de movimentos:** Apenas movimentos legais são permitidos, incluindo captura, xeque e xeque-mate.
- **Histórico de jogadas:** Área dedicada para exibir todos os lances realizados na partida.
- **Temas de cores:** Possibilidade de alternar entre diferentes temas visuais para o tabuleiro.
- **Destaques visuais:** Casas de seleção, movimentos possíveis e último lance são destacados para facilitar a visualização.
- **Controle de turno:** Indicação de qual jogador deve jogar (brancas ou pretas).
- **Novo jogo:** Opção para reiniciar a partida a qualquer momento.

## Estrutura do Projeto

- `src/view/ChessGUI.java`: Interface gráfica principal do jogo.
- `src/controller/Game.java`: Lógica central do jogo, controle de regras e estado.
- `src/model/board/Board.java`: Representação do tabuleiro e manipulação de peças.
- `src/model/pieces/`: Classes das peças (King, Queen, Rook, Bishop, Knight, Pawn).
- `resources/`: Imagens das peças utilizadas na interface.

## Como Executar

1. Compile todos os arquivos Java:
	```powershell
	Remove-Item -Recurse -Force .\out -ErrorAction SilentlyContinue
	New-Item -ItemType Directory -Force .\out | Out-Null
	$files = Get-ChildItem -Recurse -Path .\src -Filter *.java | ForEach-Object FullName
	javac -Xlint:all -encoding UTF-8 -d out $files
	```
2. Execute o jogo:
	```powershell
	java -cp "out;resources" view.ChessGUI
	```

## Melhorias Futuras

- Implementação de IA adversária
- Suporte a partidas online
- Animações e efeitos visuais
- Internacionalização da interface
- Testes automatizados

---

Desenvolvido para fins acadêmicos. Sinta-se à vontade para contribuir ou sugerir melhorias!
