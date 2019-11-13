package fit.networks.gui;

import fit.networks.controller.SnakeSwingController;
import fit.networks.gui.protocol.Protocol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SnakeGUI extends JFrame {

    private Object[][] rating = new String[][]{{"0", "1", "2"}};
    private Object[][] allGames = new String[][]{{"", "", "", "", "", ""}};
    private final JLabel leadingLabel = new JLabel(Protocol.getLeadingLabelName());
    private final JLabel sizeLabel = new JLabel(Protocol.getSizeLabelName());
    private final JLabel foodLabel = new JLabel(Protocol.getFoodLabelName());
    private JLabel leadingValueLabel = new JLabel(Protocol.getLeadingLabelName());
    private JLabel sizeValueLabel = new JLabel(Protocol.getSizeLabelName());
    private JLabel foodValueLabel = new JLabel(Protocol.getFoodLabelName());
    private JButton newGameButton = new JButton(new MainFormAction());
    private JTable ratingTable = new JTable(rating, Protocol.getRatingColumnsHeaders());
    private JTable allGamesTable = new JTable(allGames, Protocol.getAllGamesColumnsHeaders());
    private JPanel gamePanel = new JPanel();
    private JPanel infoPanel = new JPanel();
    private JPanel currentGameInfoPanel = new JPanel();
    private GameBoard gameBoard;
    private SnakeSwingController controller;

    private class NewGameForm extends JFrame {
        private JTextField width = new JTextField(10);
        private JTextField height = new JTextField(10);
        private JTextField foodStatic = new JTextField(10);
        private JTextField foodPerPlayer = new JTextField(10);
        private JTextField delayMs = new JTextField(10);
        private JTextField deadFoodProb = new JTextField(10);
        private JButton okButton = new JButton(new NewGameAction());
        private JButton cancelButton = new JButton(new NewGameAction());
        //TODO: add validation
        public NewGameForm(){
            super("New game");
            setSize(512, 512);
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
            setLayout(new BorderLayout());
            JPanel pane = new JPanel(new GridBagLayout());
            setContentPane(pane);
            GridBagConstraints c = new GridBagConstraints();
            c.gridy = GridBagConstraints.RELATIVE;
            c.gridx = 0;
            width.setText("40");
            height.setText("30");
            foodStatic.setText("10");
            foodPerPlayer.setText("1");
            delayMs.setText("1000");
            deadFoodProb.setText("0.1");
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
            add(cancelButton,c);
        }



        class NewGameAction extends AbstractAction {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JButton btn = (JButton) actionEvent.getSource();
                System.out.println("Нажатие на кнопку <" + btn.getName() + ">");
                if (btn == cancelButton){
                    setVisible(false);
                    return;
                }
                if (btn == okButton){
                    try {
                        controller.startNewGame(width.getText(), height.getText(), foodStatic.getText(),
                                foodPerPlayer.getText(), delayMs.getText(), deadFoodProb.getText());
                    } catch (Exception ex){
                        ex.printStackTrace();
                        return;
                    }
                    setVisible(false);

                }
            }
        }
    }

    private void initCurrentInfoPanel() {
        currentGameInfoPanel.setBackground(Color.LIGHT_GRAY);
        currentGameInfoPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.gridx = 0;
        currentGameInfoPanel.add(leadingLabel, c);
        c.gridy = 0;
        c.gridx = 2;
        currentGameInfoPanel.add(leadingValueLabel, c);
        c.gridy = 1;
        c.gridx = 0;
        currentGameInfoPanel.add(foodLabel, c);
        c.gridy = 1;
        c.gridx = 2;
        currentGameInfoPanel.add(foodValueLabel, c);
        c.gridy = 2;
        c.gridx = 0;
        currentGameInfoPanel.add(sizeLabel, c);
        c.gridy = 2;
        c.gridx = 2;
        currentGameInfoPanel.add(sizeValueLabel, c);

    }


    private void initInfoPanel() {
        initCurrentInfoPanel();

        infoPanel.setBackground(Color.LIGHT_GRAY);
        infoPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = GridBagConstraints.RELATIVE;
        c.gridx = 0;
        c.weightx = 1;
        c.weighty = 1;
        ratingTable.removeEditor();
        infoPanel.add(new JScrollPane(ratingTable), c);

        newGameButton.setName(Protocol.getNewGameButtonName());
        newGameButton.setText(Protocol.getNewGameButtonName());
        infoPanel.add(newGameButton, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        infoPanel.add(currentGameInfoPanel, c);

        infoPanel.add(new JScrollPane(allGamesTable), c);
        newGameButton.setName(Protocol.getNewGameButtonName());
        newGameButton.setText(Protocol.getNewGameButtonName());
    }

    private void initMain() {
        setLayout(new BorderLayout());
        JPanel pane = new JPanel(new BorderLayout());
        setContentPane(pane);
        initInfoPanel();
        gamePanel.setBackground(Color.GREEN);
        pane.add(gamePanel, BorderLayout.CENTER);
        pane.add(infoPanel, BorderLayout.EAST);
    }

    public SnakeGUI(SnakeSwingController controller) {
        super("Snake");
        this.controller = controller;
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initMain();
    }

    public void startGame(int width, int height, int foodStatic, float foodPerPlayer, int delayMs,
                          float deadFoodProb){
        Dimension dim = gamePanel.getSize();
        double dotSize = Math.floor(Math.sqrt(dim.width * dim.height / width / height));
        gameBoard = new GameBoard(width, height, (int)dotSize);
        gamePanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = GridBagConstraints.RELATIVE;
        c.gridx = 0;
        c.weightx = 1;
        c.weighty = 1;
        gamePanel.add(gameBoard, c);
        this.pack();
        this.repaint();
    }
    class MainFormAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JButton btn = (JButton) actionEvent.getSource();
            System.out.println("Нажатие на кнопку <" + btn.getName() + ">");
            if (btn == newGameButton){
                NewGameForm newGameForm = new NewGameForm();
                newGameForm.setVisible(true);
            }
        }
    }
}
