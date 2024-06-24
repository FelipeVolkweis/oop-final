package com.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Objects;

public class NavBar extends JPanel {
    private static JTextField ipField;
    private static JTextField portField;
    private static JButton disconnectButton;
    private static JButton loadButton;

    public NavBar(List<String> fifaYears) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        Border bottomLine = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK);
        Border padding = new EmptyBorder(10, 10, 10, 10);
        setBorder(new CompoundBorder(bottomLine, padding));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        JPanel div1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JComboBox<String> dropdown = new JComboBox<>(fifaYears.toArray(new String[0]));
        loadButton = new JButton("Carregar arquivo");
        div1.add(dropdown);
        div1.add(loadButton);
        add(div1, gbc);

        gbc.gridx++;
        JPanel div2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        GUIUtils.FieldPanel ipFieldPanel = GUIUtils.inputField("IP:", 120, 15, false, 'x', MainFrame.defaultIp);
        ipField = ipFieldPanel.textField;
        div2.add(ipFieldPanel.panel);

        GUIUtils.FieldPanel portFieldPanel = GUIUtils.inputField("Porta:", 60, 6, true, 'x', MainFrame.defaultPort);
        portField = portFieldPanel.textField;
        div2.add(portFieldPanel.panel);

        disconnectButton = new JButton("Desconectar");
        div2.add(disconnectButton);

        add(div2, gbc);

        gbc.gridx++;
        PlayerImagePanel imagePanel = new PlayerImagePanel();
        add(imagePanel, gbc);

        loadButton.addActionListener(e -> {
            MainFrame.playAudio("./resources/ronaldo.wav");
            MainFrame.playerTable.showGifs();
            String selectedFile = (String) dropdown.getSelectedItem();
            if (MainFrame.isConnectionActive()) {
                MainFrame.createBinaryFile(selectedFile);
                return;
            }
            String ip = ipField.getText();
            int port = Integer.parseInt(portField.getText());
            MainFrame.startSocketConnection(selectedFile, ip, port);
        });

        disconnectButton.addActionListener(e -> {
            MainFrame.disconnectSocketConnection();
            MainFrame.playerTable.showGifs();
            MainFrame.playAudio("./resources/tetra.wav");
        });

        dropdown.addActionListener(e -> {
            if (dropdown.getSelectedItem() == MainFrame.selectFile) {
                loadButton.setEnabled(false);
                return;
            }
            loadButton.setEnabled(true);
        });

        setDisconnectButtonEnabled(false);
    }

    public static void setComponentsEnabled(boolean enabled) {
        ipField.setEnabled(enabled);
        portField.setEnabled(enabled);
    }

    public static void setDisconnectButtonEnabled(boolean enabled) {
        disconnectButton.setEnabled(enabled);
    }

    private static class PlayerImagePanel extends JPanel {
        public PlayerImagePanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));

            int imageWidth = 50;
            int imageHeight = 50;

            ImageIcon player1Icon = resizeImageIcon("./resources/player1.png", imageWidth, imageHeight);
            ImageIcon player2Icon = resizeImageIcon("./resources/player2.png", imageWidth, imageHeight);
            ImageIcon player3Icon = resizeImageIcon("./resources/player3.png", imageWidth, imageHeight);

            JButton player1Label = createImageButton(player1Icon, "./resources/cr7.wav");
            JButton player2Label = createImageButton(player2Icon, "./resources/messi.wav");
            JButton player3Label = createImageButton(player3Icon, "./resources/neymar.wav");

            add(player1Label);
            add(player2Label);
            add(player3Label);
        }

        private JButton createImageButton(ImageIcon icon, String musicPath) {
            JButton button = new JButton(icon);
            button.addActionListener(e -> MainFrame.playAudio(musicPath));
            button.setBorder(null);
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setRolloverEnabled(false);
            return button;
        }

        private ImageIcon resizeImageIcon(String path, int width, int height) {
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource(path)));
            Image image = icon.getImage();
            Image resizedImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(resizedImage);
        }
    }
}