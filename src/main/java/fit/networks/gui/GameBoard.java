package fit.networks.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameBoard extends JPanel implements ActionListener {

    private final int WIDTH;
    private final int HEIGHT;
    private final int DOTSIZE;

    public GameBoard(int width, int height, int dotSize) {
        DOTSIZE = dotSize;
        WIDTH = width * DOTSIZE;
        HEIGHT = height * DOTSIZE;
        setBackground(Color.black);
        setFocusable(true);

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
    }


    @Override
    public void actionPerformed(ActionEvent actionEvent) {

    }
}
