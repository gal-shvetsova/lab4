package fit.networks.gui;

import fit.networks.controller.SnakeController;
import fit.networks.controller.SnakeControllerImpl;
import fit.networks.game.GameConfig;
import fit.networks.game.gamefield.Field;
import fit.networks.view.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class SnakeGUI extends JFrame implements View {
    private JPanel gamePanel = new JPanel();
    private InfoPanel infoPanel = new InfoPanel();
    private GameBoard gameBoard = null;
    private SnakeController controller;

    public SnakeGUI() {
        super("Snake");
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initMain();
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
    private class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            switch (key) {
                case KeyEvent.VK_LEFT: {
                    controller.keyActivity(-1, 0);
                    break;
                }
                case KeyEvent.VK_RIGHT: {
                    controller.keyActivity(1, 0);
                    break;
                }
                case KeyEvent.VK_DOWN: {
                    controller.keyActivity(0, 1);
                    break;
                }
                case KeyEvent.VK_UP: {
                    controller.keyActivity(0, -1);
                    break;
                }
            }
        }

    }

    public void loadNewField(Field field) {
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
    }

    public void endGame() {
        gamePanel.removeAll();
        gameBoard = null;
        infoPanel.getCurrentInfoPanel().hideGameInfo();
        this.pack();
        this.repaint();
    }

    public void showForm() {
        this.controller = SnakeControllerImpl.getController();
        this.setVisible(true);
    }

    public void showDeadForm() {
        //todo: make small message about dying
    }


}
