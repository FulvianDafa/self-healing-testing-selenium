package com.fulvian.gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * GUI untuk penelitian self-healing Selenium WebDriver.
 *
 * Framing GUI:
 * - Bukan sebagai tools untuk membuat test dari nol.
 * - Dipakai ketika tester sudah punya script Selenium baseline yang bermasalah karena locator berubah.
 * - Tester memilih/upload script baseline rusak, lalu menekan tombol Aktifkan Self-Healing.
 * - Sistem menjalankan Maven test dengan flag self-healing dan menampilkan log similarity.
 */
public class SelfHealingGui extends JFrame {

    private final JTextField projectRootField;
    private final JButton chooseProjectRootButton;

    private final JTextField scriptPathField;
    private final JButton chooseScriptButton;
    private final JTextField testClassField;
    private final JCheckBox copyScriptCheckBox;

    private final JComboBox<String> sutComboBox;
    private final JTextField urlField;
    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JTextField thresholdField;
    private final JTextField logPathField;

    private final JTextArea outputArea;
    private final JButton activateHealingButton;
    private final JButton refreshLogButton;
    private final JButton clearOutputButton;

    private final JTable logTable;
    private final DefaultTableModel logTableModel;

    public SelfHealingGui() {
        // Fix #1: Perbaiki typo pada title window
        setTitle("Self-Healing Script Recovery Tool");
        setSize(1280, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        projectRootField = new JTextField(System.getProperty("user.dir"));
        chooseProjectRootButton = new JButton("Pilih Project");

        scriptPathField = new JTextField();
        scriptPathField.setEditable(false);
        // Fix #4: Ubah teks tombol upload agar lebih jelas
        chooseScriptButton = new JButton("Pilih Script Rusak");
        testClassField = new JTextField();
        // Fix #5: Sederhanakan teks checkbox
        copyScriptCheckBox = new JCheckBox("Salin script ke folder test project secara otomatis", true);

        sutComboBox = new JComboBox<>(new String[]{
                "Anugrah Jaya",
                "Arsip Dokumen",
                "Sapi Admin",
                "Sapi Client",
                "Custom"
        });

        // Fix #2: Default URL saat GUI pertama dibuka harus Anugrah Jaya
        urlField = new JTextField(resolveDefaultUrlBySut("Anugrah Jaya"));
        emailField = new JTextField("admin@example.com");
        passwordField = new JPasswordField("admin123");
        thresholdField = new JTextField("0.50");
        logPathField = new JTextField(resolveDefaultHealingLogPath("Anugrah Jaya"));

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 13));

        activateHealingButton = new JButton("Aktifkan Self-Healing");
        refreshLogButton = new JButton("Refresh Log");
        clearOutputButton = new JButton("Clear Output");

        // Fix #6 & #7: Kolom tabel lengkap dan lebar kolom diatur agar mudah di-screenshot
        String[] columns = {
                "Timestamp",
                "Test Case ID",
                "SUT",
                "Original Locator",
                "Mutated Locator",
                "Selected Element",
                "Similarity Score",
                "Threshold",
                "Healing Time (ms)",
                "Status",
                "Message"
        };

        logTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        logTable = new JTable(logTableModel);
        logTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        logTable.setFont(new Font("Consolas", Font.PLAIN, 12));
        logTable.setRowHeight(24);
        configureColumnWidth();

        buildLayout();
        bindActions();
    }

    private void buildLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Input Script Baseline Bermasalah"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        addRow(formPanel, gbc, 0, "Project Maven Root:", projectRootField, chooseProjectRootButton);
        addRow(formPanel, gbc, 1, "Script Baseline Rusak:", scriptPathField, chooseScriptButton);
        addRow(formPanel, gbc, 2, "Test Class:", testClassField, null);
        // Fix #3: Ubah label "Target SUT / Log:" menjadi "Target SUT:"
        addRow(formPanel, gbc, 3, "Target SUT:", sutComboBox, null);
        addRow(formPanel, gbc, 4, "Base URL:", urlField, null);
        addRow(formPanel, gbc, 5, "Admin Email:", emailField, null);
        addRow(formPanel, gbc, 6, "Admin Password:", passwordField, null);
        addRow(formPanel, gbc, 7, "Threshold Similarity:", thresholdField, null);
        addRow(formPanel, gbc, 8, "Path Log CSV:", logPathField, null);

        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        formPanel.add(copyScriptCheckBox, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(clearOutputButton);
        buttonPanel.add(refreshLogButton);
        // Fix #8: Tombol utama tetap "Aktifkan Self-Healing"
        buttonPanel.add(activateHealingButton);

        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 3;
        formPanel.add(buttonPanel, gbc);

        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createTitledBorder("Output Proses Self-Healing / Maven"));
        outputPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Hasil Self-Healing: Similarity Score, Threshold, dan Status"));
        logPanel.add(new JScrollPane(logTable), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, outputPanel, logPanel);
        splitPane.setResizeWeight(0.48);

        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field, JButton button) {
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);

        gbc.weightx = 1;
        gbc.gridx = 1;
        panel.add(field, gbc);

        gbc.weightx = 0;
        gbc.gridx = 2;
        if (button != null) {
            panel.add(button, gbc);
        } else {
            panel.add(new JLabel(""), gbc);
        }
    }

    private void bindActions() {
        chooseProjectRootButton.addActionListener(e -> chooseProjectRoot());
        chooseScriptButton.addActionListener(e -> chooseBaselineScript());
        activateHealingButton.addActionListener(e -> activateSelfHealing());
        refreshLogButton.addActionListener(e -> loadLogTable());
        clearOutputButton.addActionListener(e -> outputArea.setText(""));

        // Fix #2: Saat SUT berubah, URL dan path log ikut menyesuaikan
        sutComboBox.addActionListener(e -> {
            String sut = (String) sutComboBox.getSelectedItem();
            if (sut != null && !"Custom".equals(sut)) {
                logPathField.setText(resolveDefaultHealingLogPath(sut));
                urlField.setText(resolveDefaultUrlBySut(sut));
            }
        });
    }

    private void chooseProjectRoot() {
        JFileChooser chooser = new JFileChooser(projectRootField.getText().trim());
        chooser.setDialogTitle("Pilih Root Project Maven yang berisi pom.xml");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = chooser.getSelectedFile();
            projectRootField.setText(selectedDirectory.getAbsolutePath());
            appendOutput("Project Maven root dipilih: " + selectedDirectory.getAbsolutePath() + "\n");
        }
    }

    private void chooseBaselineScript() {
        JFileChooser chooser = new JFileChooser(projectRootField.getText().trim());
        chooser.setDialogTitle("Pilih Script Selenium Baseline yang Bermasalah (.java)");
        chooser.setFileFilter(new FileNameExtensionFilter("Java Test Script (*.java)", "java"));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();
        scriptPathField.setText(selectedFile.getAbsolutePath());

        String className = inferTestClassName(selectedFile.toPath());
        testClassField.setText(className);

        appendOutput("Script baseline dipilih: " + selectedFile.getAbsolutePath() + "\n");

        if (className.isBlank()) {
            appendOutput("Nama test class tidak terdeteksi otomatis. Isi field Test Class secara manual.\n");
        } else {
            appendOutput("Test class terdeteksi: " + className + "\n");
        }

        warnIfScriptOutsideProject(selectedFile.toPath());
    }

    private void activateSelfHealing() {
        activateHealingButton.setEnabled(false);
        logTableModel.setRowCount(0);

        String projectRoot = projectRootField.getText().trim();
        String scriptPath = scriptPathField.getText().trim();
        String testClass = testClassField.getText().trim();
        String url = urlField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String threshold = thresholdField.getText().trim();
        String sut = String.valueOf(sutComboBox.getSelectedItem());
        String logPath = logPathField.getText().trim();

        // Fix #9: Validasi ringan sebelum Maven dijalankan (tambahkan validasi Base URL)
        if (!validateInputs(projectRoot, scriptPath, testClass, threshold, url)) {
            activateHealingButton.setEnabled(true);
            return;
        }

        try {
            Path projectRootPath = Path.of(projectRoot);
            Path selectedScriptPath = Path.of(scriptPath);

            if (copyScriptCheckBox.isSelected() && !isInsideDirectory(selectedScriptPath, projectRootPath)) {
                Path copiedScript = copyScriptToMavenTestFolder(projectRootPath, selectedScriptPath);
                appendOutput("Script disalin ke project Maven: " + copiedScript + "\n");
            }

            List<String> command = buildMavenCommand(testClass, sut, url, email, password, threshold, scriptPath, logPath);

            // Fix #10: Output area menampilkan ringkasan lengkap sebelum Maven dijalankan
            appendOutput("\n====================================\n");
            appendOutput("Mengaktifkan self-healing untuk script baseline bermasalah\n");
            appendOutput("Project Root : " + projectRoot + "\n");
            appendOutput("Script       : " + scriptPath + "\n");
            appendOutput("Test Class   : " + testClass + "\n");
            appendOutput("SUT          : " + sut + "\n");
            appendOutput("Base URL     : " + url + "\n");
            appendOutput("Threshold    : " + threshold + "\n");
            appendOutput("Log CSV      : " + logPath + "\n");
            appendOutput("Command      : " + String.join(" ", command) + "\n");
            appendOutput("====================================\n\n");

            new Thread(() -> executeCommand(command, new File(projectRoot))).start();

        } catch (Exception e) {
            appendOutput("Gagal menyiapkan proses self-healing:\n");
            appendOutput(e.getMessage() + "\n");
            activateHealingButton.setEnabled(true);
        }
    }

    // Fix #9: Tambahkan parameter url ke validasi dan validasi Base URL tidak boleh kosong
    private boolean validateInputs(String projectRoot, String scriptPath, String testClass, String threshold, String url) {
        if (projectRoot.isBlank()) {
            appendOutput("[VALIDASI] Project Maven root belum dipilih.\n");
            return false;
        }

        Path pomPath = Path.of(projectRoot, "pom.xml");
        if (!Files.exists(pomPath)) {
            appendOutput("[VALIDASI] pom.xml tidak ditemukan di project root: " + projectRoot + "\n");
            appendOutput("[VALIDASI] Pastikan memilih folder root project Maven.\n");
            return false;
        }

        if (scriptPath.isBlank()) {
            appendOutput("[VALIDASI] Script baseline bermasalah belum dipilih.\n");
            return false;
        }

        if (!Files.exists(Path.of(scriptPath))) {
            appendOutput("[VALIDASI] File script tidak ditemukan: " + scriptPath + "\n");
            return false;
        }

        if (testClass.isBlank()) {
            appendOutput("[VALIDASI] Test Class belum diisi. Isi manual, contoh: TestAnugrahJayaHealing\n");
            return false;
        }

        if (url.isBlank()) {
            appendOutput("[VALIDASI] Base URL tidak boleh kosong.\n");
            return false;
        }

        try {
            double parsedThreshold = Double.parseDouble(threshold);
            if (parsedThreshold < 0 || parsedThreshold > 1) {
                appendOutput("[VALIDASI] Threshold harus berada pada rentang 0 sampai 1. Contoh: 0.50\n");
                return false;
            }
        } catch (NumberFormatException e) {
            appendOutput("[VALIDASI] Threshold tidak valid. Gunakan angka desimal, contoh: 0.50\n");
            return false;
        }

        return true;
    }

    // Fix #12: Command Maven membawa semua parameter yang diperlukan
    private List<String> buildMavenCommand(
            String testClass,
            String sut,
            String url,
            String email,
            String password,
            String threshold,
            String scriptPath,
            String logPath
    ) {
        List<String> command = new ArrayList<>();
        command.add(getMavenCommand());
        command.add("clean");
        command.add("test");
        command.add("-Dtest=" + testClass);

        command.add("-DselfHealing.enabled=true");
        command.add("-Dhealing.threshold=" + threshold);
        command.add("-DuploadedScript=" + scriptPath);
        command.add("-Dhealing.log.file=" + logPath);
        command.add("-DsutName=" + sut);

        if ("Sapi Admin".equals(sut)) {
            command.add("-DadminUrl=" + url);
        } else {
            command.add("-DbaseUrl=" + url);
        }

        command.add("-DadminEmail=" + email);
        command.add("-DadminPassword=" + password);

        return command;
    }

    private void executeCommand(List<String> command, File workingDirectory) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.directory(workingDirectory);
            builder.redirectErrorStream(true);

            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    appendOutput(line + "\n");
                }
            }

            int exitCode = process.waitFor();

            appendOutput("\n====================================\n");
            appendOutput("Process selesai dengan exit code: " + exitCode + "\n");

            if (exitCode == 0) {
                appendOutput("Status: SELF-HEALING EXECUTION FINISHED / BUILD SUCCESS\n");
            } else {
                appendOutput("Status: BUILD FAILURE / TEST FAILED\n");
            }

            SwingUtilities.invokeLater(this::loadLogTable);

        } catch (Exception e) {
            appendOutput("Terjadi error saat menjalankan Maven test:\n");
            appendOutput(e.getMessage() + "\n");
        } finally {
            SwingUtilities.invokeLater(() -> activateHealingButton.setEnabled(true));
        }
    }

    private void loadLogTable() {
        logTableModel.setRowCount(0);

        String rawLogPath = logPathField.getText().trim();
        if (rawLogPath.isBlank()) {
            appendOutput("Path log CSV masih kosong.\n");
            return;
        }

        Path logPath = resolvePathAgainstProjectRoot(rawLogPath);

        if (!Files.exists(logPath)) {
            appendOutput("File log belum ada: " + logPath + "\n");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(logPath, StandardCharsets.UTF_8);

            boolean skipHeader = true;
            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    continue;
                }

                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                String[] values = parseCsvLine(line);
                Object[] row = normalizeLogRow(values);
                logTableModel.addRow(row);
            }

            appendOutput("\nLog berhasil dimuat dari: " + logPath + "\n");

        } catch (Exception e) {
            appendOutput("Gagal membaca log CSV:\n");
            appendOutput(e.getMessage() + "\n");
        }
    }

    /**
     * Mendukung tiga format CSV:
     *
     * Format HealingResult (12 kolom, dari AutoHealingWebDriver / HealingDriver):
     * timestamp, test_case_id, scenario_type, original_locator,
     * status, text_score, locator_score, position_score, combined_score,
     * selected_element, healing_time_ms, is_false_positive
     *
     * Format GUI lama 11 kolom:
     * timestamp, test_case_id, sut, original_locator, mutated_locator,
     * selected_element, similarity_score, threshold, healing_time_ms, status, message
     *
     * Format GUI lama 10 kolom (tanpa threshold):
     * timestamp, test_case_id, sut, original_locator, mutated_locator,
     * selected_element, similarity_score, healing_time_ms, status, message
     */
    private Object[] normalizeLogRow(String[] values) {
        Object[] row = new Object[11];
        String sut = String.valueOf(sutComboBox.getSelectedItem());
        String guiThreshold = thresholdField.getText().trim();

        // Deteksi format HealingResult (12 kolom) dari HealingDriver/AutoHealingWebDriver
        if (values.length >= 12 && isHealingResultFormat(values)) {
            row[0]  = getValue(values, 0);  // Timestamp
            row[1]  = getValue(values, 1);  // Test Case ID
            row[2]  = sut;                  // SUT (dari combobox GUI)
            row[3]  = getValue(values, 3);  // Original Locator
            row[4]  = "-";                  // Mutated Locator (tidak ada di format ini)
            row[5]  = getValue(values, 9);  // Selected Element
            row[6]  = getValue(values, 8);  // Similarity Score (combined_score)
            row[7]  = guiThreshold;          // Threshold (dari field GUI)
            row[8]  = getValue(values, 10); // Healing Time (ms)
            row[9]  = getValue(values, 4);  // Status
            row[10] = String.format("scenario_type=%s, text_score=%s, locator_score=%s, position_score=%s, false_positive=%s",
                    getValue(values, 2), getValue(values, 5), getValue(values, 6),
                    getValue(values, 7), getValue(values, 11));
            return row;
        }

        // Format GUI lama 11 kolom (sudah sesuai urutan tabel)
        if (values.length >= 11) {
            for (int i = 0; i < row.length; i++) {
                row[i] = i < values.length ? values[i] : "";
            }
            return row;
        }

        // Backward compatible untuk log lama 10 kolom (tanpa Threshold)
        row[0] = getValue(values, 0); // Timestamp
        row[1] = getValue(values, 1); // Test Case ID
        row[2] = getValue(values, 2); // SUT
        row[3] = getValue(values, 3); // Original Locator
        row[4] = getValue(values, 4); // Mutated Locator
        row[5] = getValue(values, 5); // Selected Element
        row[6] = getValue(values, 6); // Similarity Score
        row[7] = guiThreshold;         // Threshold fallback dari GUI
        row[8] = getValue(values, 7); // Healing Time
        row[9] = getValue(values, 8); // Status
        row[10] = getValue(values, 9); // Message

        return row;
    }

    private boolean isHealingResultFormat(String[] values) {
        if (values.length < 12) return false;

        String status = getValue(values, 4).trim().toUpperCase();
        if (!"SUCCESS".equals(status) && !"FAIL".equals(status)) {
            return false;
        }

        try {
            Double.parseDouble(getValue(values, 5).trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String getValue(String[] values, int index) {
        return index < values.length ? values[index] : "";
    }

    private String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (insideQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (c == ',' && !insideQuotes) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        values.add(current.toString().trim());
        return values.toArray(new String[0]);
    }

    private String inferTestClassName(Path scriptPath) {
        try {
            List<String> lines = Files.readAllLines(scriptPath, StandardCharsets.UTF_8);

            for (String line : lines) {
                String normalized = line.trim();

                if (normalized.startsWith("public class ") || normalized.startsWith("class ")) {
                    String[] parts = normalized.split("\\s+");
                    for (int i = 0; i < parts.length; i++) {
                        if ("class".equals(parts[i]) && i + 1 < parts.length) {
                            return parts[i + 1]
                                    .replace("{", "")
                                    .replace("extends", "")
                                    .trim();
                        }
                    }
                }
            }
        } catch (IOException e) {
            appendOutput("Gagal membaca script untuk mendeteksi nama class: " + e.getMessage() + "\n");
        }

        return "";
    }

    private void warnIfScriptOutsideProject(Path scriptPath) {
        Path projectRootPath = Path.of(projectRootField.getText().trim());

        if (!isInsideDirectory(scriptPath, projectRootPath)) {
            appendOutput("Catatan: script berada di luar project Maven.\n");
            appendOutput("Jika checkbox salin aktif, GUI akan menyalin script ke src/test/java/com/fulvian/tests sebelum Maven dijalankan.\n");
        }
    }

    private boolean isInsideDirectory(Path filePath, Path directoryPath) {
        try {
            Path normalizedFile = filePath.toAbsolutePath().normalize();
            Path normalizedDirectory = directoryPath.toAbsolutePath().normalize();
            return normalizedFile.startsWith(normalizedDirectory);
        } catch (Exception e) {
            return false;
        }
    }

    private Path copyScriptToMavenTestFolder(Path projectRoot, Path selectedScript) throws IOException {
        Path targetDirectory = projectRoot.resolve(Path.of("src", "test", "java", "com", "fulvian", "tests"));
        Files.createDirectories(targetDirectory);

        Path targetFile = targetDirectory.resolve(selectedScript.getFileName());
        Files.copy(selectedScript, targetFile, StandardCopyOption.REPLACE_EXISTING);
        return targetFile;
    }

    private Path resolvePathAgainstProjectRoot(String rawPath) {
        Path path = Path.of(rawPath);
        if (path.isAbsolute()) {
            return path;
        }
        return Path.of(projectRootField.getText().trim()).resolve(path).normalize();
    }

    private String resolveDefaultHealingLogPath(String sut) {
        return switch (sut) {
            case "Anugrah Jaya"  -> "results/healing_log.csv";
            case "Arsip Dokumen" -> "results/arsip_healing_log.csv";
            case "Sapi Admin"    -> "results/sapi_admin_healing_log.csv";
            case "Sapi Client"   -> "results/sapi_client_healing_log.csv";
            default              -> "results/healing_log.csv";
        };
    }

    // Fix #2: Resolusi default URL per SUT — Anugrah Jaya menggunakan URL yang benar
    private String resolveDefaultUrlBySut(String sut) {
        return switch (sut) {
            case "Anugrah Jaya"  -> "http://anugrah_jaya.test/app/index.html";
            case "Arsip Dokumen" -> "http://localhost:8000";
            case "Sapi Admin"    -> "http://localhost:8000";
            case "Sapi Client"   -> "http://localhost:5173";
            default              -> "";
        };
    }

    private String getMavenCommand() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win") ? "mvn.cmd" : "mvn";
    }

    private void appendOutput(String text) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append(text);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }

    // Fix #7: Lebar kolom diatur agar kolom penting (Status, Threshold, Score) mudah terlihat saat screenshot
    private void configureColumnWidth() {
        TableColumnModel columns = logTable.getColumnModel();
        // Timestamp | Test Case ID | SUT | Original Locator | Mutated Locator | Selected Element | Score | Threshold | Time | Status | Message
        int[] widths = {160, 130, 120, 220, 160, 280, 130, 100, 140, 120, 400};

        for (int i = 0; i < widths.length; i++) {
            columns.getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SelfHealingGui gui = new SelfHealingGui();
            gui.setVisible(true);
        });
    }
}
