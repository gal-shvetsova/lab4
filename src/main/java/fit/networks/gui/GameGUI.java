package fit.networks.gui;


import fit.networks.gui.protocol.Protocol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GameGUI extends JFrame {
    private JLabel label = new JLabel("I'm game");
    private JButton backButton = new JButton(new BackAction());
    private SnakeGUI snakeGUI;


    public GameGUI(SnakeGUI snakeGUI) {
        super("Game");
        this.snakeGUI = snakeGUI;
        this.setBounds(100,100,250,100);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container container = this.getContentPane();
        container.setLayout(new GridLayout(3,2,2,2));
        container.add(label);
        backButton.setText(Protocol.getBackButtonName());
        backButton.setName(Protocol.getBackButtonName());
        container.add(backButton);
    }


    class BackAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JButton btn = (JButton) actionEvent.getSource();
            if (btn.getName().equalsIgnoreCase(Protocol.getBackButtonName())) {
                snakeGUI.getGameGUI().setVisible(false);
                snakeGUI.setVisible(true);
            }
        }
    }
}

