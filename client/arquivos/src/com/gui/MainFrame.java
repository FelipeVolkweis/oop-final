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

/**
 * Classe que representa a janela principal da aplicação.
 * Esta classe estende a classe JFrame e contém os componentes principais da interface gráfica.
 */
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

    /**
     * Atualiza o arquivo selecionado e atualiza a exibição na barra lateral esquerda.
     * 
     * @param fileName O nome do arquivo selecionado.
     */
    public static void handleSelectFile(String fileName) {
        selectFile = fileName;
        LeftBar.updateSelectFile(fileName);
    }

    /**
     * Inicia uma conexão de socket com o servidor.
     * 
     * @param selectedFile O arquivo selecionado para ser enviado ao servidor.
     * @param ipNumber O número de IP do servidor.
     * @param portNumber O número da porta do servidor.
     */
    public static void startSocketConnection(String selectedFile, String ipNumber, int portNumber) {
        socketConnection = new SocketConnection(ipNumber, portNumber);
        socketConnection.connect(selectedFile);
    }

    /**
     * Desconecta a conexão do socket, se estiver conectado.
     */
    public static void disconnectSocketConnection() {
        if (socketConnection != null && socketConnection.isConnected()) {
            socketConnection.disconnect();
        }
    }

    /**
     * Verifica se a conexão está ativa.
     * 
     * @return true se a conexão estiver ativa, caso contrário, retorna false.
     */
    public static boolean isConnectionActive() {
        return socketConnection != null && socketConnection.isConnected();
    }

    /**
     * Manipula a resposta do socket.
     * 
     * @param response A resposta do socket.
     * @param onSuccess A ação a ser executada em caso de sucesso.
     * @param onNotFound A ação a ser executada caso o recurso não seja encontrado.
     * @param onError A ação a ser executada em caso de erro.
     */
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

    /**
     * Cria um arquivo binário a partir de um arquivo CSV.
     * 
     * @param fileName o nome do arquivo CSV
     */
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

    /**
     * Deleta um jogador pelo seu ID.
     * 
     * @param playerId o ID do jogador a ser deletado
     * @param callback uma função a ser executada após a operação de deleção
     */
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

    /**
     * Atualiza um jogador na tabela e no servidor.
     * 
     * @param playerToUpdate O jogador a ser atualizado.
     * @param table A tabela onde o jogador está sendo exibido.
     */
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

    /**
     * Retorna uma string formatada contendo os dados do jogador a ser atualizado.
     * 
     * @param playerToUpdate O jogador a ser atualizado.
     * @return Uma string formatada contendo os dados do jogador.
     */
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

    /**
     * Obtém todos os jogadores a partir de um arquivo.
     * 
     * @param fileName O nome do arquivo.
     */
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

    /**
     * Seleciona os jogadores.
     * 
     * Envia uma mensagem de seleção de arquivo para o servidor através da conexão de socket.
     * Em caso de sucesso, atualiza a tabela de jogadores com a lista de jogadores recebida na resposta.
     * Caso nenhum jogador seja encontrado, atualiza a tabela com uma lista vazia e exibe uma mensagem informativa.
     * Em caso de falha, exibe uma mensagem de erro com a descrição do erro.
     */
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

    /**
     * Desativa os componentes relacionados à conexão.
     * 
     * Esta função desativa os componentes da barra de navegação e da barra lateral,
     * além de chamar a função handleSelectFile passando null como parâmetro.
     */
    public static void disableComponentsWithConnection() {
        NavBar.setComponentsEnabled(true);
        NavBar.setDisconnectButtonEnabled(false);
        LeftBar.setComponentsEnabled(false);
        MainFrame.handleSelectFile(null);
    }

    /**
     * Habilita os componentes da interface gráfica quando há uma conexão estabelecida.
     */
    public static void enableComponentsWithConnection() {
        NavBar.setComponentsEnabled(false);
        NavBar.setDisconnectButtonEnabled(true);
    }

    /**
     * Habilita os componentes relacionados à criação de arquivo.
     * 
     * @param selectFile O arquivo selecionado.
     */
    public static void enableComponentsWithFileCreation(String selectFile) {
        LeftBar.setComponentsEnabled(true);
        LeftBar.updateButtonState();
        MainFrame.handleSelectFile(selectFile);
    }

    /**
     * Reproduz um arquivo de áudio em uma nova thread.
     * 
     * @param audioPatch O caminho do arquivo de áudio a ser reproduzido.
     */
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

    /**
     * Abre uma caixa de diálogo para editar as informações de um jogador.
     * 
     * @param player O jogador a ser editado.
     */
    public static void onEditButtonClicked(Player player) {
        PlayerEditDialog dialog = new PlayerEditDialog(instance, player);
        dialog.setVisible(true);
    }

    /**
     * Inicializa o menu da janela principal.
     */
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

    /**
     * Exibe a janela de lista de jogadores.
     * Verifica se a conexão do socket está ativa e se um arquivo foi carregado antes de exibir a janela.
     * Caso contrário, exibe uma mensagem de aviso.
     * Chama o método getAllPlayersForTextDisplay() para obter todos os jogadores para exibição em texto.
     */
    private void showPlayersListWindow(ActionEvent e) {
        if (socketConnection == null || !socketConnection.isConnected() || selectFile == null) {
            JOptionPane.showMessageDialog(this, "Primeiro, certifique-se de que um arquivo foi carregado e que a conexão está ativa.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        getAllPlayersForTextDisplay();
    }

    /**
     * Obtém todos os jogadores para exibição em formato de texto.
     * 
     * @param selectFile O arquivo selecionado.
     */
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

    /**
     * Exibe os jogadores em uma janela de texto.
     * 
     * @param players a lista de jogadores a ser exibida
     */
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

    /**
     * Retorna uma instância de JTextArea preenchida com os dados dos jogadores.
     * 
     * @param players a lista de jogadores
     * @return uma instância de JTextArea preenchida com os dados dos jogadores
     */
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