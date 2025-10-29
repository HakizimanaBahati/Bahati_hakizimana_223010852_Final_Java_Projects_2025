package com.form;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.security.MessageDigest;
import com.util.DB;

public class SignupForm extends JFrame implements ActionListener {
    private JTextField usertxt = new JTextField(15);
    private JPasswordField passtxt = new JPasswordField(15);
    private JPasswordField confirmpasstxt = new JPasswordField(15);
    private JComboBox<String> roleBox = new JComboBox<>(new String[]{"Student", "Instructor", "Admin"});
    private JButton registerBtn = new JButton("REGISTER");
    private JButton cancelBtn = new JButton("CANCEL");

    public SignupForm() {
        setTitle("Sign Up");
        setSize(500, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

      
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(240, 242, 245));

        
        JPanel header = new JPanel();
        header.setBackground(new Color(52, 152, 219));
        JLabel title = new JLabel("Create New Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        header.add(title);

        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,200,200)),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        styleTextField(usertxt);
        formPanel.add(usertxt, gbc);

       
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        styleTextField(passtxt);
        formPanel.add(passtxt, gbc);

       
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        styleTextField(confirmpasstxt);
        formPanel.add(confirmpasstxt, gbc);

        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        roleBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleBox.setBackground(Color.WHITE);
        formPanel.add(roleBox, gbc);

        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        styleButton(registerBtn, new Color(46, 204, 113)); 
        styleButton(cancelBtn, new Color(231, 76, 60));   
        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        container.add(header, BorderLayout.NORTH);
        container.add(formPanel, BorderLayout.CENTER);

        add(container);

      
        registerBtn.addActionListener(this);
        cancelBtn.addActionListener(this);

        setVisible(true);
    }

    
    private void styleButton(JButton btn, Color bgColor) {
        btn.setFocusPainted(false);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(120, 35));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });
    }

    private void styleTextField(JTextField txt) {
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180,180,180), 1, true),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    
    private String hashPassword(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelBtn) {
            dispose();
            new Loginform(); 
        }

        if (e.getSource() == registerBtn) {
            String username = usertxt.getText().trim();
            String password = new String(passtxt.getPassword());
            String confirmPassword = new String(confirmpasstxt.getPassword());
            String role = roleBox.getSelectedItem().toString();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!");
                return;
            }
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match!");
                return;
            }

            try (Connection con = DB.getConnection()) {
                String sql = "INSERT INTO User (Username, PasswordHash, Role) VALUES (?, ?, ?)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, username);
                ps.setString(2, hashPassword(password));
                ps.setString(3, role);

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Account created successfully!");
                dispose();
                new Loginform(); 
            } catch (SQLIntegrityConstraintViolationException ex) {
                JOptionPane.showMessageDialog(this, "Username already exists!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }// Hakizimana bahati
    }
}
