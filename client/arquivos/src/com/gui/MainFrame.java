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
    // Configurações padrão de IP e porta
    public static String defaultIp = "127.0.0.1";
    public static int defaultPort = 8080;
    public static String selectFile = null; // Arquivo selecionado pelo usuário
    private static SocketConnection socketConnection; // Conexão de socket
    // Lista de anos FIFA suportados
    private static final List<String> fifaYears = Arrays.asList("FIFA17", "FIFA18", "FIFA19", "FIFA20", "FIFA21", "FIFA22", "FIFA23");
    static PlayerTable playerTable; // Tabela de jogadores
    private static MainFrame instance; // Instância da janela principal

    /**
     * Construtor da classe MainFrame.
     * Configura a janela principal e seus componentes.
     */
    public MainFrame() {
        instance = this;

        setTitle("FIFA"); // Define o título da janela
        setSize(1400, 800); // Define o tamanho da janela
        setLayout(new BorderLayout()); // Define o layout da janela
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Define a operação de fechamento

        // Define o ícone da janela
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("./resources/icon.png")));
        setIconImage(icon.getImage());

        // Cria e adiciona a barra de navegação
        NavBar navBar = new NavBar(fifaYears);
        add(navBar, BorderLayout.NORTH);

        // Cria e adiciona a barra lateral
        LeftBar leftBar = new LeftBar();
        add(leftBar, BorderLayout.WEST);

        // Cria e adiciona a tabela de jogadores
        playerTable = new PlayerTable();
        add(PlayerTable.getTablePanel(), BorderLayout.CENTER);

        setLocationRelativeTo(null);  // Centraliza a janela na tela

        // Reproduz um áudio
        MainFrame.playAudio("./resources/brasilsil.wav");

        // Adiciona um listener para o evento de fechamento da janela
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnectSocketConnection(); // Desconecta a conexão do socket ao fechar a janela
                System.exit(0); // Encerra a aplicação
            }
        });

        // Adiciona um hook para desconectar a conexão do socket ao encerrar a aplicação
        Runtime.getRuntime().addShutdownHook(new Thread(MainFrame::disconnectSocketConnection));

        initializeMenu(); // Inicializa o menu
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
        socketConnection = new SocketConnection(ipNumber, portNumber); // Cria uma nova conexão de socket
        socketConnection.connect(selectedFile); // Conecta ao servidor
    }

    /**
     * Desconecta a conexão do socket, se estiver conectado.
     */
    public static void disconnectSocketConnection() {
        if (socketConnection != null && socketConnection.isConnected()) {
            socketConnection.disconnect(); // Desconecta do servidor
        }
    }

    /**
     * Verifica se a conexão está ativa.
     *
     * @return true se a conexão estiver ativa, caso contrário, retorna false.
     */
    public static boolean isConnectionActive() {
        return socketConnection != null && socketConnection.isConnected();
    } // Verifica o status da conexão

    /**
     * Manipula a resposta do socket.
     *
     * @param response A resposta do socket.
     * @param onSuccess A ação a ser executada em caso de sucesso.
     * @param onNotFound A ação a ser executada caso o recurso não seja encontrado.
     * @param onError A ação a ser executada em caso de erro.
     */
    public static void handleSocketResponse(String response, Runnable onSuccess, Runnable onNotFound, Runnable onError) {
        int status = ResponseHandler.extractStatus(response); // Extrai o status da resposta

        if (status == ResponseHandler.STATUS_OK) {
            SwingUtilities.invokeLater(onSuccess); // Executa a ação de sucesso na thread de eventos do Swing
        } else if (status == ResponseHandler.STATUS_NOT_FOUND) {
            if (onNotFound != null) {
                SwingUtilities.invokeLater(onNotFound); // Executa a ação de "não encontrado" na thread de eventos do Swing
            } else {
                ResponseHandler.handleResponse(response); // Manipula a resposta padrão
            }
        } else {
            if (onError != null) {
                SwingUtilities.invokeLater(onError); // Executa a ação de erro na thread de eventos do Swing
            } else {
                ResponseHandler.handleResponse(response); // Manipula a resposta padrão

            }
        }
    }

    /**
     * Cria um arquivo binário a partir de um arquivo CSV.
     *
     * @param fileName o nome do arquivo CSV
     */
    public static void createBinaryFile(String fileName) {
        // Envia uma mensagem para o servidor para criar o arquivo binário
        socketConnection.sendMessage(
                String.format("1 %s.csv %s.bin", fileName, fileName),
                new SocketConnection.ResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        // Define as ações a serem executadas em caso de sucesso ou erro
                        Runnable onSuccess = () -> MainFrame.enableComponentsWithFileCreation(fileName);
                        Runnable onError = () -> JOptionPane.showMessageDialog(null, "Arquivo informado não foi encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                        handleSocketResponse(response, onSuccess, null, onError); // Manipula a resposta do socket
                    }

                    @Override
                    public void onFailure(Exception e) {
                        JOptionPane.showMessageDialog(null, "Falha ao criar arquivo binário: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE); // Exibe mensagem de erro
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
        // Envia uma mensagem para o servidor para deletar o jogador
        socketConnection.sendMessage(
                String.format("5 %s.bin %sIndice.bin 1\n1 id %s", selectFile, selectFile, playerId),
                new SocketConnection.ResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        // Define as ações a serem executadas em caso de "não encontrado" ou erro
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
        // Primeiro deleta o jogador existente
        deletePlayer(playerToUpdate.getId(), () -> {
            // Obtém o comando de atualização formatado
            String updateCommand = getString(playerToUpdate);
            // Envia uma mensagem para o servidor para atualizar o jogador
            socketConnection.sendMessage(
                    updateCommand,
                    new SocketConnection.ResponseCallback() {
                        @Override
                        public void onResponse(String response) {
                            ResponseHandler.handleResponse(response); // Manipula a resposta do servidor
                            if (ResponseHandler.extractStatus(response) == ResponseHandler.STATUS_OK) {
                                // Atualiza a tabela de jogadores na interface gráfica
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
        // Converte a idade do jogador para string, usando "-1" se a idade for -1
        String ageStr = (playerToUpdate.getAge() == -1) ? "-1" : String.valueOf(playerToUpdate.getAge());
        // Formata o nome do jogador, usando "NULO" se o nome for "NULO", caso contrário, coloca o nome entre aspas
        String playerNameStr = (playerToUpdate.getPlayerName().equals("NULO")) ? "NULO" : "\"" + playerToUpdate.getPlayerName() + "\"";
        // Formata a nacionalidade do jogador, usando "NULO" se a nacionalidade for "NULO", caso contrário, coloca a nacionalidade entre aspas
        String nationalityStr = (playerToUpdate.getNationality().equals("NULO")) ? "NULO" : "\"" + playerToUpdate.getNationality() + "\"";
        // Formata o nome do clube do jogador, usando "NULO" se o nome do clube for "NULO", caso contrário, coloca o nome do clube entre aspas
        String clubNameStr = (playerToUpdate.getClubName().equals("NULO")) ? "NULO" : "\"" + playerToUpdate.getClubName() + "\"";

        // Retorna a string formatada contendo os dados do jogador a ser atualizado
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
        // Envia uma mensagem para o servidor para obter todos os jogadores do arquivo especificado
        socketConnection.sendMessage(
                String.format("2 %s.bin", fileName),
                new SocketConnection.ResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        // Define as ações a serem executadas em caso de sucesso ou "não encontrado"
                        Runnable onSuccess = () -> {
                            // Analisa a resposta e atualiza a tabela de jogadores
                            ResponseDto responseDto = JsonParser.parseResponse(response);
                            PlayerTable.updateTable(responseDto.getPlayerList());
                        };
                        Runnable onNotFound = () -> {
                            // Atualiza a tabela de jogadores com uma lista vazia e exibe uma mensagem informativa
                            PlayerTable.updateTable(Collections.emptyList());
                            JOptionPane.showMessageDialog(null, "O arquivo não tem nenhum jogador.", "Info", JOptionPane.ERROR_MESSAGE);
                        };
                        // Manipula a resposta do socket
                        handleSocketResponse(response, onSuccess, onNotFound, null);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Exibe uma mensagem de erro se a obtenção dos jogadores falhar
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
        // Envia uma mensagem para o servidor para selecionar os jogadores do arquivo especificado
        socketConnection.sendMessage(
                LeftBar.buildQuery(selectFile),
                new SocketConnection.ResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        // Define as ações a serem executadas em caso de sucesso ou "não encontrado"
                        Runnable onSuccess = () -> {
                            // Analisa a resposta e atualiza a tabela de jogadores
                            ResponseDto responseDto = JsonParser.parseResponse(response);
                            PlayerTable.updateTable(responseDto.getPlayerList());
                        };
                        Runnable onNotFound = () -> {
                            // Atualiza a tabela de jogadores com uma lista vazia e exibe uma mensagem informativa
                            PlayerTable.updateTable(Collections.emptyList());
                            JOptionPane.showMessageDialog(null, "Nenhum jogador foi encontrado.", "Info", JOptionPane.INFORMATION_MESSAGE);
                        };
                        // Manipula a resposta do socket
                        handleSocketResponse(response, onSuccess, onNotFound, null);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Exibe uma mensagem de erro se a seleção dos jogadores falhar
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
        // Habilita os componentes da barra de navegação e desativa o botão de desconexão
        NavBar.setComponentsEnabled(true);
        NavBar.setDisconnectButtonEnabled(false);
        // Desativa os componentes da barra lateral
        LeftBar.setComponentsEnabled(false);
        // Atualiza a barra lateral com nenhum arquivo selecionado
        MainFrame.handleSelectFile(null);
    }

    /**
     * Habilita os componentes da interface gráfica quando há uma conexão estabelecida.
     */
    public static void enableComponentsWithConnection() {
        // Desativa os componentes da barra de navegação e habilita o botão de desconexão
        NavBar.setComponentsEnabled(false);
        NavBar.setDisconnectButtonEnabled(true);
    }

    /**
     * Habilita os componentes relacionados à criação de arquivo.
     *
     * @param selectFile O arquivo selecionado.
     */
    public static void enableComponentsWithFileCreation(String selectFile) {
        // Habilita os componentes da barra lateral
        LeftBar.setComponentsEnabled(true);
        // Atualiza o estado dos botões da barra lateral
        LeftBar.updateButtonState();
        // Atualiza a barra lateral com o arquivo selecionado
        MainFrame.handleSelectFile(selectFile);
    }

    /**
     * Reproduz um arquivo de áudio em uma nova thread.
     *
     * @param audioPatch O caminho do arquivo de áudio a ser reproduzido.
     */
    static void playAudio(String audioPatch) {
        // Cria uma nova thread para reproduzir o áudio
        new Thread(() -> {
            try {
                // Obtém o arquivo de áudio e cria um fluxo de entrada de áudio
                File audioFile = new File(Objects.requireNonNull(MainFrame.class.getResource(audioPatch)).getFile());
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                // Abre o clipe de áudio e inicia a reprodução
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace(); // Exibe a pilha de chamadas em caso de erro
            }
        }).start();
    }

    /**
     * Abre uma caixa de diálogo para editar as informações de um jogador.
     *
     * @param player O jogador a ser editado.
     */
    public static void onEditButtonClicked(Player player) {
        // Cria e exibe uma caixa de diálogo para editar as informações do jogador
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
        // Adiciona um listener ao item de menu para exibir a janela de lista de jogadores
        listPlayersMenuItem.addActionListener(this::showPlayersListWindow);

        optionsMenu.add(listPlayersMenuItem);
        menuBar.add(optionsMenu);

        setJMenuBar(menuBar); // Define a barra de menu na janela principal
    }

    // ---- menu ----

    /**
     * Exibe a janela de lista de jogadores.
     * Verifica se a conexão do socket está ativa e se um arquivo foi carregado antes de exibir a janela.
     * Caso contrário, exibe uma mensagem de aviso.
     * Chama o método getAllPlayersForTextDisplay() para obter todos os jogadores para exibição em texto.
     */
    private void showPlayersListWindow(ActionEvent e) {
        // Verifica se a conexão do socket está ativa e se um arquivo foi carregado
        if (socketConnection == null || !socketConnection.isConnected() || selectFile == null) {
            // Exibe uma mensagem de aviso se a conexão não estiver ativa ou nenhum arquivo estiver carregado
            JOptionPane.showMessageDialog(this, "Primeiro, certifique-se de que um arquivo foi carregado e que a conexão está ativa.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        getAllPlayersForTextDisplay();
    }

    /**
     * Obtém todos os jogadores para exibição em formato de texto.
     */
    public static void getAllPlayersForTextDisplay() {
        // Envia uma mensagem para o servidor para obter todos os jogadores do arquivo selecionado
        socketConnection.sendMessage(
                String.format("2 %s.bin", selectFile), // Formata a mensagem com o nome do arquivo selecionado
                new SocketConnection.ResponseCallback() { // Define o callback para a resposta do servidor
                    @Override
                    public void onResponse(String response) {
                        // Atualiza a interface gráfica na thread de eventos do Swing
                        SwingUtilities.invokeLater(() -> {
                            // Analisa a resposta JSON e obtém a lista de jogadores
                            ResponseDto responseDto = JsonParser.parseResponse(response);
                            // Exibe os jogadores em uma janela de texto
                            displayPlayersInTextWindow(responseDto.getPlayerList());
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Exibe uma mensagem de erro se a obtenção dos jogadores falhar
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
        // Cria uma JTextArea preenchida com os dados dos jogadores
        JTextArea textArea = getjTextArea(players);

        // Cria um JScrollPane para permitir a rolagem da JTextArea
        JScrollPane scrollPane = new JScrollPane(textArea);
        // Cria um JDialog para exibir os dados dos jogadores
        JDialog dialog = new JDialog(instance, "Lista de Jogadores em Formato CSV", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.add(scrollPane); // Adiciona o JScrollPane ao JDialog
        dialog.pack(); // Ajusta o tamanho do JDialog
        dialog.setLocationRelativeTo(instance); // Centraliza o JDialog em relação à janela principal
        // Garante que o scroll esteja no topo
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
        dialog.setVisible(true); // Torna o JDialog visível
    }

    /**
     * Retorna uma instância de JTextArea preenchida com os dados dos jogadores.
     *
     * @param players a lista de jogadores
     * @return uma instância de JTextArea preenchida com os dados dos jogadores
     */
    private static JTextArea getjTextArea(List<Player> players) {
        // Cria uma JTextArea com 20 linhas e 40 colunas
        JTextArea textArea = new JTextArea(20, 40);
        textArea.setEditable(false); // Define a JTextArea como não editável

        // Cabeçalho do CSV
        textArea.append("ID,Idade,Nome do Jogador,Nacionalidade,Nome do Clube\n");

        // Iteração e formatação de cada jogador
        for (Player player : players) {
            String idade = player.getAge() == -1 ? "" : String.valueOf(player.getAge()); // Converte -1 para string vazia
            // Converte o nome do jogador, nacionalidade e nome do clube, substituindo "NULO" por uma string vazia
            String nomeJogador = player.getPlayerName().equals("NULO") ? "" : player.getPlayerName();
            String nacionalidade = player.getNationality().equals("NULO") ? "" : player.getNationality();
            String nomeClube = player.getClubName().equals("NULO") ? "" : player.getClubName();

            // Adiciona os dados do jogador à JTextArea em formato CSV
            textArea.append(String.format("%d,%s,%s,%s,%s\n",
                    player.getId(),
                    idade,
                    nomeJogador,
                    nacionalidade,
                    nomeClube
            ));
        }
        return textArea; // Retorna a JTextArea preenchida
    }
}