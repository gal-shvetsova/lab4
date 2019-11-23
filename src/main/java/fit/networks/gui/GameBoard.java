package fit.networks.gui;

import fit.networks.controller.SnakeControllerImpl;
import fit.networks.game.gamefield.Field;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameBoard extends JPanel implements ActionListener {

    private final int WIDTH;
    private final int HEIGHT;
    private final int DOTSIZE;
    private final int ALLDOTS;

    private Field field = null;


    public GameBoard(int width, int height, int dotSize) {
        DOTSIZE = dotSize;
        ALLDOTS = width * height;
        WIDTH = width * DOTSIZE;
        HEIGHT = height * DOTSIZE;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setMinimumSize(new Dimension(WIDTH, HEIGHT));

    }

    public void doDrawing(Field field) {
        this.field = field;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (field == null) return;
        for (int i = 0; i < field.getWidth(); i++) {
            for (int j = 0; j < field.getHeight(); j++)
                if (!field.isEmpty(i, j)) {
                    g.setColor(field.getColor(i, j));
                    g.fillRect(i * DOTSIZE, j * DOTSIZE, DOTSIZE, DOTSIZE);
                }
        }
        Toolkit.getDefaultToolkit().sync();
    }


    @Override
    public void actionPerformed(ActionEvent actionEvent) {

    }



}
