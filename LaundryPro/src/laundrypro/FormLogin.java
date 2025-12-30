package laundrypro;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
/**
 *
 * @author Genzyy
 */
public class FormLogin extends JFrame {

    // Komponen
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin, btnCancel;
    private JCheckBox cbShowPass;

    public FormLogin() {
        initComponents();
    }

    private void initComponents() {
        // 1. Setup Frame
        setTitle("Laundry Pro - Login");
        setSize(400, 450); // Ukuran sedikit diperbesar agar lega
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null); // Absolute Layout
        
        // Ubah background frame jadi putih bersih
        getContentPane().setBackground(Color.WHITE);

        // ========================================================
        // 2. HEADER (Warna Biru di Atas)
        // ========================================================
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(25, 118, 210)); // Biru Laundry
        headerPanel.setBounds(0, 0, 400, 90);
        headerPanel.setLayout(null);
        add(headerPanel);

        // Judul di dalam Header
        JLabel lblIcon = new JLabel("🧺"); // Icon Emoji Laundry
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        lblIcon.setForeground(Color.WHITE);
        lblIcon.setBounds(20, 20, 50, 50);
        headerPanel.add(lblIcon);

        JLabel lblJudul = new JLabel("LAUNDRY PRO");
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblJudul.setForeground(Color.WHITE);
        lblJudul.setBounds(80, 20, 200, 30);
        headerPanel.add(lblJudul);
        
        JLabel lblSubJudul = new JLabel("Silakan login user");
        lblSubJudul.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubJudul.setForeground(new Color(220, 220, 220)); // Putih agak transparan
        lblSubJudul.setBounds(80, 50, 200, 20);
        headerPanel.add(lblSubJudul);

        // ========================================================
        // 3. FORM INPUT
        // ========================================================
        int startX = 40;
        int startY = 120;
        int width = 300;

        // Label Username
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUser.setForeground(new Color(50, 50, 50));
        lblUser.setBounds(startX, startY, width, 20);
        add(lblUser);

        // Text Field Username
        txtUser = new JTextField();
        txtUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUser.setBounds(startX, startY + 25, width, 35);
        txtUser.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200))); // Border halus
        add(txtUser);

        // Label Password
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPass.setForeground(new Color(50, 50, 50));
        lblPass.setBounds(startX, startY + 75, width, 20);
        add(lblPass);

        // Text Field Password
        txtPass = new JPasswordField();
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPass.setBounds(startX, startY + 100, width, 35);
        txtPass.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        add(txtPass);

        // Checkbox
        cbShowPass = new JCheckBox("Tampilkan Password");
        cbShowPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbShowPass.setBackground(Color.WHITE); // Samakan dengan background
        cbShowPass.setBounds(startX, startY + 140, 150, 20);
        cbShowPass.setFocusPainted(false); // Hilangkan garis putus-putus saat diklik
        cbShowPass.addActionListener(e -> {
            if (cbShowPass.isSelected()) txtPass.setEchoChar((char) 0);
            else txtPass.setEchoChar('•');
        });
        add(cbShowPass);

        // ========================================================
        // 4. TOMBOL (BUTTONS)
        // ========================================================
        
        // Tombol Login (Biru)
        btnLogin = new JButton("LOGIN");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(25, 118, 210)); // Biru sama dengan header
        btnLogin.setForeground(Color.WHITE); // Tulisan Putih
        btnLogin.setBounds(startX, 290, 140, 45);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false); // Flat style
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> prosesLogin());
        add(btnLogin);

        // Tombol Cancel (Merah Bata / Abu Tua)
        btnCancel = new JButton("BATAL");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancel.setBackground(new Color(220, 53, 69)); // Merah soft
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setBounds(200, 290, 140, 45);
        btnCancel.setFocusPainted(false);
        btnCancel.setBorderPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> System.exit(0));
        add(btnCancel);
        
        // Footer Copyright
        JLabel lblFooter = new JLabel("© 2024 Laundry Pro System");
        lblFooter.setHorizontalAlignment(SwingConstants.CENTER);
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblFooter.setForeground(Color.GRAY);
        lblFooter.setBounds(0, 380, 400, 20);
        add(lblFooter);
        
        getRootPane().setDefaultButton(btnLogin);
    }

    private void prosesLogin() {
        String username = txtUser.getText();
        String password = new String(txtPass.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan Password harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Connection c = Koneksi.getKoneksi();
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement p = c.prepareStatement(sql);
            p.setString(1, username);
            p.setString(2, password);

            ResultSet r = p.executeQuery();

            if (r.next()) {
                JOptionPane.showMessageDialog(this, "Login Berhasil!");
                new FormLaundry().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Username atau Password Salah!", "Gagal", JOptionPane.ERROR_MESSAGE);
                txtPass.setText("");
                txtPass.requestFocus();
            }
            r.close(); p.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Menggunakan tampilan Nimbus agar komponen (seperti checkbox) terlihat lebih modern
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {}

        SwingUtilities.invokeLater(() -> {
            new FormLogin().setVisible(true);
        });
    }
}