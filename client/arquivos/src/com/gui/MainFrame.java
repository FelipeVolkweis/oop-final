package com.gui;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainFrame extends JFrame {
    public static String defaultIp = "127.0.0.1";
    public static int defaultPort = 8080;
    public static String selectFile = null;
    private static SocketConnection socketConnection;
    private static final List<String> fifaYears = Arrays.asList("FIFA17", "FIFA18", "FIFA19", "FIFA20", "FIFA21", "FIFA22", "FIFA23");
    static PlayerTable playerTable;
    private static MainFrame instance;

    public MainFrame() {
        instance = this;

        setTitle("FIFA");
        setSize(1400, 800);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("./resources/icon.png")));
        setIconImage(icon.getImage());

        NavBar navBar = new NavBar(fifaYears);
        add(navBar, BorderLayout.NORTH);

        LeftBar leftBar = new LeftBar();
        add(leftBar, BorderLayout.WEST);

        playerTable = new PlayerTable();
        add(PlayerTable.getTablePanel(), BorderLayout.CENTER);

        setLocationRelativeTo(null);

        MainFrame.playAudio("./resources/brasilsil.wav");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnectSocketConnection();
                System.exit(0);
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(MainFrame::disconnectSocketConnection));

        initializeMenu();
    }

    public static void handleSelectFile(String fileName) {
        selectFile = fileName;
        LeftBar.updateSelectFile(fileName);
    }

    public static void startSocketConnection(String selectedFile, String ipNumber, int portNumber) {
        socketConnection = new SocketConnection(ipNumber, portNumber);
        socketConnection.connect(selectedFile);
    }

    public static void disconnectSocketConnection() {
        if (socketConnection != null && socketConnection.isConnected()) {
            socketConnection.disconnect();
        }
    }

    public static boolean isConnectionActive() {
        return socketConnection != null && socketConnection.isConnected();
    }

    public static void handleSocketResponse(String response, Runnable onSuccess, Runnable onNotFound, Runnable onError) {
        int status = ResponseHandler.extractStatus(response);

        if (status == ResponseHandler.STATUS_OK) {
            SwingUtilities.invokeLater(onSuccess);
        } else if (status == ResponseHandler.STATUS_NOT_FOUND) {
            if (onNotFound != null) {
                SwingUtilities.invokeLater(onNotFound);
            } else {
                ResponseHandler.handleResponse(response);
            }
        } else {
            if (onError != null) {
                SwingUtilities.invokeLater(onError);
            } else {
                ResponseHandler.handleResponse(response);
            }
        }
    }

    public static void createBinaryFile(String fileName) {
        socketConnection.sendMessage(
                String.format("1 %s.csv %s.bin", fileName, fileName),
                new SocketConnection.ResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        Runnable onSuccess = () -> MainFrame.enableComponentsWithFileCreation(fileName);
                        Runnable onError = () -> JOptionPane.showMessageDialog(null, "Arquivo informado não foi encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                        handleSocketResponse(response, onSuccess, null, onError);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        JOptionPane.showMessageDialog(null, "Falha ao criar arquivo binário: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
        );
    }

    public static void deletePlayer(int playerId, Runnable callback) {
        socketConnection.sendMessage(
                String.format("5 %s.bin %sIndice.bin 1\n1 id %s", selectFile, selectFile, playerId),
                new SocketConnection.ResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        Runnable onNotFound = () -> JOptionPane.showMessageDialog(null, "Jogador não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                        Runnable onError = () -> JOptionPane.showMessageDialog(null, "Falha ao deletar o jogador.", "Erro", JOptionPane.ERROR_MESSAGE);
                        handleSocketResponse(response, callback, onNotFound, onError);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        JOptionPane.showMessageDialog(null, "Falha ao deletar o jogador de id: " + playerId + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
        );
    }

    public static void updatePlayer(Player playerToUpdate, JTable table) {
        deletePlayer(playerToUpdate.getId(), () -> {
            String updateCommand = getString(playerToUpdate);
            socketConnection.sendMessage(
                    updateCommand,
                    new SocketConnection.ResponseCallback() {
                        @Override
                        public void onResponse(String response) {
                            ResponseHandler.handleResponse(response);
                            if (ResponseHandler.extractStatus(response) == ResponseHandler.STATUS_OK) {
                                SwingUtilities.invokeLater(() -> ((PlayerTableModel) table.getModel()).updatePlayer(playerToUpdate));
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, String.format("Falha em atualizar jogador de id: %s", playerToUpdate.getId()), "Erro", JOptionPane.ERROR_MESSAGE));
                        }
                    }
            );
        });
    }

    private static String getString(Player playerToUpdate) {
        String ageStr = (playerToUpdate.getAge() == -1) ? "-1" : String.valueOf(playerToUpdate.getAge());
        String playerNameStr = (playerToUpdate.getPlayerName().equals("NULO")) ? "NULO" : "\"" + playerToUpdate.getPlayerName() + "\"";
        String nationalityStr = (playerToUpdate.getNationality().equals("NULO")) ? "NULO" : "\"" + playerToUpdate.getNationality() + "\"";
        String clubNameStr = (playerToUpdate.getClubName().equals("NULO")) ? "NULO" : "\"" + playerToUpdate.getClubName() + "\"";

        return String.format(
                "6 %s.bin %sIndice.bin 1\n%s %s %s %s %s",
                selectFile, selectFile,
                playerToUpdate.getId(),
                ageStr,
                playerNameStr,
                nationalityStr,
                clubNameStr
        );
    }

    public static void getAllPlayers(String fileName) {
        socketConnection.sendMessage(
                String.format("2 %s.bin", fileName),
                new SocketConnection.ResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        Runnable onSuccess = () -> {
                            ResponseDto responseDto = JsonParser.parseResponse(response);
                            PlayerTable.updateTable(responseDto.getPlayerList());
                        };
                        Runnable onNotFound = () -> {
                            PlayerTable.updateTable(Collections.emptyList());
                            JOptionPane.showMessageDialog(null, "O arquivo não tem nenhum jogador.", "Info", JOptionPane.ERROR_MESSAGE);
                        };
                        handleSocketResponse(response, onSuccess, onNotFound, null);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        JOptionPane.showMessageDialog(null, "Falha em obter os jogadores: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
        );
    }

    public static void selectPlayers() {
        socketConnection.sendMessage(
                LeftBar.buildQuery(selectFile),
                new SocketConnection.ResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        Runnable onSuccess = () -> {
                            ResponseDto responseDto = JsonParser.parseResponse(response);
                            PlayerTable.updateTable(responseDto.getPlayerList());
                        };
                        Runnable onNotFound = () -> {
                            PlayerTable.updateTable(Collections.emptyList());
                            JOptionPane.showMessageDialog(null, "Nenhum jogador foi encontrado.", "Info", JOptionPane.INFORMATION_MESSAGE);
                        };
                        handleSocketResponse(response, onSuccess, onNotFound, null);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        JOptionPane.showMessageDialog(null, "Falha em selecionar os jogadores: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
        );
    }

    public static void disableComponentsWithConnection() {
        NavBar.setComponentsEnabled(true);
        NavBar.setDisconnectButtonEnabled(false);
        LeftBar.setComponentsEnabled(false);
        MainFrame.handleSelectFile(null);
    }

    public static void enableComponentsWithConnection() {
        NavBar.setComponentsEnabled(false);
        NavBar.setDisconnectButtonEnabled(true);
    }

    public static void enableComponentsWithFileCreation(String selectFile) {
        LeftBar.setComponentsEnabled(true);
        LeftBar.updateButtonState();
        MainFrame.handleSelectFile(selectFile);
    }

    static void playAudio(String audioPatch) {
        new Thread(() -> {
            try {
                File audioFile = new File(Objects.requireNonNull(MainFrame.class.getResource(audioPatch)).getFile());
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void onEditButtonClicked(Player player) {
        PlayerEditDialog dialog = new PlayerEditDialog(instance, player);
        dialog.setVisible(true);
    }

    private void initializeMenu() {
        // ---- menu ----
        JMenuBar menuBar = new JMenuBar();

        JMenu optionsMenu = new JMenu("Opções");
        JMenuItem listPlayersMenuItem = new JMenuItem("Listar Jogadores em Texto");
        listPlayersMenuItem.addActionListener(this::showPlayersListWindow);

        optionsMenu.add(listPlayersMenuItem);
        menuBar.add(optionsMenu);

        setJMenuBar(menuBar);
    }

    // ---- menu ----

    private void showPlayersListWindow(ActionEvent e) {
        if (socketConnection == null || !socketConnection.isConnected() || selectFile == null) {
            JOptionPane.showMessageDialog(this, "Primeiro, certifique-se de que um arquivo foi carregado e que a conexão está ativa.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        getAllPlayersForTextDisplay();
    }

    public static void getAllPlayersForTextDisplay() {
        socketConnection.sendMessage(
                String.format("2 %s.bin", selectFile),
                new SocketConnection.ResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        SwingUtilities.invokeLater(() -> {
                            ResponseDto responseDto = JsonParser.parseResponse(response);
                            displayPlayersInTextWindow(responseDto.getPlayerList());
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        JOptionPane.showMessageDialog(null, "Falha em obter os jogadores: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
        );
    }

    private static void displayPlayersInTextWindow(List<Player> players) {
        JTextArea textArea = getjTextArea(players);

        JScrollPane scrollPane = new JScrollPane(textArea);
        JDialog dialog = new JDialog(instance, "Lista de Jogadores em Formato CSV", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.add(scrollPane);
        dialog.pack();
        dialog.setLocationRelativeTo(instance);
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
        dialog.setVisible(true);
    }

    private static JTextArea getjTextArea(List<Player> players) {
        JTextArea textArea = new JTextArea(20, 40);
        textArea.setEditable(false);

        textArea.append("ID, Idade, Nome do Jogador, Nacionalidade, Nome do Clube\n");

        for (Player player : players) {
            textArea.append(String.format("%d, %d, %s, %s, %s\n",
                    player.getId(),
                    player.getAge(),
                    player.getPlayerName().replace("NULO", ""),
                    player.getNationality().replace("NULO", ""),
                    player.getClubName().replace("NULO", "")
            ));
        }
        return textArea;
    }
}