package fit.networks.gui;

import fit.networks.controller.SnakeControllerImpl;
import fit.networks.game.GameConfig;
import fit.networks.gui.protocol.Protocol;

import javax.swing.*;
import java.awt.*;

public class CurrentInfoPanel extends JPanel {
    private final JLabel leadingLabel = new JLabel(Protocol.getLeadingLabelName());
    private final JLabel sizeLabel = new JLabel(Protocol.getSizeLabelName());
    private final JLabel foodLabel = new JLabel(Protocol.getFoodLabelName());
    private JLabel leadingValueLabel = new JLabel(Protocol.getLeadingLabelName());
    private JLabel sizeValueLabel = new JLabel(Protocol.getSizeLabelName());
    private JLabel foodValueLabel = new JLabel(Protocol.getFoodLabelName());


    public CurrentInfoPanel(){
        super();
        initCurrentInfoPanel();
    }

    private void initCurrentInfoPanel() {
        setBackground(Color.LIGHT_GRAY);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.gridx = 0;
        add(leadingLabel, c);
        c.gridy = 0;
        c.gridx = 2;
        add(leadingValueLabel, c);
        c.gridy = 1;
        c.gridx = 0;
        add(foodLabel, c);
        c.gridy = 1;
        c.gridx = 2;
        add(foodValueLabel, c);
        c.gridy = 2;
        c.gridx = 0;
        add(sizeLabel, c);
        c.gridy = 2;
        c.gridx = 2;
        add(sizeValueLabel, c);
        hideGameInfo();
    }

    void setGameInfo(GameConfig gameConfig) {
        leadingValueLabel.setText(SnakeControllerImpl.getController().getName());
        foodValueLabel.setText(gameConfig.getFoodStatic() + " + " + gameConfig.getFoodPerPlayer() + "x");
        sizeValueLabel.setText(gameConfig.getWidth() + " * " + gameConfig.getHeight());

        leadingValueLabel.setVisible(true);
        foodValueLabel.setVisible(true);
        sizeValueLabel.setVisible(true);
    }

     void hideGameInfo() {
        leadingValueLabel.setVisible(false);
        foodValueLabel.setVisible(false);
        sizeValueLabel.setVisible(false);
    }
}
