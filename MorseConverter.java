import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MorseConverter {

    private JFrame frame;
    private JTextArea inputArea;
    private JTextArea outputArea;
    private JSlider volumeSlider;
    private JSlider speedSlider;
    private List<String> history;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                MorseConverter window = new MorseConverter();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public MorseConverter() {
        history = loadHistory();
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        JLabel lblInput = new JLabel("Input:");
        lblInput.setBounds(10, 11, 46, 14);
        frame.getContentPane().add(lblInput);

        inputArea = new JTextArea();
        inputArea.setBounds(10, 36, 564, 70);
        frame.getContentPane().add(inputArea);

        JButton btnTextToMorse = new JButton("Text to Morse");
        btnTextToMorse.setBounds(10, 117, 150, 23);
        btnTextToMorse.addActionListener(e -> convertTextToMorse());
        btnTextToMorse.setBackground(Color.CYAN);
        frame.getContentPane().add(btnTextToMorse);

        JButton btnMorseToText = new JButton("Morse to Text");
        btnMorseToText.setBounds(170, 117, 150, 23);
        btnMorseToText.addActionListener(e -> convertMorseToText());
        btnMorseToText.setBackground(Color.GREEN);
        frame.getContentPane().add(btnMorseToText);

        JButton btnSaveToFile = new JButton("Save to File");
        btnSaveToFile.setBounds(330, 117, 150, 23);
        btnSaveToFile.addActionListener(e -> saveTextToFile());
        btnSaveToFile.setBackground(Color.ORANGE);
        frame.getContentPane().add(btnSaveToFile);

        JButton btnGenerateQRCode = new JButton("Generate QR Code");
        btnGenerateQRCode.setBounds(490, 117, 150, 23);
        btnGenerateQRCode.addActionListener(e -> generateQRCode());
        btnGenerateQRCode.setBackground(Color.PINK);
        frame.getContentPane().add(btnGenerateQRCode);

        JLabel lblOutput = new JLabel("Output:");
        lblOutput.setBounds(10, 151, 46, 14);
        frame.getContentPane().add(lblOutput);

        outputArea = new JTextArea();
        outputArea.setBounds(10, 176, 564, 70);
        frame.getContentPane().add(outputArea);

        JButton btnPlayMorse = new JButton("Play Morse Code");
        btnPlayMorse.setBounds(10, 257, 150, 23);
        btnPlayMorse.addActionListener(e -> playMorseCode());
        btnPlayMorse.setBackground(Color.LIGHT_GRAY);
        frame.getContentPane().add(btnPlayMorse);

        JButton btnViewHistory = new JButton("View History");
        btnViewHistory.setBounds(170, 257, 150, 23);
        btnViewHistory.addActionListener(e -> viewHistory());
        btnViewHistory.setBackground(Color.YELLOW);
        frame.getContentPane().add(btnViewHistory);

        JButton btnExit = new JButton("Exit");
        btnExit.setBounds(490, 257, 150, 23);
        btnExit.addActionListener(e -> {
            saveHistory(history);
            System.exit(0);
        });
        btnExit.setBackground(Color.RED);
        frame.getContentPane().add(btnExit);

        JLabel lblVolume = new JLabel("Volume:");
        lblVolume.setBounds(10, 291, 60, 14);
        frame.getContentPane().add(lblVolume);

        volumeSlider = new JSlider(0, 100, 50);
        volumeSlider.setBounds(80, 291, 200, 23);
        frame.getContentPane().add(volumeSlider);

        JLabel lblSpeed = new JLabel("Speed:");
        lblSpeed.setBounds(10, 325, 60, 14);
        frame.getContentPane().add(lblSpeed);

        speedSlider = new JSlider(1, 10, 1);
        speedSlider.setBounds(80, 325, 200, 23);
        frame.getContentPane().add(speedSlider);
    }

    private void convertTextToMorse() {
        String text = inputArea.getText();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Input text cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String morseCode = textToMorse(text);
        outputArea.setText(morseCode);
        history.add("Text to Morse: " + text + " => " + morseCode);
    }

    private void convertMorseToText() {
        String morseCode = inputArea.getText();
        if (morseCode.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Input Morse code cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String text = morseToText(morseCode);
        outputArea.setText(text);
        history.add("Morse to Text: " + morseCode + " => " + text);
    }

    private void saveTextToFile() {
        String text = outputArea.getText();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Output text cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        saveToFile(text);
    }

    private void generateQRCode() {
        String morseCode = outputArea.getText();
        if (morseCode.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Output Morse code cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            generateQRCode(morseCode);
            JOptionPane.showMessageDialog(frame, "QR code generated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error generating QR code: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void playMorseCode() {
        String morseCode = outputArea.getText();
        if (morseCode.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Output Morse code cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int volume = volumeSlider.getValue();
        int speed = speedSlider.getValue();
        playMorseCode(morseCode, speed, 800, volume);
    }

    private void viewHistory() {
        StringBuilder historyText = new StringBuilder();
        for (String entry : history) {
            historyText.append(entry).append("\n");
        }
        JOptionPane.showMessageDialog(frame, historyText.toString(), "Conversion History", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveToFile(String textToSave) {
        String desktopPath = System.getProperty("user.home") + "/Desktop/";
        String fileName = "converted_text.txt";
        String filePath = desktopPath + fileName;

        try (PrintWriter writer = new PrintWriter(filePath)) {
            writer.println(textToSave);
            JOptionPane.showMessageDialog(frame, "Text saved to file: " + filePath, "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(frame, "Error saving text to file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String readFileContent(String filePath) throws FileNotFoundException {
        StringBuilder fileContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileContent.toString();
    }

    private static String textToMorse(String text) {
        String[] morseCodes = {
                ".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---",
                "-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.", "...", "-",
                "..-", "...-", ".--", "-..-", "-.--", "--..", "/", "-----", ".----",
                "..---", "...--", "....-", ".....", "-....", "--...", "---..", "----."
        };

        char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ/0123456789".toCharArray();


        text = text.toUpperCase();
        StringBuilder morseCode = new StringBuilder();
        for (char letter : text.toCharArray()) {
            int index = -1;
            for (int i = 0; i < alphabet.length; i++) {
                if (alphabet[i] == letter) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                morseCode.append(morseCodes[index]).append(" ");
            } else if (letter == ' ') {
                morseCode.append("/ ");
            }
        }
        return morseCode.toString();
    }

    private static String morseToText(String morseCode) {
        String[] morseCodes = {
                ".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---",
                "-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.", "...", "-",
                "..-", "...-", ".--", "-..-", "-.--", "--..", "/", "-----", ".----",
                "..---", "...--", "....-", ".....", "-....", "--...", "---..", "----."
        };

        char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ/0123456789".toCharArray();

        StringBuilder text = new StringBuilder();
        String[] words = morseCode.split("/");
        for (String word : words) {
            String[] letters = word.trim().split("\\s+");
            for (String letter : letters) {
                int index = -1;
                for (int i = 0; i < morseCodes.length; i++) {
                    if (morseCodes[i].equals(letter)) {
                        index = i;
                        break;
                    }
                }
                if (index != -1) {
                    text.append(alphabet[index]);
                }
            }
            text.append(" ");
        }
        return text.toString();
    }

    public static void playMorseCode(String morseCode) {
        playMorseCode(morseCode, 1, 800, 100);
    }

    public static void playMorseCode(String morseCode, int speed, int pitch, int volume) {
        try {
            AudioFormat audioFormat = new AudioFormat(8000 * speed, 8, 1, true, true);
            SourceDataLine line = AudioSystem.getSourceDataLine(audioFormat);
            line.open(audioFormat);
            line.start();

            String[] codes = morseCode.split("\\s+");
            for (String code : codes) {
                for (char c : code.toCharArray()) {
                    if (c == '.') {
                        playSound(line, 100 * speed, speed, pitch, volume);
                    } else if (c == '-') {
                        playSound(line, 300 * speed, speed, pitch, volume);
                    }
                    Thread.sleep(100 * speed);
                }
                Thread.sleep(300 * speed);
            }
            line.drain();
            line.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void playSound(SourceDataLine line, int ms, int speed, int freq, int volume) throws InterruptedException {
        byte[] buf = new byte[1];
        for (int i = 0; i < ms * 8; i++) {
            double angle = i / ((8000.0 * speed) / freq) * 2.0 * Math.PI;
            buf[0] = (byte) (Math.sin(angle) * volume);
            line.write(buf, 0, 1);
            Thread.sleep(1000 / (8000 * speed));
        }
    }

    public static void saveHistory(List<String> history) {
        try (PrintWriter writer = new PrintWriter("history.txt")) {
            for (String entry : history) {
                writer.println(entry);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static List<String> loadHistory() {
        List<String> history = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("history.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                history.add(line);
            }
        } catch (IOException e) {
            // File not found or error reading file, ignore and return empty list
        }
        return history;
    }

    public static void generateQRCode(String morseCode) {
        try {
            String qrText = "Morse Code: " + morseCode;
            int width = 300;
            int height = 300;
            String fileType = "png";

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrText, BarcodeFormat.QR_CODE, width, height);

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            File qrFile = new File("qrcode.png");
            javax.imageio.ImageIO.write(image, fileType, qrFile);
            JOptionPane.showMessageDialog(null, "QR code saved as: " + qrFile.getAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error generating QR code: " + e.getMessage());
        }
    }
}