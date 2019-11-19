package fit.networks.gui;

import fit.networks.controller.SnakeSwingController;
import fit.networks.game.Cell;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GameBoard extends JPanel implements ActionListener {

    private final int WIDTH;
    private final int HEIGHT;
    private final int DOTSIZE;
    private final int ALLDOTS;

    private Cell[][] field = null;
    private Integer[] x = null;
    private Integer[] y = null;
    private SnakeSwingController controller;


    public GameBoard(int width, int height, int dotSize, SnakeSwingController controller) {
        DOTSIZE = dotSize;
        ALLDOTS = width * height;
        WIDTH = width * DOTSIZE;
        HEIGHT = height * DOTSIZE;
        field = new Cell[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++)
                field[i][j] = new Cell();
        }
        this.controller = controller;
        setBackground(Color.WHITE);
     //   setFocusable(true);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setMinimumSize(new Dimension(WIDTH, HEIGHT));

    }

    public void doDrawing(Cell[][] field) {
        this.field = field;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (field == null) return;
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++)
                if (!field[i][j].isEmpty()) {
                    g.setColor(field[i][j].getColor());
                    g.fillRect(i * DOTSIZE, j * DOTSIZE, DOTSIZE, DOTSIZE);
                }
        }
        Toolkit.getDefaultToolkit().sync();
    }


    @Override
    public void actionPerformed(ActionEvent actionEvent) {

    }



}
