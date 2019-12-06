package fit.networks.gui;

import fit.networks.controller.GameController;
import fit.networks.controller.GameControllerImpl;
import fit.networks.gui.protocol.Protocol;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

public class InfoPanel extends JPanel {
    private Object[][] rating = new String[][]{{"0", "1", "2"}};
    private Object[][] allGames = new String[][]{{"", "", "", "", ""}};
    private JTable ratingTable = new JTable(rating, Protocol.getRatingColumnsHeaders());
    private JTable allGamesTable = new JTable(allGames, Protocol.getAllGamesColumnsHeaders());
    private JButton newGameButton = new JButton(new MainFormAction());
    private JButton leaveGameButton = new JButton(new MainFormAction());
    private JButton joinGameButton = new JButton(new MainFormAction());
    private CurrentInfoPanel currentInfoPanel = new CurrentInfoPanel();
    private int row = -1;
    private Logger logger = Logger.getLogger("info panel");
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
                return true;
            }
        };

        for (int i = 0; i < 4; i++) {
            model.addColumn(Protocol.getAllGamesColumnsHeaders()[i]);
        }

        allGamesTable = new JTable(model);
        allGamesTable.setFocusable(true);
        allGamesTable.requestFocus();
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
        requestFocus();
        add(joinGameButton, c);
        ListSelectionModel selectionModel = allGamesTable.getSelectionModel();

        selectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                handleSelectionEvent(e);
            }
        });
    }

    protected void handleSelectionEvent(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return;
        String strSource= e.getSource().toString();
        int start = strSource.indexOf("{")+1,
                stop  = strSource.length()-1;
        System.out.println(strSource);
        String num = strSource.substring(start, stop);
        if (!num.equals(""))
            row = Integer.parseInt(num);
        System.out.println(row);
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
        int indexStartAddress = gameInfoStr.indexOf("["),
                indexFinishAddress = gameInfoStr.indexOf("]"),
                indexStartPort = gameInfoStr.lastIndexOf("["),
                indexFinishPort = gameInfoStr.lastIndexOf("]");

        String addressStr = gameInfoStr.substring(indexStartAddress + 1, indexFinishAddress);
        String portStr = gameInfoStr.substring(indexStartPort + 1, indexFinishPort);
        System.out.println(addressStr + portStr);
        GameControllerImpl.getInstance().joinGame(addressStr, Integer.parseInt(portStr));
    }

    class MainFormAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JButton btn = (JButton) actionEvent.getSource();
            logger.info(btn.getName() + (btn == joinGameButton));
            if (btn == newGameButton) {
                GameParamsForm newGameForm = new GameParamsForm();
                newGameForm.setVisible(true);
                leaveGameButton.setEnabled(true);
                newGameButton.setEnabled(false);
            } else if (btn == leaveGameButton) {
                GameControllerImpl.getInstance().leaveGame();
                newGameButton.setEnabled(true);
                leaveGameButton.setEnabled(false);
            } else if (btn == joinGameButton) {
                sendJoinRequest(allGamesTable.getModel().getValueAt(row, 0));
            }
        }
    }
}
