package fit.networks.gui;

import fit.networks.controller.GameController;
import fit.networks.controller.GameControllerImpl;
import fit.networks.gui.protocol.Protocol;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class InfoPanel extends JPanel {
    private Object[][] rating = new String[][]{{"0", "1", "2"}};
    private Object[][] allGames = new String[][]{{"", "", "", "", ""}};
    private JTable ratingTable = new JTable(rating, Protocol.getRatingColumnsHeaders());
    private JTable allGamesTable = new JTable(allGames, Protocol.getAllGamesColumnsHeaders());
    private JButton newGameButton = new JButton(new MainFormAction());
    private JButton leaveGameButton = new JButton(new MainFormAction());
    private JButton joinGameButton = new JButton(new MainFormAction());
    private CurrentInfoPanel currentInfoPanel = new CurrentInfoPanel();

    public InfoPanel() {
        super();
        initInfoPanel();
    }

    public CurrentInfoPanel getCurrentInfoPanel() {
        return currentInfoPanel;
    }

    private void initButtons() {
        newGameButton.setName(Protocol.getNewGameButtonName());
        newGameButton.setText(Protocol.getNewGameButtonName());
        leaveGameButton.setName(Protocol.getLeaveGameButtonName());
        leaveGameButton.setText(Protocol.getLeaveGameButtonName());
        leaveGameButton.setEnabled(false);
        joinGameButton.setText(Protocol.getJoinGameButtonName());
        joinGameButton.setName(Protocol.getJoinGameButtonName());

    }

    private void initInfoPanel() {
        initButtons();
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (int i = 0; i < 4; i++) {
            model.addColumn(Protocol.getAllGamesColumnsHeaders()[i]);
        }

        allGamesTable = new JTable(model);
        allGamesTable.setFocusable(false);
        allGamesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        allGamesTable.setRowSelectionAllowed(true);
        setBackground(Color.LIGHT_GRAY);
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = GridBagConstraints.RELATIVE;
        c.gridx = 0;
        c.weightx = 1;
        c.weighty = 1;
        ratingTable.removeEditor();
        add(new JScrollPane(ratingTable));
        add(currentInfoPanel, c);
        add(newGameButton, c);
        add(leaveGameButton, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        add(new JScrollPane(allGamesTable), c);

        add(joinGameButton, c);
    }

    public void updateTable(String[][] games) {
        DefaultTableModel dm = (DefaultTableModel) allGamesTable.getModel();
        int rowCount = dm.getRowCount();
        for (int i = rowCount - 1; i >= 0; i--) {
            dm.removeRow(i);
        }

        for (String[] game : games) {
            dm.addRow(game);
        }
    }

    private void sendJoinRequest(Object gameInfo){
        String gameInfoStr = (String)gameInfo;
        System.out.println(gameInfo);
        int indexStartAddress = gameInfoStr.indexOf("["),
                indexFinishAddress = gameInfoStr.indexOf("]"),
                indexStartPort = gameInfoStr.lastIndexOf("["),
                indexFinishPort = gameInfoStr.lastIndexOf("]");

        String addressStr = gameInfoStr.substring(indexStartAddress + 1, indexFinishAddress);
        String portStr = gameInfoStr.substring(indexStartPort + 1, indexFinishPort);
        System.out.println(addressStr + portStr);
        GameControllerImpl.getController().joinGame(addressStr, Integer.parseInt(portStr));
    }

    class MainFormAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JButton btn = (JButton) actionEvent.getSource();
            if (btn == newGameButton) {
                GameParamsForm newGameForm = new GameParamsForm();
                newGameForm.setVisible(true);
                leaveGameButton.setEnabled(true);
                newGameButton.setEnabled(false);
            } else if (btn == leaveGameButton) {
                GameControllerImpl.getController().leaveGame();
                newGameButton.setEnabled(true);
                leaveGameButton.setEnabled(false);
            } else if (btn == joinGameButton) {
                int index = allGamesTable.getSelectedRow();
                sendJoinRequest(allGamesTable.getModel().getValueAt(index, 0));

            }
        }
    }
}
