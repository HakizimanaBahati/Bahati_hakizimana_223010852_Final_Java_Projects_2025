package com.panel;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;
import com.util.DB;

public class AssignmentPanel extends JPanel implements ActionListener {

    JTable table;
    DefaultTableModel model;

    JButton addBtn = new JButton("Add");
    JButton updateBtn = new JButton("Update");
    JButton deleteBtn = new JButton("Delete");
    JButton loadBtn = new JButton("Load Assignments");
    JButton clearBtn = new JButton("Clear");

    JComboBox<String> studentCombo = new JComboBox<>();
    JComboBox<String> courseCombo = new JComboBox<>();
    JTextField titleTxt = new JTextField();
    JTextField descriptionTxt = new JTextField();
    JTextField dueDateTxt = new JTextField();
    JTextField maxMarksTxt = new JTextField();
    JComboBox<String> statusCombo = new JComboBox<>(new String[]{"assigned", "submitted", "graded"});
    JTextField remarksTxt = new JTextField();

    public AssignmentPanel() {
        setLayout(null);
        setBackground(Color.WHITE);

       
        JLabel title = new JLabel(" Welcome to Assignments  Panel");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(30, 144, 255));
        title.setBounds(20, 10, 400, 30);
        add(title);

     
        int xOffset = 40;
        int y = 60;
        addField("Student", studentCombo, xOffset, y); y += 40;
        addField("Course", courseCombo, xOffset, y); y += 40;
        addField("Title", titleTxt, xOffset, y); y += 40;
        addField("Description", descriptionTxt, xOffset, y); y += 40;
        addField("Due Date (YYYY-MM-DD)", dueDateTxt, xOffset, y); y += 40;
        addField("Max Marks", maxMarksTxt, xOffset, y); y += 40;
        addField("Status", statusCombo, xOffset, y); y += 40;
        addField("Remarks", remarksTxt, xOffset, y); y += 50;

        
        int buttonX = 650;
        styleButton(addBtn, new Color(46, 204, 113), buttonX, 60);
        styleButton(updateBtn, new Color(52, 152, 219), buttonX, 110);
        styleButton(deleteBtn, new Color(231, 76, 60), buttonX, 160);
        styleButton(loadBtn, new Color(155, 89, 182), buttonX, 210);
        styleButton(clearBtn, new Color(241, 196, 15), buttonX, 260);

        addBtn.addActionListener(this);
        updateBtn.addActionListener(this);
        deleteBtn.addActionListener(this);
        loadBtn.addActionListener(this);
        clearBtn.addActionListener(this);

        
        String[] columns = {"AssignmentID", "Student", "Course", "Title", "Description", "DueDate", "MaxMarks", "Status", "Remarks"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(model) {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                String status = getValueAt(row, 7).toString();
                if ("assigned".equalsIgnoreCase(status)) c.setBackground(new Color(255, 255, 200));
                else if ("submitted".equalsIgnoreCase(status)) c.setBackground(new Color(200, 230, 255));
                else if ("graded".equalsIgnoreCase(status)) c.setBackground(new Color(200, 255, 200));
                else c.setBackground(Color.WHITE);
                return c;
            }
        };

        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(230, 230, 230));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(40, 380, 820, 270);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        add(scrollPane);

     
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    studentCombo.setSelectedItem(model.getValueAt(row, 1));
                    courseCombo.setSelectedItem(model.getValueAt(row, 2));
                    titleTxt.setText(model.getValueAt(row, 3).toString());
                    descriptionTxt.setText(model.getValueAt(row, 4).toString());
                    dueDateTxt.setText(model.getValueAt(row, 5).toString());
                    maxMarksTxt.setText(model.getValueAt(row, 6).toString());
                    statusCombo.setSelectedItem(model.getValueAt(row, 7));
                    remarksTxt.setText(model.getValueAt(row, 8) != null ? model.getValueAt(row, 8).toString() : "");
                }
            }
        });

       
        loadStudents();
        loadCourses();
        loadAssignments();
    }

    private void addField(String label, JComponent field, int x, int y) {
        JLabel l = new JLabel(label);
        l.setBounds(x, y, 160, 25);
        field.setBounds(x + 170, y, 250, 25);
        add(l); add(field);
    }

    private void styleButton(JButton btn, Color bgColor, int x, int y) {
        btn.setBounds(x, y, 180, 35);
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
                        "INSERT INTO assignment(StudentID, CourseID, Title, Description, DueDate, MaxMarks, Status, Remarks) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
                ps.setInt(1, getSelectedStudentID());
                ps.setInt(2, getSelectedCourseID());
                ps.setString(3, titleTxt.getText());
                ps.setString(4, descriptionTxt.getText());
                ps.setString(5, dueDateTxt.getText());
                ps.setDouble(6, Double.parseDouble(maxMarksTxt.getText()));
                ps.setString(7, statusCombo.getSelectedItem().toString());
                ps.setString(8, remarksTxt.getText());
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Assignment Added!");
            } else if (e.getSource() == updateBtn) {
                int row = table.getSelectedRow();
                if (row < 0) { JOptionPane.showMessageDialog(this, "Select an assignment to update!"); return; }
                int assignmentID = (int) model.getValueAt(row, 0);
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE assignment SET StudentID=?, CourseID=?, Title=?, Description=?, DueDate=?, MaxMarks=?, Status=?, Remarks=? WHERE AssignmentID=?");
                ps.setInt(1, getSelectedStudentID());
                ps.setInt(2, getSelectedCourseID());
                ps.setString(3, titleTxt.getText());
                ps.setString(4, descriptionTxt.getText());
                ps.setString(5, dueDateTxt.getText());
                ps.setDouble(6, Double.parseDouble(maxMarksTxt.getText()));
                ps.setString(7, statusCombo.getSelectedItem().toString());
                ps.setString(8, remarksTxt.getText());
                ps.setInt(9, assignmentID);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Assignment Updated!");
            } else if (e.getSource() == deleteBtn) {
                int row = table.getSelectedRow();
                if (row < 0) { JOptionPane.showMessageDialog(this, "Select an assignment to delete!"); return; }
                int assignmentID = (int) model.getValueAt(row, 0);
                PreparedStatement ps = con.prepareStatement("DELETE FROM assignment WHERE AssignmentID=?");
                ps.setInt(1, assignmentID);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Assignment Deleted!");
            } else if (e.getSource() == loadBtn) {
                loadAssignments();
            } else if (e.getSource() == clearBtn) {
                clearFields();
            }
            loadAssignments();
            clearFields();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void loadAssignments() {
        try (Connection con = DB.getConnection()) {
            model.setRowCount(0);
            String sql = "SELECT a.AssignmentID, CONCAT(s.FirstName,' ',s.LastName) AS StudentName, " +
                         "c.CourseName, a.Title, a.Description, a.DueDate, a.MaxMarks, a.Status, a.Remarks " +
                         "FROM assignment a " +
                         "JOIN student s ON a.StudentID = s.StudentID " +
                         "JOIN course c ON a.CourseID = c.CourseID";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("AssignmentID"),
                        rs.getString("StudentName"),
                        rs.getString("CourseName"),
                        rs.getString("Title"),
                        rs.getString("Description"),
                        rs.getDate("DueDate"),
                        rs.getDouble("MaxMarks"),
                        rs.getString("Status"),
                        rs.getString("Remarks")
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadStudents() {
        try (Connection con = DB.getConnection()) {
            studentCombo.removeAllItems();
            ResultSet rs = con.createStatement().executeQuery("SELECT StudentID, FirstName, LastName FROM student");
            while (rs.next()) {
                studentCombo.addItem(rs.getString("FirstName") + " " + rs.getString("LastName") + " [" + rs.getInt("StudentID") + "]");
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadCourses() {
        try (Connection con = DB.getConnection()) {
            courseCombo.removeAllItems();
            ResultSet rs = con.createStatement().executeQuery("SELECT CourseID, CourseName FROM course");
            while (rs.next()) {
                courseCombo.addItem(rs.getString("CourseName") + " [" + rs.getInt("CourseID") + "]");
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private int getSelectedStudentID() {
        String item = (String) studentCombo.getSelectedItem();
        return Integer.parseInt(item.substring(item.lastIndexOf("[") + 1, item.lastIndexOf("]")));
    }

    private int getSelectedCourseID() {
        String item = (String) courseCombo.getSelectedItem();
        return Integer.parseInt(item.substring(item.lastIndexOf("[") + 1, item.lastIndexOf("]")));
    }

    private void clearFields() {
        titleTxt.setText("");
        descriptionTxt.setText("");
        dueDateTxt.setText("");
        maxMarksTxt.setText("");
        remarksTxt.setText("");
    }

    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Assignment Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(950, 700);
        frame.setLocationRelativeTo(null);
        frame.add(new AssignmentPanel());
        frame.setVisible(true);
    }
}
