package fit.networks.gui;

import fit.networks.gui.protocol.Protocol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SnakeGUI extends JFrame {
    private JLabel label = new JLabel("I'm working");
    private JButton newGame = new JButton(new NewGameAction());
    private Object[][] array = new String[][] {{ "Сахар" , "кг", "1.5" },
            { "Мука"  , "кг", "4.0" },
            { "Молоко", "л" , "2.2" }};
    private JTable rating = new JTable(array, Protocol.getRatingColumnsHeaders());
    private JPanel gamePanel = new JPanel();
    private SnakeGUI snakeGUI = this;
    private GameGUI gameGUI = new GameGUI(snakeGUI);


    public SnakeGUI() {
        super("Snake");
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        newGame.setName(Protocol.getNewGameButtonName());
        newGame.setText(Protocol.getNewGameButtonName());
        Container container = this.getContentPane();
        container.setLayout(new GridLayout(3,2,2,2));
        container.add(label);
        container.add(newGame);
        container.add(new JScrollPane(rating));
        container.add(gamePanel);

    }

    GameGUI getGameGUI(){
        return gameGUI;
    }

    class NewGameAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JButton btn = (JButton) actionEvent.getSource();
            System.out.println("Нажатие на кнопку <" + btn.getName() + ">");
            if (btn.getName().equalsIgnoreCase(Protocol.getNewGameButtonName())) {
                gameGUI.setVisible(true);
                snakeGUI.setVisible(false);
            }
        }
    }
}
