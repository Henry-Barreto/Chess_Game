// ========================= src/view/ChessGUI.java =========================

/**
 * ChessGUI.java
 * Interface gráfica principal do jogo de xadrez.
 *
 * Responsável por exibir o tabuleiro, gerenciar interações do usuário,
 * mostrar histórico de jogadas e status do jogo.
 *
 * Principais responsabilidades:
 * - Renderizar o tabuleiro e peças
 * - Destacar movimentos legais, seleção e último lance
 * - Integrar com a lógica do jogo (classe Game)
 * - Exibir histórico e status
 */
package view;

import controller.Game;
import ai.IANivel3;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import model.board.Position;
import model.pieces.Pawn;
import model.pieces.Piece;

// Classe principal da interface gráfica do jogo de xadrez
public class ChessGUI extends JFrame {
    private static final long serialVersionUID = 1L; // evita warning de serialização

    // --- Temas de cores ---
    public enum BoardTheme {
        VERDE_CINZA(new Color(255, 255, 255), new Color(0, 0, 0)),
        ROXO_CINZA(new Color(140, 84, 126), new Color(193, 193, 193)),
        AZUL_CINZA(new Color(84, 126, 140), new Color(193, 193, 193));

        public final Color light, dark;
        BoardTheme(Color light, Color dark) {
            this.light = light;
            this.dark = dark;
        }
    }

    private BoardTheme currentTheme = BoardTheme.VERDE_CINZA;
    private static final Color HILITE_SELECTED = new Color(255, 237, 41); // cor de seleção
    private static final Color HILITE_LEGAL = new Color(26, 20, 196);     // cor de movimentos legais
    private static final Color HILITE_LASTMOVE = new Color(220, 170, 30); // cor do último lance

    private static final Border BORDER_SELECTED = new MatteBorder(3, 3, 3, 3, HILITE_SELECTED);
    private static final Border BORDER_LEGAL = new MatteBorder(3, 3, 3, 3, HILITE_LEGAL);
    private static final Border BORDER_LASTMOVE = new MatteBorder(3, 3, 3, 3, HILITE_LASTMOVE);

    private final Game game; // lógica do jogo

    private final JPanel boardPanel; // painel do tabuleiro
    private final JButton[][] squares = new JButton[8][8]; // botões das casas

    private final JLabel status; // barra de status inferior
    private final JTextArea history; // área de histórico de jogadas
    private final JScrollPane historyScroll; // scroll do histórico

    // Menu e controles
    private JCheckBoxMenuItem pcAsBlack;
    private JMenuItem newGameItem, quitItem;
    private JMenu themeMenu;
    private JRadioButtonMenuItem verdeItem, roxoItem, azulItem;

    // Controle de seleção e movimentos legais
    private Position selected = null;
    private List<Position> legalForSelected = new ArrayList<>();

    // Realce do último lance
    private Position lastFrom = null, lastTo = null;

    // IA
    private boolean aiThinking = false;
    private final Random rnd = new Random();
    private int aiLevel = 0; // 0 = fácil, 1 = médio, 2 = difícil

    // Construtor da interface
    public ChessGUI() {
        super("RoyalChess");
        
        // Tenta aplicar o tema Nimbus
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ignored) {}

        this.game = new Game();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        // Menu superior
        setJMenuBar(buildMenuBar());

        // Painel do tabuleiro (8x8)
        boardPanel = new JPanel(new GridLayout(8, 8, 0, 0));
        boardPanel.setBackground(new Color(210, 210, 210));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        // Cria os botões das casas do tabuleiro
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                final int rr = r;
                final int cc = c;
                JButton b = new JButton();
                b.setMargin(new Insets(0, 0, 0, 0));
                b.setFocusPainted(false);
                b.setOpaque(true);
                b.setBorderPainted(true);
                b.setContentAreaFilled(true);
                b.setFont(b.getFont().deriveFont(Font.BOLD, 24f)); // fallback Unicode
                b.addActionListener(e -> handleClick(new Position(rr, cc))); // ação de clique
                squares[r][c] = b;
                boardPanel.add(b);
            }
        }

        // Barra inferior de status
        status = new JLabel("Jogada: Brancas");
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        status.setFont(new Font("Segoe UI", Font.CENTER_BASELINE, 14));
        status.setForeground(Color.WHITE);

        // Histórico de jogadas
        history = new JTextArea(14, 22);
        history.setEditable(false);
        history.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        history.setForeground(Color.WHITE);
        history.setBackground(new Color(156, 174, 102));
        historyScroll = new JScrollPane(history);

        // Painel lateral direito (histórico + controles)
        JPanel rightPanel = new JPanel(new BorderLayout(6, 6));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        rightPanel.setBackground(new Color(210, 210, 210));
        JLabel histLabel = new JLabel("Histórico de Jogadas:");
        histLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        histLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        rightPanel.add(histLabel, BorderLayout.NORTH);
        rightPanel.add(historyScroll, BorderLayout.CENTER);
        rightPanel.add(buildSideControls(), BorderLayout.SOUTH);

        add(boardPanel, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        // Seletor de tema agora está no menu

        // Atualiza ícones ao redimensionar o painel do tabuleiro
        boardPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refresh(); // recarrega ícones ajustando o tamanho
            }
        });
        
        getContentPane().setBackground(Color.DARK_GRAY); // fundo da janela
        setMinimumSize(new Dimension(1100, 780));
        setLocationRelativeTo(null);

        // Atalhos de teclado: Ctrl+N (novo jogo), Ctrl+Q (sair)
        setupAccelerators();

        setVisible(true);
        refresh();
        maybeTriggerAI(); // inicia IA se for vez do PC
    }

    // ----------------- Menus e controles -----------------

    // Cria a barra de menu superior
    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();

        JMenu gameMenu = new JMenu("Menu");

        newGameItem = new JMenuItem("Novo Jogo");
        newGameItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        newGameItem.addActionListener(e -> doNewGame());

        pcAsBlack = new JCheckBoxMenuItem("PC joga com as Pretas");
        pcAsBlack.setSelected(false);

        quitItem = new JMenuItem("Sair");
        quitItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        quitItem.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        // Submenu de tema
        themeMenu = new JMenu("Tema");
        ButtonGroup themeGroup = new ButtonGroup();
        verdeItem = new JRadioButtonMenuItem("Verde/Cinza", true);
        roxoItem = new JRadioButtonMenuItem("Roxo/Cinza");
        azulItem = new JRadioButtonMenuItem("Azul/Cinza");
        themeGroup.add(verdeItem);
        themeGroup.add(roxoItem);
        themeGroup.add(azulItem);
        themeMenu.add(verdeItem);
        themeMenu.add(roxoItem);
        themeMenu.add(azulItem);

        verdeItem.addActionListener(e -> {
            currentTheme = BoardTheme.VERDE_CINZA;
            refresh();
        });
        roxoItem.addActionListener(e -> {
            currentTheme = BoardTheme.ROXO_CINZA;
            refresh();
        });
        azulItem.addActionListener(e -> {
            currentTheme = BoardTheme.AZUL_CINZA;
            refresh();
        });

        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(pcAsBlack);
        gameMenu.addSeparator();
        gameMenu.add(themeMenu);
        gameMenu.addSeparator();
        gameMenu.add(quitItem);

        mb.add(gameMenu);
        return mb;
    }

    // Cria os controles laterais (novo jogo, IA, nível)
    private JPanel buildSideControls() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton btnNew = new JButton("Novo Jogo");
        btnNew.addActionListener(e -> doNewGame());
        btnNew.setBackground(new Color(128,128,128)); // verde
        btnNew.setForeground(Color.WHITE); // texto branco
        btnNew.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnNew.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnNew.setBackground(new Color(93, 101, 50)); // verde escuro
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnNew.setBackground(new Color(128,128,128)); // verde original
            }
        });
        panel.add(btnNew);

        JCheckBox cb = new JCheckBox("PC (Pretas)");
        cb.setSelected(pcAsBlack.isSelected());
        cb.addActionListener(e -> pcAsBlack.setSelected(cb.isSelected()));
        cb.setFocusPainted(false);
        cb.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cb.setForeground(new Color(126, 140, 84));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                cb.setForeground(new Color(44, 62, 80));
            }
        });
        panel.add(cb);

        panel.add(new JLabel("IA:"));
        JComboBox<String> aiLevelBox = new JComboBox<>(new String[]{"Fácil", "Médio", "Difícil"});
        aiLevelBox.setSelectedIndex(aiLevel);
        aiLevelBox.addActionListener(e -> aiLevel = aiLevelBox.getSelectedIndex());
        panel.add(aiLevelBox);

        return panel;
    }

    // Configura atalhos de teclado para novo jogo e sair
    private void setupAccelerators() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                        "newGame");
        getRootPane().getActionMap().put("newGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doNewGame();
            }
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                        "quit");
        getRootPane().getActionMap().put("quit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispatchEvent(new WindowEvent(ChessGUI.this, WindowEvent.WINDOW_CLOSING));
            }
        });
    }

    // Inicia um novo jogo
    private void doNewGame() {
        selected = null;
        legalForSelected.clear();
        lastFrom = lastTo = null;
        aiThinking = false;
        game.newGame();
        refresh();
        maybeTriggerAI();
    }

    // ----------------- Interação de tabuleiro -----------------

    // Lida com cliques nas casas do tabuleiro
    private void handleClick(Position clicked) {
        if (game.isGameOver() || aiThinking)
            return;

        // Se for vez do PC, ignora cliques do usuário
        if (pcAsBlack.isSelected() && !game.whiteToMove())
            return;

        Piece p = game.board().get(clicked);

        if (selected == null) {
            // Seleciona peça da vez
            if (p != null && p.isWhite() == game.whiteToMove()) {
                selected = clicked;
                legalForSelected = game.legalMovesFrom(selected);
            }
        } else {
            // Já havia uma seleção
            List<Position> legals = game.legalMovesFrom(selected); // recalcula por segurança
            if (legals.contains(clicked)) {
                Character promo = null;
                Piece moving = game.board().get(selected);
                if (moving instanceof Pawn && game.isPromotion(selected, clicked)) {
                    promo = askPromotion(); // pergunta peça para promoção
                }
                lastFrom = selected;
                lastTo = clicked;

                game.move(selected, clicked, promo);

                selected = null;
                legalForSelected.clear();

                refresh();
                maybeAnnounceEnd();
                maybeTriggerAI();
                return;
            } else if (p != null && p.isWhite() == game.whiteToMove()) {
                // Troca seleção para outra peça da vez
                selected = clicked;
                legalForSelected = game.legalMovesFrom(selected);
            } else {
                // Clique inválido: limpa seleção
                selected = null;
                legalForSelected.clear();
            }
        }
        refresh();
    }

    // Pergunta ao usuário qual peça promover o peão
    private Character askPromotion() {
        String[] opts = { "Rainha", "Torre", "Bispo", "Cavalo" };
        int ch = JOptionPane.showOptionDialog(
                this,
                "Escolha a peça para promoção:",
                "Promoção",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opts,
                opts[0]);
        return switch (ch) {
            case 1 -> 'R';
            case 2 -> 'B';
            case 3 -> 'N';
            default -> 'Q';
        };
    }

    // ----------------- IA (não bloqueante) -----------------

    // Aciona a jogada da IA se for vez do PC
    private void maybeTriggerAI() {
        if (game.isGameOver())
            return;
        if (!pcAsBlack.isSelected())
            return;
        if (game.whiteToMove())
            return; // PC joga de pretas

        aiThinking = true;
        status.setText("Vez: Pretas — PC pensando...");

        // Executa IA em thread separada (SwingWorker)
        new SwingWorker<Void, Void>() {
            Position aiFrom, aiTo;

            @Override
            protected Void doInBackground() {
                Move chosen = null;
                if (aiLevel == 0) {
                    // Fácil: aleatório
                    var allMoves = collectAllLegalMovesForSide(false);
                    if (allMoves.isEmpty())
                        return null;
                    chosen = allMoves.get(rnd.nextInt(allMoves.size()));
                } else if (aiLevel == 1) {
                    // Médio: prioriza capturas e centro
                    var allMoves = collectAllLegalMovesForSide(false);
                    if (allMoves.isEmpty())
                        return null;
                    int bestScore = Integer.MIN_VALUE;
                    List<Move> bestList = new ArrayList<>();
                    for (Move mv : allMoves) {
                        int score = 0;
                        Piece target = game.board().get(mv.to);
                        if (target != null) score += pieceValue(target);
                        score += centerBonus(mv.to);
                        if (score > bestScore) {
                            bestScore = score;
                            bestList.clear();
                            bestList.add(mv);
                        } else if (score == bestScore) {
                            bestList.add(mv);
                        }
                    }
                    chosen = bestList.get(rnd.nextInt(bestList.size()));
                } else if (aiLevel == 2) {
                    // Difícil: usa IANivel3
                    IANivel3 iaNivel3 = new IANivel3();
                    model.board.Move move = iaNivel3.makeMove(game);
                    if (move != null) {
                        aiFrom = move.getFrom();
                        aiTo = move.getTo();
                        return null;
                    }
                }
                if (chosen != null) {
                    aiFrom = chosen.from;
                    aiTo = chosen.to;
                }
                return null;
            }

            @Override
            protected void done() {
                try { get(); } catch (Exception ignored) {}
                if (aiFrom != null && aiTo != null && !game.isGameOver() && !game.whiteToMove()) {
                    lastFrom = aiFrom;
                    lastTo = aiTo;
                    Character promo = null;
                    Piece moving = game.board().get(aiFrom);
                    if (moving instanceof Pawn && game.isPromotion(aiFrom, aiTo)) {
                        promo = 'Q';
                    }
                    game.move(aiFrom, aiTo, promo);
                }
                aiThinking = false;
                refresh();
                maybeAnnounceEnd();
            }
        }.execute();
    }

    // Classe interna para representar um movimento simples (usada pela IA)
    private static class Move {
        final Position from, to;

        Move(Position f, Position t) {
            this.from = f;
            this.to = t;
        }
    }

    // Coleta todos os movimentos legais para o lado especificado
    private List<Move> collectAllLegalMovesForSide(boolean whiteSide) {
        List<Move> moves = new ArrayList<>();
        if (whiteSide != game.whiteToMove())
            return moves;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position from = new Position(r, c);
                Piece piece = game.board().get(from);
                if (piece != null && piece.isWhite() == whiteSide) {
                    for (Position to : game.legalMovesFrom(from)) {
                        moves.add(new Move(from, to));
                    }
                }
            }
        }
        return moves;
    }

    // Valor das peças para IA
    private int pieceValue(Piece p) {
        if (p == null)
            return 0;
        switch (p.getSymbol()) {
            case "P":
                return 100;
            case "N":
            case "B":
                return 300;
            case "R":
                return 500;
            case "Q":
                return 900;
            case "K":
                return 20000;
        }
        return 0;
    }

    // Bônus para casas centrais (IA)
    private int centerBonus(Position pos) {
        int r = pos.getRow(), c = pos.getColumn();
        if ((r == 3 || r == 4) && (c == 3 || c == 4))
            return 10;
        if ((r >= 2 && r <= 5) && (c >= 2 && c <= 5))
            return 4;
        return 0;
    }

    // ----------------- Atualização de UI -----------------

    // Atualiza toda a interface (tabuleiro, ícones, status, histórico)
    private void refresh() {
        // 1) Cores base e limpa bordas
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                boolean light = (r + c) % 2 == 0;
                Color base = light ? currentTheme.light : currentTheme.dark;
                JButton b = squares[r][c];
                b.setBackground(base);
                b.setBorder(null);
                b.setToolTipText(null);
            }
        }

        // 2) Realce do último lance
        if (lastFrom != null)
            squares[lastFrom.getRow()][lastFrom.getColumn()].setBorder(BORDER_LASTMOVE);
        if (lastTo != null)
            squares[lastTo.getRow()][lastTo.getColumn()].setBorder(BORDER_LASTMOVE);

        // 3) Realce da seleção e movimentos legais
        if (selected != null) {
            squares[selected.getRow()][selected.getColumn()].setBorder(BORDER_SELECTED);
            for (Position d : legalForSelected) {
                squares[d.getRow()][d.getColumn()].setBorder(BORDER_LEGAL);
            }
        }

        // 4) Ícones das peças (ou Unicode como fallback)
        int iconSize = computeSquareIconSize();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = game.board().get(new Position(r, c));
                JButton b = squares[r][c];

                if (p == null) {
                    b.setIcon(null);
                    b.setText("");
                    continue;
                }

                char sym = p.getSymbol().charAt(0);
                ImageIcon icon = ImageUtil.getPieceIcon(p.isWhite(), sym, iconSize);
                if (icon != null) {
                    b.setIcon(icon);
                    b.setText("");
                } else {
                    b.setIcon(null);
                    b.setText(toUnicode(p.getSymbol(), p.isWhite()));
                }
            }
        }

        // 5) Atualiza status e histórico
        String side = game.whiteToMove() ? "Brancas" : "Pretas";
        String chk = game.inCheck(game.whiteToMove()) ? " — Xeque!" : "";
        if (aiThinking)
            chk = " — PC pensando...";
        status.setText("Jogada: " + side + chk);

        // Atualiza cor do histórico conforme tema
        history.setBackground(currentTheme.light);

        StringBuilder sb = new StringBuilder();
        var hist = game.history();
        for (int i = 0; i < hist.size(); i++) {
            if (i % 2 == 0)
                sb.append((i / 2) + 1).append('.').append(' ');
            sb.append(hist.get(i)).append(' ');
            if (i % 2 == 1)
                sb.append('\n');
        }
        history.setText(sb.toString());
        history.setCaretPosition(history.getDocument().getLength());
    }

    // Exibe mensagem de fim de jogo
    private void maybeAnnounceEnd() {
        if (!game.isGameOver())
            return;
        String msg;
        if (game.inCheck(game.whiteToMove())) {
            msg = "Xeque-mate! " + (game.whiteToMove() ? "Brancas" : "Pretas") + " estão em mate.";
        } else {
            msg = "Empate por afogamento (stalemate).";
        }
        JOptionPane.showMessageDialog(this, msg, "Fim de Jogo", JOptionPane.INFORMATION_MESSAGE);
    }

    // Retorna o Unicode da peça (fallback)
    private String toUnicode(String sym, boolean white) {
        return switch (sym) {
            case "K" -> white ? "\u2654" : "\u265A";
            case "Q" -> white ? "\u2655" : "\u265B";
            case "R" -> white ? "\u2656" : "\u265C";
            case "B" -> white ? "\u2657" : "\u265D";
            case "N" -> white ? "\u2658" : "\u265E";
            case "P" -> white ? "\u2659" : "\u265F";
            default -> "";
        };
    }

    // Calcula o tamanho ideal do ícone da peça
    private int computeSquareIconSize() {
        JButton b = squares[0][0];
        int w = Math.max(1, b.getWidth());
        int h = Math.max(1, b.getHeight());
        int side = Math.min(w, h);
        if (side <= 1)
            return 64;
        return Math.max(24, side - 8);
    }

    // Ponto de entrada do programa
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGUI::new);
    }
}
