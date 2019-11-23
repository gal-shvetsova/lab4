package fit.networks.gui;

import fit.networks.controller.SnakeControllerImpl;
import fit.networks.game.GameConfig;
import fit.networks.gui.protocol.Protocol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

class NewGameForm extends JFrame {
    private JTextField width = new JTextField(10);
    private JTextField height = new JTextField(10);
    private JTextField foodStatic = new JTextField(10);
    private JTextField foodPerPlayer = new JTextField(10);
    private JTextField delayMs = new JTextField(10);
    private JTextField deadFoodProb = new JTextField(10);
    private JButton okButton = new JButton(new NewGameAction());
    private JButton cancelButton = new JButton(new NewGameAction());
    private GameConfig gameConfig;

    //TODO: add validation
    public NewGameForm() {
        super("New game");
        this.gameConfig = null;
        setDefaultValues();
        setSize(512, 512);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
        setLayout(new BorderLayout());
        JPanel pane = new JPanel(new GridBagLayout());
        setContentPane(pane);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = GridBagConstraints.RELATIVE;
        c.gridx = 0;
        add(new JLabel(Protocol.getWidthLabelName()), c);
        add(width, c);
        add(new JLabel(Protocol.getHeightLabelName()), c);
        add(height, c);
        add(new JLabel(Protocol.getFoodStaticName()), c);
        add(foodStatic, c);
        add(new JLabel(Protocol.getFoodPerPlayerName()), c);
        add(foodPerPlayer, c);
        add(new JLabel(Protocol.getDelayMsName()), c);
        add(delayMs, c);
        add(new JLabel(Protocol.getDeadFoodProbName()), c);
        add(deadFoodProb, c);
        okButton.setText(Protocol.getOkButtonName());
        okButton.setName(Protocol.getOkButtonName());
        cancelButton.setText(Protocol.getCancelButtonName());
        cancelButton.setName(Protocol.getCancelButtonName());

        add(okButton, c);
        add(cancelButton, c);
    }

    private void setDefaultValues(){
        width.setText("40");
        height.setText("30");
        foodStatic.setText("10");
        foodPerPlayer.setText("1");
        delayMs.setText("1000");
        deadFoodProb.setText("0.1");
    }

    class NewGameAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        private void performDataAndStartGame() {
            int parsedWidth, parsedHeight, parsedFoodStatic, parsedDelay;
            float parsedFoodPerPlayer, parsedDeadFoodProb;
            try {
                parsedWidth = Integer.parseInt(width.getText());
                parsedHeight = Integer.parseInt(height.getText());
                parsedFoodStatic = Integer.parseInt(foodStatic.getText());
                parsedDelay = Integer.parseInt(delayMs.getText());
                parsedFoodPerPlayer = Float.parseFloat(foodPerPlayer.getText());
                parsedDeadFoodProb = Float.parseFloat(deadFoodProb.getText());

                gameConfig = new GameConfig(parsedWidth, parsedHeight, parsedFoodStatic, parsedFoodPerPlayer, parsedDelay,
                        parsedDeadFoodProb);
                SnakeControllerImpl.getController().startNewGame(gameConfig);
            } catch (NumberFormatException ex) {
                System.out.println("err");
            } catch (IllegalArgumentException argEx) {
                System.out.println("arg err");
                argEx.printStackTrace();
            }
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JButton btn = (JButton) actionEvent.getSource();
            if (btn == okButton) {
                performDataAndStartGame();
            }
            setVisible(false);
        }
    }
}

