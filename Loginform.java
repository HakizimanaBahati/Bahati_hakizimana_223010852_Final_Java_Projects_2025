package com.form;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import java.security.MessageDigest;
import java.util.prefs.Preferences; 
import com.util.DB;

public class Loginform extends JFrame implements ActionListener {
    private JTextField usertxt = new JTextField(15);
    private JPasswordField passtxt = new JPasswordField(15);
    private JButton loginbtn = new JButton("LOGIN");
    private JButton cancelbtn = new JButton("CANCEL");
    private JButton signupbtn = new JButton("SIGN UP");
    private JCheckBox notRobotChk = new JCheckBox("I'm not a robot");
    private JCheckBox showPassChk = new JCheckBox("Show Password");
    private JCheckBox rememberMeChk = new JCheckBox("Remember Me");
    private Preferences prefs = Preferences.userRoot().node(this.getClass().getName());

    public Loginform() {
        setTitle("Login Form");
        setSize(500, 450); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(false);//

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(240, 242, 245));

        
        JPanel header = new JPanel();
        header.setBackground(new Color(30, 144, 255));
        JLabel title = new JLabel("Welcome to Education Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        header.add(title);

        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,200,200)),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        styleTextField(usertxt);
        mainPanel.add(usertxt, gbc);

       
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        styleTextField(passtxt);
        mainPanel.add(passtxt, gbc);

       
        gbc.gridx = 1; gbc.gridy = 2;
        mainPanel.add(showPassChk, gbc);

     
        gbc.gridx = 1; gbc.gridy = 3;
        mainPanel.add(rememberMeChk, gbc);

       
        gbc.gridx = 1; gbc.gridy = 4;
        mainPanel.add(notRobotChk, gbc);


        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        styleButton(loginbtn, new Color(46, 204, 113));  
        styleButton(cancelbtn, new Color(231, 76, 60));  
        styleButton(signupbtn, new Color(52, 152, 219)); 

        buttonPanel.add(loginbtn);
        buttonPanel.add(cancelbtn);
        buttonPanel.add(signupbtn);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        container.add(header, BorderLayout.NORTH);
        container.add(mainPanel, BorderLayout.CENTER);

        add(container);

      
        loginbtn.addActionListener(this);
        cancelbtn.addActionListener(this);
        signupbtn.addActionListener(this);
  

        showPassChk.addActionListener(e -> {
            if (showPassChk.isSelected()) {
                passtxt.setEchoChar((char) 0);
            } else {
                passtxt.setEchoChar('•');
            }
        });

       
        String savedUser = prefs.get("username", "");
        if (!savedUser.isEmpty()) {
            usertxt.setText(savedUser);
            rememberMeChk.setSelected(true);
        }

        setVisible(true);
    }

   
    private void styleButton(JButton btn, Color bgColor) {
        btn.setFocusPainted(false);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(100, 35));
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
        if (e.getSource() == cancelbtn) {
            System.exit(0);
        }
        if (e.getSource() == signupbtn) {
            dispose();
            new SignupForm(); 
        }
        
        if (e.getSource() == loginbtn) {
            if (!notRobotChk.isSelected()) {
                JOptionPane.showMessageDialog(this, "Please confirm you are not a robot!");
                return;
            }

            try (Connection con = DB.getConnection()) {
                String sql = "SELECT * FROM User WHERE Username=? AND PasswordHash=?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, usertxt.getText());
                ps.setString(2, hashPassword(new String(passtxt.getPassword())));

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String role = rs.getString("Role");
                    int userId = rs.getInt("UserID");
                    JOptionPane.showMessageDialog(this, "Login successful! Role: " + role);

                    if (rememberMeChk.isSelected()) {
                        prefs.put("username", usertxt.getText());
                    } else {
                        prefs.remove("username");
                    }

                    dispose();
                    new EMS(role, userId);  
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Loginform::new);
    }
}
