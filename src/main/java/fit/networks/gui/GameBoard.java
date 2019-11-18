package fit.networks.gui;

import fit.networks.controller.SnakeSwingController;

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

    private Integer x[] = null;
    private Integer y[] = null;
    private SnakeSwingController controller;


    public GameBoard(int width, int height, int dotSize, SnakeSwingController controller) {
        DOTSIZE = dotSize;
        ALLDOTS = width * height;
        WIDTH = width * DOTSIZE;
        HEIGHT = height * DOTSIZE;
        x = new Integer[width];
        y = new Integer[height];
        for (int i = 0; i < width; i++) x[i] = 0;
        for (int i = 0; i < height; i++) y[i] = 0;
        this.controller = controller;
        setBackground(Color.black);
        setFocusable(true);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setMinimumSize(new Dimension(WIDTH, HEIGHT));

    }

    public void doDrawing(Integer[] x, Integer[] y) {
        this.x = x;
        this.y = y;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.RED);
        if (x == null || y == null) return;
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < y.length; j++)
                if (x[i] != 0 && y[j] != 0)
                    g.drawRect(i * DOTSIZE, j * DOTSIZE, DOTSIZE, DOTSIZE);
        }

        Toolkit.getDefaultToolkit().sync();

    }


    @Override
    public void actionPerformed(ActionEvent actionEvent) {

    }


    private class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

        }
    }
}
