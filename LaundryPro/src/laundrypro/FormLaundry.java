package laundrypro;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.*;
import com.formdev.flatlaf.FlatClientProperties;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Genzyy
 */
public class FormLaundry extends JFrame {

    // --- Komponen Global ---
    JTextField txtNama, txtBerat, txtCari;
    JComboBox<String> cmbLayanan, cmbStatus;
    JTable tabelData;
    DefaultTableModel model;
    
    // Tombol-tombol
    JButton btnSimpan, btnEdit, btnHapus, btnReset, btnCetak, btnLogout, btnAmbil, btnCetakNota;
    
    // Variabel Bantu
    String idSelected = "";
    private JLabel lblTotalTransaksi, lblTotalPendapatan, lblSedangProses;
    private JProgressBar progressBar;
    private Timer statsTimer;

    public FormLaundry() {
        initComponents();
        loadData(""); 
        loadDashboardStats();
        startStatsUpdateTimer();
    }

    private void initComponents() {
        // 1. Setup Frame
        setTitle("Laundry Pro - Dashboard Admin");
        setSize(1366, 768);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Set background color
        getContentPane().setBackground(new Color(248, 250, 252));

        // --- 2. HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(1000, 70));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(0, 30, 0, 30)
        ));

        // Logo dan Judul
        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerLeft.setOpaque(false);
        
        JLabel iconLabel = new JLabel("LAUNDRY PRO");
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        iconLabel.setForeground(new Color(37, 99, 235));
        
        headerLeft.add(iconLabel);

        // User Info dan Logout
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setOpaque(false);
        
        JLabel lblUser = new JLabel("Admin");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUser.setForeground(new Color(71, 85, 105));
        
        JLabel lblTime = new JLabel();
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTime.setForeground(new Color(148, 163, 184));
        updateTime(lblTime);
        
        btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(new Color(239, 68, 68));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setFocusPainted(false);
        btnLogout.putClientProperty(FlatClientProperties.STYLE, 
            "arc: 6; " +
            "borderWidth: 0; " +
            "focusWidth: 0"
        );
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Yakin ingin logout?", "Konfirmasi Logout", 
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                new FormLogin().setVisible(true);
                this.dispose();
            }
        });

        headerRight.add(lblUser);
        headerRight.add(Box.createHorizontalStrut(10));
        headerRight.add(new JSeparator(SwingConstants.VERTICAL) {{
            setPreferredSize(new Dimension(1, 20));
        }});
        headerRight.add(Box.createHorizontalStrut(10));
        headerRight.add(lblTime);
        headerRight.add(Box.createHorizontalStrut(20));
        headerRight.add(btnLogout);

        header.add(headerLeft, BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);

        // --- 3. DASHBOARD STATS ---
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        statsPanel.setOpaque(false);
        
        // Stat 1: Total Transaksi Hari Ini
        JPanel stat1 = createStatCard("Total Transaksi", "0", new Color(59, 130, 246));
        lblTotalTransaksi = (JLabel) ((BorderLayout) stat1.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        
        // Stat 2: Total Pendapatan
        JPanel stat2 = createStatCard("Total Pendapatan", "Rp 0", new Color(34, 197, 94));
        lblTotalPendapatan = (JLabel) ((BorderLayout) stat2.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        
        // Stat 3: Sedang Proses
        JPanel stat3 = createStatCard("Sedang Diproses", "0", new Color(245, 158, 11));
        lblSedangProses = (JLabel) ((BorderLayout) stat3.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        
        statsPanel.add(stat1);
        statsPanel.add(stat2);
        statsPanel.add(stat3);

        // --- 4. MAIN CONTENT ---
        JPanel mainContent = new JPanel(new BorderLayout(20, 0));
        mainContent.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));
        mainContent.setOpaque(false);

        // PANEL KIRI (INPUT FORM) - 35% width
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setOpaque(false);
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        // Judul Form
        JLabel formTitle = new JLabel("Tambah Transaksi Baru");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(new Color(30, 41, 59));
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(formTitle);
        
        formPanel.add(Box.createVerticalStrut(8));
        
        JLabel formSubtitle = new JLabel("Isi data pelanggan di bawah");
        formSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formSubtitle.setForeground(new Color(100, 116, 139));
        formSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(formSubtitle);
        
        formPanel.add(Box.createVerticalStrut(30));

        // Input Fields
        formPanel.add(createInputField("Nama Pelanggan *", txtNama = new JTextField()));
        formPanel.add(Box.createVerticalStrut(20));
        
        formPanel.add(createComboBoxField("Jenis Layanan *", 
            cmbLayanan = new JComboBox<>(new String[]{
                "Cuci Komplit (Rp 6.000/kg)", 
                "Cuci Kering (Rp 4.000/kg)", 
                "Setrika Saja (Rp 3.000/kg)"
            })));
        formPanel.add(Box.createVerticalStrut(20));
        
        formPanel.add(createInputField("Berat (Kg) *", txtBerat = new JTextField("0")));
        formPanel.add(Box.createVerticalStrut(20));
        
        formPanel.add(createComboBoxField("Status *", 
            cmbStatus = new JComboBox<>(new String[]{
                "Proses", 
                "Selesai", 
                "Sudah Diambil"
            })));
        cmbStatus.setSelectedItem("Proses");
        cmbStatus.setEnabled(false);


        
        // Info Total Harga
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        totalPanel.setOpaque(false);
        totalPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel totalLabel = new JLabel("Total Harga: ");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalLabel.setForeground(new Color(30, 41, 59));
        
        JLabel totalValue = new JLabel("Rp 0");
        totalValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalValue.setForeground(new Color(34, 197, 94));
        
        // Update total saat berat atau layanan berubah
        ActionListener updateTotal = e -> {
            try {
                int total = hitungHarga();
                totalValue.setText("Rp " + String.format("%,d", total));
            } catch (Exception ex) {
                totalValue.setText("Rp 0");
            }
        };
        
        txtBerat.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                updateTotal.actionPerformed(null);
            }
        });
        
        cmbLayanan.addActionListener(updateTotal);
        
        totalPanel.add(totalLabel);
        totalPanel.add(Box.createHorizontalStrut(10));
        totalPanel.add(totalValue);
        
        formPanel.add(Box.createVerticalStrut(25));
        formPanel.add(totalPanel);
        formPanel.add(Box.createVerticalStrut(30));

        // Panel Tombol tanpa emoji
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 12, 12));
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        buttonPanel.setOpaque(false);
        
        btnSimpan = createStyledButton("SIMPAN", new Color(34, 197, 94));
        btnEdit = createStyledButton("EDIT", new Color(59, 130, 246));
        btnHapus = createStyledButton("HAPUS", new Color(239, 68, 68));
        btnReset = createStyledButton("RESET", new Color(148, 163, 184));
        
        btnEdit.setEnabled(false);
        btnHapus.setEnabled(false);
        
        btnSimpan.addActionListener(e -> aksiSimpan());
        btnEdit.addActionListener(e -> aksiEdit());
        btnHapus.addActionListener(e -> aksiHapus());
        btnReset.addActionListener(e -> resetForm());
        
        btnCetakNota = createStyledButton("CETAK NOTA", new Color(139, 92, 246));
        btnCetakNota.setEnabled(false);
        btnCetakNota.addActionListener(e -> aksiCetakNota());

        buttonPanel.add(btnCetakNota);
        
        btnAmbil = createStyledButton("AMBIL LAUNDRY", new Color(16, 185, 129));
        btnAmbil.setEnabled(false);
        btnAmbil.addActionListener(e -> aksiAmbilLaundry());

        
        buttonPanel.add(btnSimpan);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnHapus);
        buttonPanel.add(btnReset);
        buttonPanel.add(btnAmbil);

        formPanel.add(buttonPanel);
        
        formContainer.add(formPanel, BorderLayout.CENTER);

        // PANEL KANAN (DATA TABLE) - 65% width
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        // Search Panel tanpa emoji
        JPanel searchPanel = new JPanel(new BorderLayout(15, 0));
        searchPanel.setOpaque(false);
        
        JLabel searchLabel = new JLabel("Cari Data:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchLabel.setForeground(new Color(71, 85, 105));
        
        txtCari = new JTextField();
        txtCari.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Cari berdasarkan nama pelanggan...");
        txtCari.putClientProperty(FlatClientProperties.STYLE, 
            "arc: 8; " +
            "borderWidth: 1; " +
            "borderColor: #E2E8F0; " +
            "focusedBorderColor: #3B82F6; " +
            "margin: 8,12,8,12"
        );
        txtCari.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                loadData(txtCari.getText());
            }
        });
        
        btnCetak = createStyledButton("CETAK LAPORAN", new Color(139, 92, 246));
        btnCetak.addActionListener(e -> cetak());
        
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(txtCari, BorderLayout.CENTER);
        searchPanel.add(btnCetak, BorderLayout.EAST);

        // Table
        String[] col = {"No", "Pelanggan", "Layanan", "Berat", "Total", "Status", "Tanggal"};
        model = new DefaultTableModel(col, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };
        
        tabelData = new JTable(model);
        tabelData.setRowHeight(45);
        tabelData.setShowGrid(false);
        tabelData.setIntercellSpacing(new Dimension(0, 0));
        tabelData.setSelectionBackground(new Color(219, 234, 254));
        tabelData.setSelectionForeground(new Color(30, 41, 59));
        
        // Header styling
        JTableHeader headerTable = tabelData.getTableHeader();
        headerTable.setFont(new Font("Segoe UI", Font.BOLD, 13));
        headerTable.setBackground(new Color(248, 250, 252));
        headerTable.setForeground(new Color(71, 85, 105));
        headerTable.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(226, 232, 240)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        headerTable.setPreferredSize(new Dimension(headerTable.getWidth(), 50));
        
        tabelData.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabelData.setForeground(new Color(71, 85, 105));
        
        // Center align untuk kolom tertentu
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        
        // Set renderer per kolom
        tabelData.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tabelData.getColumnModel().getColumn(0).setPreferredWidth(60);
        
        tabelData.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
        tabelData.getColumnModel().getColumn(1).setPreferredWidth(180);
        
        tabelData.getColumnModel().getColumn(2).setCellRenderer(leftRenderer);
        tabelData.getColumnModel().getColumn(2).setPreferredWidth(150);
        
        tabelData.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tabelData.getColumnModel().getColumn(3).setPreferredWidth(80);
        
        tabelData.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tabelData.getColumnModel().getColumn(4).setPreferredWidth(120);
        
        tabelData.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());
        tabelData.getColumnModel().getColumn(5).setPreferredWidth(120);
        
        tabelData.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        tabelData.getColumnModel().getColumn(6).setPreferredWidth(150);
        
        JScrollPane scroll = new JScrollPane(tabelData);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        
        tabelData.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    pilihDataTabel();
         
                }
            }
        });

        // Progress Bar
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setForeground(new Color(59, 130, 246));
        progressBar.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(scroll, BorderLayout.CENTER);
        tablePanel.add(progressBar, BorderLayout.SOUTH);
        
        tableContainer.add(tablePanel, BorderLayout.CENTER);

        // Set width ratio 35:65
        mainContent.add(formContainer, BorderLayout.WEST);
        mainContent.add(tableContainer, BorderLayout.CENTER);

        // --- 5. ASSEMBLE EVERYTHING ---
        add(header, BorderLayout.NORTH);
        add(statsPanel, BorderLayout.CENTER);
        add(mainContent, BorderLayout.SOUTH);
    }

    // ================== HELPER METHODS ==================

    private JPanel createStatCard(String title, String initialValue, Color color) {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(100, 116, 139));
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        
        // Value dengan font yang lebih besar
        JLabel valueLabel = new JLabel(initialValue);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }

    private JPanel createInputField(String label, JTextField textField) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(71, 85, 105));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.putClientProperty(FlatClientProperties.STYLE, 
            "arc: 8; " +
            "borderWidth: 1; " +
            "borderColor: #E2E8F0; " +
            "focusedBorderColor: #3B82F6; " +
            "margin: 8,12,8,12"
        );
        
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(8));
        panel.add(textField);
        
        return panel;
    }

    private JPanel createComboBoxField(String label, JComboBox<String> comboBox) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(71, 85, 105));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBackground(Color.WHITE);
        comboBox.putClientProperty(FlatClientProperties.STYLE, 
            "arc: 8; " +
            "borderWidth: 1; " +
            "borderColor: #E2E8F0; " +
            "focusedBorderColor: #3B82F6"
        );
        
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(8));
        panel.add(comboBox);
        
        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.putClientProperty(FlatClientProperties.STYLE, 
            "arc: 8; " +
            "borderWidth: 0; " +
            "focusWidth: 0"
        );
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                if(button.isEnabled()) {
                    button.setBackground(color.darker());
                }
            }
            public void mouseExited(MouseEvent e) { 
                if(button.isEnabled()) {
                    button.setBackground(color);
                }
            }
        });
        
        return button;
    }

    private void updateTime(JLabel label) {
        Timer timer = new Timer(1000, e -> {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            label.setText(time);
        });
        timer.start();
    }

    private void startStatsUpdateTimer() {
        statsTimer = new Timer(30000, e -> loadDashboardStats());
        statsTimer.start();
    }

    // Custom Renderer untuk Status tanpa emoji
    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            
            String status = value != null ? value.toString() : "";
            if (status.contains("Proses")) {
                label.setBackground(new Color(254, 249, 231));
                label.setForeground(new Color(245, 158, 11));
                label.setText(status);
            } else if (status.contains("Selesai")) {
                label.setBackground(new Color(236, 253, 243));
                label.setForeground(new Color(34, 197, 94));
                label.setText(status);
            } else if (status.contains("Diambil")) {
                label.setBackground(new Color(239, 246, 255));
                label.setForeground(new Color(59, 130, 246));
                label.setText(status);
            } else {
                label.setText(status);
            }
            
            if (isSelected) {
                label.setBackground(new Color(59, 130, 246));
                label.setForeground(Color.WHITE);
            }
            
            return label;
        }
    }

    // ================== BUSINESS LOGIC ==================

    private void loadDashboardStats() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private int totalTransaksi = 0;
            private int totalPendapatan = 0;
            private int sedangProses = 0;
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    Connection c = Koneksi.getKoneksi();
                    
                    // Total Transaksi Hari Ini
                    String sql1 = "SELECT COUNT(*) FROM transaksi WHERE DATE(tanggal) = CURDATE()";
                    PreparedStatement p1 = c.prepareStatement(sql1);
                    ResultSet r1 = p1.executeQuery();
                    if (r1.next()) {
                        totalTransaksi = r1.getInt(1);
                    }
                    
                    // Total Pendapatan Hari Ini
                    String sql2 = "SELECT COALESCE(SUM(total_harga), 0) FROM transaksi WHERE DATE(tanggal) = CURDATE()";
                    PreparedStatement p2 = c.prepareStatement(sql2);
                    ResultSet r2 = p2.executeQuery();
                    if (r2.next()) {
                        totalPendapatan = r2.getInt(1);
                    }
                    
                    // Sedang Diproses
                    String sql3 = "SELECT COUNT(*) FROM transaksi WHERE status LIKE '%Proses%'";
                    PreparedStatement p3 = c.prepareStatement(sql3);
                    ResultSet r3 = p3.executeQuery();
                    if (r3.next()) {
                        sedangProses = r3.getInt(1);
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void done() {
                if (lblTotalTransaksi != null) {
                    lblTotalTransaksi.setText(String.valueOf(totalTransaksi));
                }
                if (lblTotalPendapatan != null) {
                    lblTotalPendapatan.setText("Rp " + String.format("%,d", totalPendapatan));
                }
                if (lblSedangProses != null) {
                    lblSedangProses.setText(String.valueOf(sedangProses));
                }
            }
        };
        worker.execute();
    }

    private void loadData(String keyword) {
        progressBar.setVisible(true);
        model.setRowCount(0);
        
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    Thread.sleep(300);
                    
                    Connection c = Koneksi.getKoneksi();
                    String sql = "SELECT * FROM transaksi WHERE nama_pelanggan LIKE ? ORDER BY id DESC";
                    PreparedStatement p = c.prepareStatement(sql);
                    p.setString(1, "%" + keyword + "%");
                    
                    ResultSet r = p.executeQuery();
                    int rowNum = 1;
                    while (r.next()) {
                        model.addRow(new Object[]{
                            String.valueOf(rowNum++),
                            r.getString("nama_pelanggan"),
                            r.getString("jenis_layanan"),
                            r.getString("berat") + " Kg",
                            "Rp " + String.format("%,d", r.getInt("total_harga")),
                            r.getString("status"),
                            r.getString("tanggal")
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Error Load: " + e.getMessage());
                }
                return null;
            }
            
            @Override
            protected void done() {
                progressBar.setVisible(false);
            }
        };
        worker.execute();
    }

    private void aksiSimpan() {
        if (txtNama.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama pelanggan harus diisi!", "Peringatan", 
                JOptionPane.WARNING_MESSAGE);
            txtNama.requestFocus();
            return;
        }
        
        try {
            int berat = Integer.parseInt(txtBerat.getText());
            if (berat <= 0) {
                JOptionPane.showMessageDialog(this, "Berat harus lebih dari 0!", "Peringatan", 
                    JOptionPane.WARNING_MESSAGE);
                txtBerat.requestFocus();
                return;
            }
            
            int total = hitungHarga();
            Connection c = Koneksi.getKoneksi();
            String sql = "INSERT INTO transaksi (nama_pelanggan, jenis_layanan, berat, total_harga, status) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement p = c.prepareStatement(sql);
            p.setString(1, txtNama.getText());
            p.setString(2, cmbLayanan.getSelectedItem().toString());
            p.setInt(3, berat);
            p.setInt(4, total);
            p.setString(5, "Proses");
            
            p.executeUpdate();
            JOptionPane.showMessageDialog(this, 
                "Data berhasil disimpan!\nTotal: Rp " + String.format("%,d", total), 
                "Sukses", JOptionPane.INFORMATION_MESSAGE);
            
            resetForm();
            loadData("");
            loadDashboardStats();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Berat harus berupa angka!", "Error", 
                JOptionPane.ERROR_MESSAGE);
            txtBerat.requestFocus();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aksiEdit() {
        if(idSelected.isEmpty()) return;
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Yakin ingin mengupdate data ini?", "Konfirmasi Update", 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if(confirm != JOptionPane.YES_OPTION) return;
        
        try {
            int berat = Integer.parseInt(txtBerat.getText());
            if (berat <= 0) {
                JOptionPane.showMessageDialog(this, "Berat harus lebih dari 0!", "Peringatan", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int total = hitungHarga();
            Connection c = Koneksi.getKoneksi();
            String sql = "UPDATE transaksi SET nama_pelanggan=?, jenis_layanan=?, berat=?, total_harga=?, status=? WHERE id=?";
            PreparedStatement p = c.prepareStatement(sql);
            p.setString(1, txtNama.getText());
            p.setString(2, cmbLayanan.getSelectedItem().toString());
            p.setInt(3, berat);
            p.setInt(4, total);
            p.setString(5, cmbStatus.getSelectedItem().toString());
            p.setString(6, idSelected);
            
            p.executeUpdate();
            JOptionPane.showMessageDialog(this, 
                "Data berhasil diupdate!\nTotal: Rp " + String.format("%,d", total), 
                "Sukses", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
            loadData("");
            loadDashboardStats();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mengupdate data: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aksiHapus() {
        if(idSelected.isEmpty()) return;
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Yakin ingin menghapus data ini?\nTindakan ini tidak dapat dibatalkan.", 
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if(confirm != JOptionPane.YES_OPTION) return;
        
        try {
            Connection c = Koneksi.getKoneksi();
            String sql = "DELETE FROM transaksi WHERE id=?";
            PreparedStatement p = c.prepareStatement(sql);
            p.setString(1, idSelected);
            p.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Data berhasil dihapus!", 
                "Sukses", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
            loadData("");
            loadDashboardStats();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus data: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pilihDataTabel() {
        int row = tabelData.getSelectedRow();
        if(row != -1) {
            String namaPelanggan = model.getValueAt(row, 1).toString();
            try {
                Connection c = Koneksi.getKoneksi();
                String sql = "SELECT id FROM transaksi WHERE nama_pelanggan = ? ORDER BY id DESC LIMIT 1";
                PreparedStatement p = c.prepareStatement(sql);
                p.setString(1, namaPelanggan);
                ResultSet r = p.executeQuery();
                if (r.next()) {
                    idSelected = r.getString("id");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            txtNama.setText(namaPelanggan);
            
            String lyn = model.getValueAt(row, 2).toString();
            cmbLayanan.setSelectedItem(lyn);
            
            String brt = model.getValueAt(row, 3).toString().replace(" Kg", "");
            txtBerat.setText(brt);
            
            String sts = model.getValueAt(row, 5).toString();
            cmbStatus.setSelectedItem(sts);
            
           btnSimpan.setEnabled(false);
                btnEdit.setEnabled(false);
                btnHapus.setEnabled(false);
                btnAmbil.setEnabled(false);
                btnCetakNota.setEnabled(false);

                if (sts.equalsIgnoreCase("Proses")) {
                    btnEdit.setEnabled(true);
                    btnHapus.setEnabled(true);
                    btnCetakNota.setEnabled(true); // 🔥 CETAK AKTIF
                }
                else if (sts.equalsIgnoreCase("Selesai")) {
                    btnAmbil.setEnabled(true);     // 🔥 AMBIL AKTIF
                }

        }
    }

    private void resetForm() {
        txtNama.setText("");
        txtBerat.setText("0");
        cmbLayanan.setSelectedIndex(0);
        cmbStatus.setSelectedItem("Proses");
        cmbStatus.setEnabled(false);
        idSelected = "";
        
        btnSimpan.setEnabled(true);
        btnEdit.setEnabled(false);
        btnHapus.setEnabled(false);
        tabelData.clearSelection();
        txtCari.setText("");
        btnCetakNota.setEnabled(false);
        btnAmbil.setEnabled(false);
    }
    
    private int hitungHarga() {
        int harga = 6000;
        if(cmbLayanan.getSelectedIndex() == 1) harga = 4000;
        if(cmbLayanan.getSelectedIndex() == 2) harga = 3000;
        return Integer.parseInt(txtBerat.getText()) * harga;
    }

    private void cetak() {
        try {
            MessageFormat header = new MessageFormat("LAPORAN TRANSAKSI LAUNDRY PRO");
            MessageFormat footer = new MessageFormat("Halaman {0,number,integer} - Dicetak pada: " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            tabelData.print(JTable.PrintMode.FIT_WIDTH, header, footer);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak laporan: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    @Override
    public void dispose() {
        if (statsTimer != null) {
            statsTimer.stop();
        }
        super.dispose();
    }
    
    private void cetakNota(String nama, String layanan, int berat, int total, String status) {
    JTextArea nota = new JTextArea();
    nota.setEditable(false);
    nota.setFont(new Font("Monospaced", Font.PLAIN, 12));

    nota.setText(
        "====================================\n" +
        "           LAUNDRY PRO\n" +
        "   Sistem Manajemen Laundry Modern\n" +
        "====================================\n\n" +
        "Tanggal  : " + LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n" +
        "------------------------------------\n" +
        "Nama     : " + nama + "\n" +
        "Layanan  : " + layanan + "\n" +
        "Berat    : " + berat + " Kg\n" +
        "------------------------------------\n" +
        "TOTAL    : Rp " + String.format("%,d", total) + "\n" +
        "STATUS   : " + status + "\n" +
        "------------------------------------\n" +
        "Terima kasih telah menggunakan\n" +
        "LAYANAN LAUNDRY PRO\n" +
        "====================================\n"
    );

    try {
        nota.print();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
            "Gagal mencetak nota: " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}
private void updateStatusOtomatis(String id, String statusBaru) {
    try {
        Connection c = Koneksi.getKoneksi();
        String sql = "UPDATE transaksi SET status=? WHERE id=?";
        PreparedStatement p = c.prepareStatement(sql);
        p.setString(1, statusBaru);
        p.setString(2, id);
        p.executeUpdate();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
            "Gagal update status otomatis: " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
  }
    private String getLastInsertedId() {
    try {
        Connection c = Koneksi.getKoneksi();
        Statement s = c.createStatement();
        ResultSet r = s.executeQuery(
            "SELECT id FROM transaksi ORDER BY id DESC LIMIT 1"
        );
        if (r.next()) {
            return r.getString("id");
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return "";
}
    private void aksiAmbilLaundry() {
    if (idSelected.isEmpty()) return;

    int confirm = JOptionPane.showConfirmDialog(this,
        "Laundry sudah diambil pelanggan?",
        "Konfirmasi Pengambilan",
        JOptionPane.YES_NO_OPTION);

    if (confirm != JOptionPane.YES_OPTION) return;

    try {
        Connection c = Koneksi.getKoneksi();
        String sql = "UPDATE transaksi SET status=?, tanggal_diambil=NOW() WHERE id=?";
        PreparedStatement p = c.prepareStatement(sql);
        p.setString(1, "Sudah Diambil");
        p.setString(2, idSelected);
        p.executeUpdate();

        // Cetak Nota Pengambilan
        cetakNota(
            txtNama.getText(),
            cmbLayanan.getSelectedItem().toString(),
            Integer.parseInt(txtBerat.getText()),
            hitungHarga(),
            "Sudah Diambil"
        );

        JOptionPane.showMessageDialog(this,
            "Laundry berhasil diambil.",
            "Sukses", JOptionPane.INFORMATION_MESSAGE);

        resetForm();
        loadData("");
        loadDashboardStats();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
            "Gagal memproses pengambilan: " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}
private void aksiCetakNota() {
    if (idSelected.isEmpty()) return;

    int confirm = JOptionPane.showConfirmDialog(this,
        "Cetak nota dan tandai sebagai SELESAI?",
        "Konfirmasi Cetak Nota",
        JOptionPane.YES_NO_OPTION);

    if (confirm != JOptionPane.YES_OPTION) return;

    try {
        cetakNota(
            txtNama.getText(),
            cmbLayanan.getSelectedItem().toString(),
            Integer.parseInt(txtBerat.getText()),
            hitungHarga(),
            "Selesai"
        );

        updateStatusOtomatis(idSelected, "Selesai");

        JOptionPane.showMessageDialog(this,
            "Nota dicetak.\nStatus berubah menjadi SELESAI.",
            "Sukses", JOptionPane.INFORMATION_MESSAGE);

        loadData("");
        loadDashboardStats();
        resetForm();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
            "Gagal mencetak nota: " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}
}

