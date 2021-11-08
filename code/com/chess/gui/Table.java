package com.chess.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.chess.engine.classic.board.Board;
import com.chess.engine.classic.board.Board.MoveStatus;
import com.chess.engine.classic.board.Move;
import com.chess.engine.classic.board.Move.MoveFactory;
import com.chess.engine.classic.board.StandardBoardConfigurator;
import com.chess.engine.classic.board.Tile;
import com.chess.engine.classic.pieces.Piece;
import com.chess.engine.classic.player.Player;
import com.chess.engine.classic.player.ai.MiniMax;
import com.chess.engine.classic.player.ai.SimpleBoardEvaluator;


public final class Table extends Observable{

    private static JFrame gameFrame;
    private static Board chessBoard;
    private static GameHistoryPanel gameHistoryPanel;
    private static TakenPiecesPanel takenPiecesPanel;
    private static BoardPanel boardPanel;
    private static ChatPanel chatPanel;
    private static ArrayList<Move> moveLog;
    private final GameSetup gameSetup;
    private static final Dimension OUTER_FRAME_DIMENSION = new Dimension(600, 600);
    private static final Table instance=new Table();
    private Table() {
        gameFrame = new JFrame("Shatranj2");
        final JMenuBar tableMenuBar = new JMenuBar();
        populateMenuBar(tableMenuBar);
        gameFrame.setJMenuBar(tableMenuBar);
        gameFrame.setLayout(new BorderLayout());
        chessBoard = new Board(new StandardBoardConfigurator());
        gameHistoryPanel = new GameHistoryPanel();
        chatPanel = new ChatPanel();
        takenPiecesPanel = new TakenPiecesPanel();
        boardPanel = new BoardPanel(chessBoard);
        moveLog = new ArrayList<>();
        this.addObserver(new TableGameAIWatcher());
        gameFrame.add(takenPiecesPanel, BorderLayout.WEST);
        gameFrame.add(boardPanel, BorderLayout.CENTER);
        gameFrame.add(gameHistoryPanel, BorderLayout.EAST);
        gameFrame.add(chatPanel, BorderLayout.SOUTH);
        // Make sure we have nice window decorations.
        gameFrame.setDefaultLookAndFeelDecorated(true);
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setSize(OUTER_FRAME_DIMENSION);
        gameFrame.setVisible(true);
        gameSetup=new GameSetup(this.gameFrame, true);
    }
    enum PlayerType {
        HUMAN,
        COMPUTER
    }
   public static Table get()
   {
       return instance;
   }
  private GameSetup getGameSetup()
  {
      return gameSetup;
  }
 
  private void setUpdate(final GameSetup gameSetup) {
    setChanged();
    notifyObservers(gameSetup);
}

    private static void populateMenuBar(final JMenuBar tableMenuBar) {
        final JMenu fileMenu = createFileMenu();
        final JMenu optionsMenu = createOptionsMenu();
        tableMenuBar.add(fileMenu);
        tableMenuBar.add(optionsMenu);
    }

    private static JMenu createFileMenu() {
        final JMenu file_menu = new JMenu("File");
        file_menu.setMnemonic(KeyEvent.VK_F);
        final JMenuItem newFileItem = new JMenuItem("New Game", KeyEvent.VK_N);
        newFileItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                undoAllMoves();
            }
            }
        );
        file_menu.add(newFileItem);
        
        final JMenuItem evaluateBoardMenuItem = new JMenuItem("Evaluate Board", KeyEvent.VK_E);
        evaluateBoardMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                System.out.println(new SimpleBoardEvaluator().evaluate(chessBoard));
            }
        });
        file_menu.add(evaluateBoardMenuItem);


        final JMenuItem legalMovesMenuItem = new JMenuItem("Current State", KeyEvent.VK_L);
        legalMovesMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                System.out.println(chessBoard.getWhitePieces());
                System.out.println(chessBoard.getBlackPieces());
                System.out.println(chessBoard.currentPlayer().playerInfo());
                System.out.println(chessBoard.currentPlayer().getOpponent().playerInfo());
            }
        });
        file_menu.add(legalMovesMenuItem);

        final JMenuItem makeMoveMenuItem = new JMenuItem("Make a smart move", KeyEvent.VK_M);
        makeMoveMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                final Thread t = new Thread("Think Tank") {
                    @Override
                    public void run() {
                        chessBoard.currentPlayer().setMoveStrategy(new MiniMax(4));
                        final Move bestMove = chessBoard.currentPlayer().getMoveStrategy().execute(chessBoard);
                        Player.makeMove(chessBoard, bestMove);
                        moveLog.add(bestMove);
                        gameHistoryPanel.redo(moveLog);
                        takenPiecesPanel.redo(moveLog);
                        boardPanel.drawBoard();
                    }
                };
                t.start();
            }
        });
        file_menu.add(makeMoveMenuItem);

        final JMenuItem undoMoveMenuItem = new JMenuItem("Undo last move", KeyEvent.VK_M);
        undoMoveMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                if(moveLog.size() > 0) {
                    undoLastMove();
                }
            }
        });
        file_menu.add(undoMoveMenuItem);

        final JMenuItem exitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                gameFrame.dispose();
                System.exit(0);
            }
        });
        file_menu.add(exitMenuItem);
        return file_menu;


    }

    private static JMenu createOptionsMenu() {

        final JMenu options_menu = new JMenu("Options");
        options_menu.setMnemonic(KeyEvent.VK_O);
       
       

        final JMenuItem setupGameMenuItem = new JMenuItem("Setup Game", KeyEvent.VK_S);
        setupGameMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                Table.get().getGameSetup().promptUser();
                Table.get().setUpdate(Table.get().getGameSetup());
                
            }
        });
        options_menu.add(setupGameMenuItem);


        return options_menu;
    }
    private static class TableGameAIWatcher
            implements Observer {

        @Override
        public void update(final Observable o,
                           final Object arg) {

            if (Table.get().getGameSetup().isAIPlayer(Table.get().getGameBoard().currentPlayer()) &&
                !Table.get().getGameBoard().currentPlayer().isInCheckMate() &&
                !Table.get().getGameBoard().currentPlayer().isInStaleMate()) {
                System.out.println(Table.get().getGameBoard().currentPlayer() + " is set to AI, thinking....");
                final AIThinkTank thinkTank = new AIThinkTank();
                thinkTank.execute();
            }

            if (Table.get().getGameBoard().currentPlayer().isInCheckMate()) {

                System.out.println("Game Over"+Table.get().getGameBoard().currentPlayer()+" is check mate!");
                BufferedImage image;
                String s=""+Table.get().getGameBoard().currentPlayer();

                
                try {
                    image = ImageIO.read(new File("art/simple/"+s.charAt(0)+"K.gif"));
                    JOptionPane.showMessageDialog(boardPanel, "Game Over: Player " + Table.get().getGameBoard().currentPlayer() + " is check mate!",
                            "Notification", JOptionPane.WARNING_MESSAGE,new ImageIcon(image));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                    
                }
                 

            if (Table.get().getGameBoard().currentPlayer().isInStaleMate()) {
                System.out.println("Game Over"+Table.get().getGameBoard().currentPlayer()+" is in stale mate!");
                BufferedImage image;
                String s=""+Table.get().getGameBoard().currentPlayer();
                
                try {
                    image = ImageIO.read(new File("art/simple/"+s.charAt(0)+"K.gif"));
                    JOptionPane.showMessageDialog(boardPanel, "Game Over: Player " + Table.get().getGameBoard().currentPlayer() + " is in stale mate!",
                            "Notification", JOptionPane.WARNING_MESSAGE,new ImageIcon(image));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }
    private static class AIThinkTank extends SwingWorker<Move, String> {

        private AIThinkTank() {
        }

        @Override
        protected Move doInBackground() {
                final MiniMax strategy = new MiniMax(Table.get().getGameSetup().getSearchDepth());
                final Move bestMove=strategy.execute(Table.get().getGameBoard());
                return bestMove ;  
        }

        @Override
        public void done() {
            try {
                final Move bestMove = get();
                Table.get().updateComputerMove(bestMove);
                Table.get().getGameBoard().currentPlayer().makeMove(chessBoard, bestMove);
                Table.get().getmoveLog().add(bestMove);
                Table.get().getHistoryPanel().redo(Table.get().getmoveLog());
                Table.get().getTakenPiecesPanel().redo(Table.get().getmoveLog());
                Table.get().getBoardPanel().drawBoard();
                Table.get().moveMadeUpdate(PlayerType.COMPUTER);


            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void show() {
        Table.get().getmoveLog().clear();
        Table.get().getHistoryPanel().redo(Table.get().getmoveLog());
        Table.get().getTakenPiecesPanel().redo(Table.get().getmoveLog());
        Table.get().getBoardPanel().drawBoard();
    }
    private void updateComputerMove(Move move){
    }
    private ArrayList<Move> getmoveLog(){
        return moveLog;
    }
    private BoardPanel getBoardPanel()
    {
        return boardPanel;
    }
    private GameHistoryPanel getHistoryPanel()
    {
        return gameHistoryPanel;
    }
    private TakenPiecesPanel getTakenPiecesPanel()
    {
        return takenPiecesPanel;
    }
   private void moveMadeUpdate(PlayerType playerType)
   {
      setChanged();
      notifyObservers(playerType);
   }
    
    public static void requestMove(final Board board,
                                   final Move move,
                                   final boolean showPopUp) {

        final MoveStatus status = Player.makeMove(board, move);
        if(status == MoveStatus.DONE) {
            moveLog.add(move);
            gameHistoryPanel.redo(moveLog);
            takenPiecesPanel.redo(moveLog);
        } else {
            try {
                final BufferedImage image = ImageIO.read(new File("art/misc/illegal.png"));
                if (!(status == MoveStatus.DONE) && showPopUp) {
                    JOptionPane.showMessageDialog(boardPanel, "Move " +move.getMovedPiece()+ ": " +move+ " is illegal",
                            "Notification", JOptionPane.WARNING_MESSAGE,new ImageIcon(image));
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        boardPanel.drawBoard();
    }

    public Board getGameBoard() {
        return chessBoard;
    }
    private static void undoAllMoves() {

        for(int i = moveLog.size() - 1; i >= 0; i--) {
            final Move lastMove = moveLog.remove(moveLog.size() - 1);
            Player.unMakeMove(chessBoard, lastMove);
        }
        moveLog.clear();
        gameHistoryPanel.redo(moveLog);
        takenPiecesPanel.redo(moveLog);
        boardPanel.drawBoard();
    }

    private static void undoLastMove() {
        final Move lastMove = moveLog.remove(moveLog.size() - 1);
        Player.unMakeMove(chessBoard, lastMove);
        moveLog.remove(lastMove);
        gameHistoryPanel.redo(moveLog);
        takenPiecesPanel.redo(moveLog);
        boardPanel.drawBoard();
    }
    
 class TilePanel extends JPanel {

    private final Board chessBoard;
    private final int tileId;
    private static Tile sourceTile;
    private static Tile destinationTile;
    private static Piece movedPiece;
    private static Color lightTileColor = Color.decode("#FFFACD");
    private static Color darkTileColor = Color.decode("#593E1A");
    private static final Dimension TILE_PANEL_DIMENSION = new Dimension(10, 10);


    TilePanel(final BoardPanel boardPanel,
              final Board chessBoard,
              final int tileId) {
        this.chessBoard = chessBoard;
        this.tileId = tileId;
        setPreferredSize(TILE_PANEL_DIMENSION);
        assignTileColor();
        assignTilePieceIcon();
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(final MouseEvent event) {
                if(sourceTile == null) {
                    sourceTile = chessBoard.getTile(tileId);
                    if(sourceTile.getPiece().getPieceAlliance()!=Table.get().getGameBoard().currentPlayer().getAlliance())
                        sourceTile=null;
                    else
                    movedPiece = sourceTile.getPiece();
                } else {
                    destinationTile = chessBoard.getTile(tileId);
                    Table.requestMove(chessBoard, MoveFactory.createMove(chessBoard,
                            sourceTile.getTileCoordinate(), destinationTile.getTileCoordinate()), true);
                    sourceTile = null;
                    destinationTile = null;
                    movedPiece = null;
                }
                SwingUtilities.invokeLater(() -> {
                    gameHistoryPanel.redo(moveLog);
                    takenPiecesPanel.redo(moveLog);
                    if (gameSetup.isAIPlayer(chessBoard.currentPlayer())) {
                        Table.get().moveMadeUpdate(PlayerType.HUMAN);
                    }
                    boardPanel.drawBoard();
                });
                boardPanel.drawBoard();
            }

            @Override
            public void mouseExited(final MouseEvent e) {
            }

            @Override
            public void mouseEntered(final MouseEvent e) {
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
            }

            @Override
            public void mousePressed(final MouseEvent e) {
            }

           
        });
        setBorder(BorderFactory.createRaisedBevelBorder());
        validate();
    }

    public void drawTile() {
        removeAll();
        setBorder(BorderFactory.createRaisedSoftBevelBorder());
        assignTileColor();
        assignTilePieceIcon();
        highlightMovedPiece();
        highlightLegals();
        validate();
        repaint();
    }

    public static void setLightTileColor(final Color color) {
        lightTileColor = color;
    }

    public static void setDarkTileColor(final Color color) {
        darkTileColor = color;
    }

    private void highlightMovedPiece() {
        if(movedPiece != null && movedPiece.getPieceAlliance() == chessBoard.currentPlayer().getAlliance() &&
                movedPiece.getPiecePosition() == this.tileId) {
            setBorder(BorderFactory.createLineBorder(Color.cyan));
        }
    }

    private void highlightLegals() {
        for (final Move move : pieceLegalMoves()) {
            if (move.getDestinationCoordinate() == this.tileId) {
                try {
                    add(new JLabel(new ImageIcon(ImageIO.read(new File("art/misc/green_dot.png")))));
                }
                catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private List<Move> pieceLegalMoves() {
        if(movedPiece != null && movedPiece.getPieceAlliance() == chessBoard.currentPlayer().getAlliance()) {
            return movedPiece.calculateLegalMoves(this.chessBoard);
        }
        return Collections.emptyList();
    }

    private void assignTilePieceIcon() {
        this.removeAll();
        if(chessBoard.getTile(this.tileId).isTileOccupied()) {
            final Piece p = chessBoard.getTile(this.tileId).getPiece();
            try{
                final BufferedImage image = ImageIO.read(new File(
                        "art/simple/" + p.getPieceAlliance().toString().substring(0, 1) + "" + p.toString() +
                                ".gif"));
                JLabel imageLabel = new JLabel(new ImageIcon(image));
                add(imageLabel);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void assignTileColor() {
        if (this.tileId >= 0 && this.tileId < 8 ||
                this.tileId >= 16 && this.tileId < 24 ||
                this.tileId >= 32 && this.tileId < 40 ||
                this.tileId >= 48 && this.tileId < 56) {
            if (this.tileId % 2 == 0) {
                setBackground(lightTileColor);
            } else {
                setBackground(darkTileColor);
            }
        } else if(this.tileId >= 8 && this.tileId < 16 ||
                this.tileId >= 24 && this.tileId < 32 ||
                this.tileId >= 40 && this.tileId < 48 ||
                this.tileId >= 56 && this.tileId < 64) {
            if (!(this.tileId % 2 == 0)) {
                setBackground(lightTileColor);
            } else {
                setBackground(darkTileColor);
            }
        }
    }

}
class BoardPanel extends JPanel {

    final TilePanel[] boardTiles;
    final Board chessBoard;
    private static final Dimension BOARD_PANEL_DIMENSION = new Dimension(400, 350);

    BoardPanel(final Board chessBoard) {
        super(new GridLayout(8,8));
        this.chessBoard = chessBoard;
        this.boardTiles = new TilePanel[64];
        for (int i = 0; i < boardTiles.length; i++) {
            this.boardTiles[i] = new TilePanel(this, chessBoard, i);
            add(this.boardTiles[i]);
        }
        setPreferredSize(BOARD_PANEL_DIMENSION);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.decode("#8B4726"));
    }

    public void drawBoard() {
        for(final TilePanel t : this.boardTiles) {
            t.drawTile();
        }
        validate();
        repaint();
    }

    public void setTileDarkColor(final Color darkColor) {
        drawBoard();
    }

    public void setTileLightColor(final Color lightColor) {
        drawBoard();
    }
}
}

