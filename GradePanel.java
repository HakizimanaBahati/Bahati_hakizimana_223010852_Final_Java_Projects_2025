package com.panel;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import com.util.DB;

public class GradePanel extends JPanel implements ActionListener {

    JTable table;
    DefaultTableModel model;

    JButton addBtn = new JButton("Add");
    JButton updateBtn = new JButton("Update");
    JButton deleteBtn = new JButton("Delete");
    JButton loadBtn = new JButton("Load Grades");

    JTextField searchTxt = new JTextField();
    JComboBox<String> assignmentCombo = new JComboBox<>();
    JTextField gradeValueTxt = new JTextField();
    JComboBox<String> gradeLetterCombo = new JComboBox<>(new String[]{"A", "B", "C", "D", "E", "F"});
    JTextField feedbackTxt = new JTextField();

    public GradePanel() {
        setLayout(null);
        setBackground(Color.WHITE);

        
        JLabel title = new JLabel("Student Grades");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(30, 144, 255));
        title.setBounds(20, 10, 400, 30);
        add(title);

       
        searchTxt.setBounds(20, 60, 400, 40);
        searchTxt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchTxt.setBorder(BorderFactory.createTitledBorder("Search by Assignment or Grade"));
        add(searchTxt);

        
        int xOffset = 20, y = 120;
        addField("Assignment", assignmentCombo, xOffset, y); y += 40;
        addField("Grade Value", gradeValueTxt, xOffset, y); y += 40;
        addField("Grade Letter", gradeLetterCombo, xOffset, y); y += 40;
        addField("Feedback", feedbackTxt, xOffset, y); y += 50;

       
        int btnX = 800;
        styleButton(addBtn, new Color(46, 204, 113), btnX, 60);
        styleButton(updateBtn, new Color(52, 152, 219), btnX, 110);
        styleButton(deleteBtn, new Color(231, 76, 60), btnX, 160);
        styleButton(loadBtn, new Color(155, 89, 182), btnX, 210);

        addBtn.addActionListener(this);
        updateBtn.addActionListener(this);
        deleteBtn.addActionListener(this);
        loadBtn.addActionListener(this);

       
        String[] columns = {"GradeID", "Assignment Title", "GradeValue", "GradeLetter", "Feedback", "CreatedAt"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(model) {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                String grade = getValueAt(row, 3).toString();
                switch (grade) {
                    case "A": c.setBackground(new Color(198, 239, 206)); break;
                    case "B": c.setBackground(new Color(219, 238, 244)); break;
                    case "C": c.setBackground(new Color(255, 255, 204)); break;
                    case "D": c.setBackground(new Color(255, 229, 204)); break;
                    case "E": c.setBackground(new Color(255, 204, 203)); break;
                    case "F": 
                        c.setBackground(new Color(204, 0, 0)); 
                        c.setForeground(Color.WHITE); 
                        break;
                    default: 
                        c.setBackground(Color.WHITE); 
                        c.setForeground(Color.BLACK);
                        break;
                }
                return c;
            }
        };

        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(230, 230, 230));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(20, y, 880, 300);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        add(scrollPane);

        
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    assignmentCombo.setSelectedItem(model.getValueAt(row, 1));
                    gradeValueTxt.setText(model.getValueAt(row, 2).toString());
                    gradeLetterCombo.setSelectedItem(model.getValueAt(row, 3));
                    feedbackTxt.setText(model.getValueAt(row, 4) != null ? model.getValueAt(row, 4).toString() : "");
                }
            }
        });

        
        searchTxt.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        
        loadAssignments();
        loadGrades();
    }

    private void addField(String label, JComponent field, int x, int y) {
        JLabel l = new JLabel(label);
        l.setBounds(x, y, 150, 25);
        field.setBounds(x + 160, y, 250, 25);
        add(l);
        add(field);
    }

    private void styleButton(JButton btn, Color bgColor, int x, int y) {
        btn.setBounds(x, y, 120, 35);
        btn.setFocusPainted(false);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { btn.setBackground(bgColor.darker()); }
            public void mouseExited(MouseEvent evt) { btn.setBackground(bgColor); }
        });
        add(btn);
    }

    public void actionPerformed(ActionEvent e) {
        try (Connection con = DB.getConnection()) {
            if (e.getSource() == addBtn) {
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO grade(AssignmentID, GradeValue, GradeLetter, Feedback, CreatedAt) VALUES(?,?,?,?,NOW())");
                ps.setInt(1, getSelectedAssignmentID());
                ps.setDouble(2, Double.parseDouble(gradeValueTxt.getText()));
                ps.setString(3, gradeLetterCombo.getSelectedItem().toString());
                ps.setString(4, feedbackTxt.getText());
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Grade Added!");
            } else if (e.getSource() == updateBtn) {
                int row = table.getSelectedRow();
                if (row < 0) { JOptionPane.showMessageDialog(this, "Select a grade to update!"); return; }
                int gradeID = (int) model.getValueAt(row, 0);
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE grade SET AssignmentID=?, GradeValue=?, GradeLetter=?, Feedback=? WHERE GradeID=?");
                ps.setInt(1, getSelectedAssignmentID());
                ps.setDouble(2, Double.parseDouble(gradeValueTxt.getText()));
                ps.setString(3, gradeLetterCombo.getSelectedItem().toString());
                ps.setString(4, feedbackTxt.getText());
                ps.setInt(5, gradeID);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Grade Updated!");
            } else if (e.getSource() == deleteBtn) {
                int row = table.getSelectedRow();
                if (row < 0) { JOptionPane.showMessageDialog(this, "Select a grade to delete!"); return; }
                int gradeID = (int) model.getValueAt(row, 0);
                PreparedStatement ps = con.prepareStatement("DELETE FROM grade WHERE GradeID=?");
                ps.setInt(1, gradeID);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Grade Deleted!");
            }
            loadGrades();
            clearFields();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void loadAssignments() {
        try (Connection con = DB.getConnection()) {
            assignmentCombo.removeAllItems();
            ResultSet rs = con.createStatement().executeQuery("SELECT AssignmentID, Title FROM assignment");
            while (rs.next()) {
                assignmentCombo.addItem(rs.getString("Title") + " [" + rs.getInt("AssignmentID") + "]");
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private int getSelectedAssignmentID() {
        String item = (String) assignmentCombo.getSelectedItem();
        return Integer.parseInt(item.substring(item.lastIndexOf("[") + 1, item.lastIndexOf("]")));
    }

    private void loadGrades() {
        try (Connection con = DB.getConnection()) {
            model.setRowCount(0);
            String sql = "SELECT g.GradeID, a.Title AS AssignmentTitle, g.GradeValue, g.GradeLetter, g.Feedback, g.CreatedAt " +
                         "FROM grade g JOIN assignment a ON g.AssignmentID = a.AssignmentID";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("GradeID"),
                        rs.getString("AssignmentTitle"),
                        rs.getDouble("GradeValue"),
                        rs.getString("GradeLetter"),
                        rs.getString("Feedback"),
                        rs.getString("CreatedAt")
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void filterTable() {
        String query = searchTxt.getText().toLowerCase();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
    }

    private void clearFields() {
        gradeValueTxt.setText("");
        feedbackTxt.setText("");
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Grade Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(950, 650);
        frame.setLocationRelativeTo(null);
        frame.add(new GradePanel());
        frame.setVisible(true);
    }
}
