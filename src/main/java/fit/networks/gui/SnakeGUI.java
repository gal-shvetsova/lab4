package fit.networks.gui;

import fit.networks.controller.GameController;
import fit.networks.controller.GameControllerImpl;
import fit.networks.game.GameConfig;
import fit.networks.game.gamefield.Field;
import fit.networks.view.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SnakeGUI extends JFrame implements View {
    private static SnakeGUI snakeGui;
    private JPanel gamePanel = new JPanel();
    private InfoPanel infoPanel = new InfoPanel();
    private boolean isStarted = false;
    private GameBoard gameBoard = null;
    private GameController controller;

    private static SnakeGUI instance;

    private SnakeGUI() {
        super("Snake");
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        initMain();
        this.addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                controller.exitGame();
            }
        } );
    }

    private void initMain() {
        setLayout(new BorderLayout());
        JPanel pane = new JPanel(new BorderLayout());
        setContentPane(pane);
        gamePanel.setBackground(Color.GREEN);
        pane.add(gamePanel, BorderLayout.CENTER);
        pane.add(infoPanel, BorderLayout.EAST);
        gamePanel.addKeyListener(new TAdapter());
    }

    private static class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            switch (key) {
                case KeyEvent.VK_LEFT: {
                    GameControllerImpl.getInstance().keyActivity(-1, 0);
                    break;
                }
                case KeyEvent.VK_RIGHT: {
                    GameControllerImpl.getInstance().keyActivity(1, 0);
                    break;
                }
                case KeyEvent.VK_DOWN: {
                    GameControllerImpl.getInstance().keyActivity(0, 1);
                    break;
                }
                case KeyEvent.VK_UP: {
                    GameControllerImpl.getInstance().keyActivity(0, -1);
                    break;
                }
            }
        }

    }

    public void loadNewField(Field field) {
        if (!isStarted) {
            return;
        }
        if (gameBoard != null)
            gameBoard.doDrawing(field);
    }

    public void loadAvailableGames(String[][] games) {
        infoPanel.updateTable(games);
    }


    public void startGame(GameConfig config) {
        Dimension dim = gamePanel.getSize();
        double dotSize = Math.floor(Math.sqrt(dim.width * dim.height / config.getWidth() / config.getHeight()));
        gameBoard = new GameBoard(config.getWidth(), config.getHeight(), (int) dotSize);
        infoPanel.getCurrentInfoPanel().setGameInfo(config);
        gamePanel.add(gameBoard);
        gamePanel.requestFocus();
        this.pack();
        this.repaint();
        isStarted = true;
    }

    public void endGame() {
        gamePanel.removeAll();
        gameBoard = null;
        infoPanel.getCurrentInfoPanel().hideGameInfo();
        isStarted = false;
        showDeadForm();
        this.pack();
        this.repaint();
    }

    public void showForm() {
        this.controller = GameControllerImpl.getInstance();
        this.setVisible(true);
    }

    public void showDeadForm() {
        JOptionPane.showMessageDialog(null, "You are dead");
    }

    @Override
    public void showErrorMessage(String errorMessage) {
        JOptionPane.showMessageDialog(null, errorMessage);
    }

    @Override
    public boolean isStarted() {
        return isStarted;
    }

    @Override
    public void loadRatingTable(String[][] ratingTable) {
        infoPanel.updateRating(ratingTable);
    }

    public static SnakeGUI getInstance() {
        if (instance == null) {
            snakeGui = new SnakeGUI();
        }
        return snakeGui;
    }
}
