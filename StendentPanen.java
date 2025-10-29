package com.panel;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import com.util.DB;

public class StendentPanen extends JPanel implements ActionListener {

    private JTextField studentIdTxt = new JTextField();
    private JTextField firstNameTxt = new JTextField();
    private JTextField lastNameTxt = new JTextField();
    private JTextField emailTxt = new JTextField();
    private JTextField searchTxt = new JTextField();

    private JButton addBtn = new JButton("Add");
    private JButton updateBtn = new JButton("Update");
    private JButton deleteBtn = new JButton("Delete");
    private JButton loadBtn = new JButton("Load");

    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    public StendentPanen() {
        setLayout(null);
        setBackground(Color.WHITE);

        JLabel header = new JLabel("Welcome to the Student Panel");
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(new Color(30, 144, 255));
        header.setBounds(20, 10, 400, 30);
        add(header);

        // Table setup
        String[] columns = {"StudentID", "FirstName", "LastName", "Email", "CreatedAt"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(52, 152, 219, 100));
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(230, 230, 230));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected)
                    c.setBackground(new Color(52, 152, 219, 100));
                else
                    c.setBackground(row % 2 == 0 ? new Color(245, 245, 250) : Color.WHITE);
                setHorizontalAlignment(column == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
                return c;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(20, 360, 850, 300);
        add(sp);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Search field
        JLabel searchLabel = new JLabel("Search by Name/Email:");
        searchLabel.setBounds(20, 320, 180, 25);
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(searchLabel);

        searchTxt.setBounds(200, 320, 200, 25);
        searchTxt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(searchTxt);
        searchTxt.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

        // Input fields
        int y = 50;
        addField("StudentID", studentIdTxt, y);
        studentIdTxt.setEditable(false);
        y += 40;
        addField("First Name", firstNameTxt, y);
        y += 40;
        addField("Last Name", lastNameTxt, y);
        y += 40;
        addField("Email", emailTxt, y);
        y += 40;

        // Buttons
        addButtons();

        // Table click event
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int modelRow = table.convertRowIndexToModel(row);
                    studentIdTxt.setText(String.valueOf(model.getValueAt(modelRow, 0)));
                    firstNameTxt.setText((String) model.getValueAt(modelRow, 1));
                    lastNameTxt.setText((String) model.getValueAt(modelRow, 2));
                    emailTxt.setText((String) model.getValueAt(modelRow, 3));
                }
            }
        });

        loadStudents();
    }

    private void addField(String lbl, JComponent field, int y) {
        JLabel l = new JLabel(lbl);
        l.setBounds(20, y, 120, 25);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBounds(150, y, 200, 25);
        add(l);
        add(field);
    }

    private void addButtons() {
        styleButton(addBtn, new Color(46, 204, 113), 400, 20);
        styleButton(updateBtn, new Color(52, 152, 219), 400, 70);
        styleButton(deleteBtn, new Color(231, 76, 60), 400, 120);
        styleButton(loadBtn, new Color(155, 89, 182), 400, 170);

        addBtn.addActionListener(this);
        updateBtn.addActionListener(this);
        deleteBtn.addActionListener(this);
        loadBtn.addActionListener(this);
    }

    private void styleButton(JButton btn, Color bg, int x, int y) {
        btn.setBounds(x, y, 120, 30);
        btn.setFocusPainted(false);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(btn);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
    }

    private void filterTable() {
        String text = searchTxt.getText();
        if (text.trim().length() == 0)
            sorter.setRowFilter(null);
        else
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1, 2, 3));
    }

    public void actionPerformed(ActionEvent e) {
        try (Connection con = DB.getConnection()) {

            if (e.getSource() == addBtn) {
                if (firstNameTxt.getText().isEmpty() || lastNameTxt.getText().isEmpty() || emailTxt.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "First Name, Last Name, and Email are required!");
                    return;
                }

                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO Student (FirstName, LastName, Email) VALUES (?, ?, ?)",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(1, firstNameTxt.getText());
                ps.setString(2, lastNameTxt.getText());
                ps.setString(3, emailTxt.getText());
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) studentIdTxt.setText(String.valueOf(keys.getInt(1)));
                JOptionPane.showMessageDialog(this, "Student Added!");
                loadStudents();
                clearFields();

            } else if (e.getSource() == updateBtn) {
                if (studentIdTxt.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Select a student!");
                    return;
                }

                PreparedStatement ps = con.prepareStatement(
                        "UPDATE Student SET FirstName=?, LastName=?, Email=? WHERE StudentID=?");
                ps.setString(1, firstNameTxt.getText());
                ps.setString(2, lastNameTxt.getText());
                ps.setString(3, emailTxt.getText());
                ps.setInt(4, Integer.parseInt(studentIdTxt.getText()));
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Student Updated!");
                loadStudents();
                clearFields();

            } else if (e.getSource() == deleteBtn) {
                if (studentIdTxt.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Select a student!");
                    return;
                }

                PreparedStatement ps = con.prepareStatement("DELETE FROM Student WHERE StudentID=?");
                ps.setInt(1, Integer.parseInt(studentIdTxt.getText()));
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Student Deleted!");
                loadStudents();
                clearFields();

            } else if (e.getSource() == loadBtn) {
                loadStudents();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void loadStudents() {
        try (Connection con = DB.getConnection()) {
            model.setRowCount(0);
            String sql = "SELECT StudentID, FirstName, LastName, Email, CreatedAt FROM Student";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("StudentID"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("Email"),
                        rs.getTimestamp("CreatedAt")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void clearFields() {
        studentIdTxt.setText("");
        firstNameTxt.setText("");
        lastNameTxt.setText("");
        emailTxt.setText("");
    }
}
