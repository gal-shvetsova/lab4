package fit.networks.gui;

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
    private JButton newGameButton = new JButton(new NewGameAction());
    private JTable ratingTable = new JTable(rating, Protocol.getRatingColumnsHeaders());
    private JTable allGamesTable = new JTable(allGames, Protocol.getAllGamesColumnsHeaders());
    private JPanel gamePanel = new JPanel();
    private JPanel infoPanel = new JPanel();
    private JPanel currentGameInfoPanel = new JPanel();
    private JPanel gameFieldPanel = new JPanel();

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

    private void initGamePanel() {
        gamePanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.gridx = 0;
        c.weightx = 1;
        c.weighty = 1;
        gamePanel.add(gameFieldPanel, c);
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
        initGamePanel();
        initInfoPanel();
        pane.add(gamePanel, BorderLayout.CENTER);
        pane.add(infoPanel, BorderLayout.EAST);
    }

    public SnakeGUI() {
        super("Snake");
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initMain();

    }


    class NewGameAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JButton btn = (JButton) actionEvent.getSource();
            System.out.println("Нажатие на кнопку <" + btn.getName() + ">");
        }
    }
}
