package com.panel;

import java.awt.*;
import java.awt.event.*;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.util.DB;

public class UserPanel extends JPanel implements ActionListener {

    private JTextField idTxt = new JTextField();
    private JTextField nameTxt = new JTextField();
    private JPasswordField passTxt = new JPasswordField();
    private JComboBox<String> roleCmb = new JComboBox<>(new String[]{"admin", "student", "instructor"});
    private JCheckBox showPassChk = new JCheckBox("Show Password");

    private JButton addBtn = new JButton("➕ Add");
    private JButton updateBtn = new JButton("✏ Update");
    private JButton deleteBtn = new JButton("🗑 Delete");
    private JButton loadBtn = new JButton("🔄 Load");

    private JTable table;
    private DefaultTableModel model;

    public UserPanel() {
        setLayout(null);
        setBackground(new Color(245, 247, 250));

      
        JLabel header = new JLabel("Welcome to User Panel", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(new Color(52, 73, 94));
        header.setBounds(0, 10, 750, 40);
        add(header);

       
        String[] columns = {"UserID", "Username", "PasswordHash", "Role"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(52, 152, 219, 120));
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(230, 230, 230));
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(new Color(52, 152, 219, 120));
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                }
                setHorizontalAlignment(column == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
                return c;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(20, 320, 700, 260);
        add(sp);

        
        int y = 70;
        addField("UserID:", idTxt, y);
        idTxt.setEditable(false);
        y += 40;
        addField("Username:", nameTxt, y);
        y += 40;
        addField("Password:", passTxt, y);
        showPassChk.setBounds(280, y, 150, 25);
        showPassChk.setBackground(new Color(245, 247, 250));
        add(showPassChk);
        y += 40;
        addComboField("Role:", roleCmb, y);
        y += 40;

        
        styleButton(addBtn, new Color(46, 204, 113), 500, 70);
        styleButton(updateBtn, new Color(52, 152, 219), 500, 120);
        styleButton(deleteBtn, new Color(231, 76, 60), 500, 170);
        styleButton(loadBtn, new Color(155, 89, 182), 500, 220);

        addBtn.addActionListener(this);
        updateBtn.addActionListener(this);
        deleteBtn.addActionListener(this);
        loadBtn.addActionListener(this);

      
        showPassChk.addActionListener(e -> {
            if (showPassChk.isSelected()) passTxt.setEchoChar((char) 0);
            else passTxt.setEchoChar('•');
        });

      
        loadUsers();

        
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int modelRow = table.convertRowIndexToModel(row);
                    idTxt.setText(model.getValueAt(modelRow, 0).toString());
                    nameTxt.setText(model.getValueAt(modelRow, 1).toString());
                    passTxt.setText(model.getValueAt(modelRow, 2).toString());
                    roleCmb.setSelectedItem(model.getValueAt(modelRow, 3).toString());
                }
            }
        });
    }

    
    private void addField(String lbl, JComponent txt, int y) {
        JLabel l = new JLabel(lbl);
        l.setBounds(20, y, 100, 25);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBounds(130, y, 200, 25);
        add(l);
        add(txt);
    }

    private void addComboField(String lbl, JComboBox<String> cmb, int y) {
        JLabel l = new JLabel(lbl);
        l.setBounds(20, y, 100, 25);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmb.setBounds(130, y, 200, 25);
        add(l);
        add(cmb);
    }

    private void styleButton(JButton btn, Color bgColor, int x, int y) {
        btn.setBounds(x, y, 140, 35);
        btn.setFocusPainted(false);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(btn);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                btn.setBackground(bgColor.darker());
            }

            public void mouseExited(MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });
    }

    private String hashPassword(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public void actionPerformed(ActionEvent e) {
        try (Connection con = DB.getConnection()) {
            if (e.getSource() == addBtn) {
                if (nameTxt.getText().isEmpty() || passTxt.getPassword().length == 0) {
                    JOptionPane.showMessageDialog(this, "Username and Password cannot be empty!");
                    return;
                }
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO `User` (Username, PasswordHash, Role) VALUES (?, ?, ?)",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(1, nameTxt.getText());
                ps.setString(2, hashPassword(new String(passTxt.getPassword())));
                ps.setString(3, roleCmb.getSelectedItem().toString());
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) idTxt.setText(String.valueOf(keys.getInt(1)));
                JOptionPane.showMessageDialog(this, "✅ User Added!");
                loadUsers();
                clearFieldsExceptId();

            } else if (e.getSource() == updateBtn) {
                if (idTxt.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Select a user!");
                    return;
                }
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE `User` SET Username=?, PasswordHash=?, Role=? WHERE UserID=?");
                ps.setString(1, nameTxt.getText());
                ps.setString(2, hashPassword(new String(passTxt.getPassword())));
                ps.setString(3, roleCmb.getSelectedItem().toString());
                ps.setInt(4, Integer.parseInt(idTxt.getText()));
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "✅ User Updated!");
                loadUsers();
                clearFieldsExceptId();

            } else if (e.getSource() == deleteBtn) {
                if (idTxt.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Select a user!");
                    return;
                }
                PreparedStatement ps = con.prepareStatement("DELETE FROM `User` WHERE UserID=?");
                ps.setInt(1, Integer.parseInt(idTxt.getText()));
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "🗑 User Deleted!");
                loadUsers();
                clearFieldsExceptId();

            } else if (e.getSource() == loadBtn) {
                loadUsers();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
        }
    }

    private void loadUsers() {
        try (Connection con = DB.getConnection()) {
            model.setRowCount(0);
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM `User`");
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("UserID"),
                        rs.getString("Username"),
                        rs.getString("PasswordHash"),
                        rs.getString("Role")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void clearFieldsExceptId() {
        nameTxt.setText("");
        passTxt.setText("");
        roleCmb.setSelectedIndex(0);
    }
}
