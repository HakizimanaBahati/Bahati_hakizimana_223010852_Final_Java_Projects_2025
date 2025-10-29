package com.panel;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;
import com.util.DB;

public class EnrollmentPanel extends JPanel implements ActionListener {

    private JTable table;
    private DefaultTableModel model;

    private JTextField enrollmentIdTxt = new JTextField();
    private JComboBox<String> studentCmb = new JComboBox<>();
    private JComboBox<String> courseCmb = new JComboBox<>();
    private JTextField statusTxt = new JTextField();
    private JTextField remarksTxt = new JTextField();

    private JButton addBtn = new JButton("Add");
    private JButton updateBtn = new JButton("Update");
    private JButton deleteBtn = new JButton("Delete");
    private JButton loadBtn = new JButton("Load Enrollments");
    private JButton clearBtn = new JButton("Clear");

    public EnrollmentPanel() {
        setLayout(null);
        setBackground(Color.WHITE);

        // ---------- Header ----------
        JLabel title = new JLabel("Student Enrollments");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(30, 144, 255));
        title.setBounds(20, 10, 400, 30);
        add(title);

        // ---------- Form Fields ----------
        int xOffset = 40;
        int y = 60;
        addField("Enrollment ID", enrollmentIdTxt, xOffset, y, false); y += 40;
        addComboField("Student", studentCmb, xOffset, y); y += 40;
        addComboField("Course", courseCmb, xOffset, y); y += 40;
        addField("Status", statusTxt, xOffset, y, true); y += 40;
        addField("Remarks", remarksTxt, xOffset, y, true); y += 50;

        // ---------- Right-Side Buttons ----------
        int buttonX = 600;
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

        // ---------- Table ----------
        String[] columns = { "EnrollmentID", "Student Name", "Course Name", "Enroll Date", "Status", "Remarks" };
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(200, 230, 250));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(230, 230, 230));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    if (row % 2 == 0) c.setBackground(new Color(245, 245, 250));
                    else c.setBackground(Color.WHITE);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(40, 320, 800, 330);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        add(scrollPane);

        // ---------- Table Row Click ----------
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int modelRow = table.convertRowIndexToModel(row);
                    enrollmentIdTxt.setText(String.valueOf(model.getValueAt(modelRow, 0)));
                    studentCmb.setSelectedItem(model.getValueAt(modelRow, 1));
                    courseCmb.setSelectedItem(model.getValueAt(modelRow, 2));
                    statusTxt.setText(model.getValueAt(modelRow, 4).toString());
                    remarksTxt.setText(model.getValueAt(modelRow, 5).toString());
                }
            }
        });

        // Load initial data
        loadStudents();
        loadCourses();
        loadEnrollments();
    }

    private void addField(String lbl, JTextField field, int x, int y, boolean editable) {
        JLabel l = new JLabel(lbl);
        l.setBounds(x, y, 120, 25);
        field.setBounds(x + 130, y, 250, 25);
        field.setEditable(editable);
        add(l); add(field);
    }

    private void addComboField(String lbl, JComboBox<String> cmb, int x, int y) {
        JLabel l = new JLabel(lbl);
        l.setBounds(x, y, 120, 25);
        cmb.setBounds(x + 130, y, 250, 25);
        add(l); add(cmb);
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

    @Override
    public void actionPerformed(ActionEvent e) {
        try (Connection con = DB.getConnection()) {
            if (e.getSource() == addBtn) {
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO enrollment(StudentID, CourseID, EnrollDate, Status, Remarks) VALUES(?, ?, NOW(), ?, ?)");
                ps.setInt(1, getSelectedStudentID());
                ps.setInt(2, getSelectedCourseID());
                ps.setString(3, statusTxt.getText());
                ps.setString(4, remarksTxt.getText());
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Enrollment Added!");
            } else if (e.getSource() == updateBtn) {
                if (enrollmentIdTxt.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Select an enrollment!");
                    return;
                }
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE enrollment SET StudentID=?, CourseID=?, Status=?, Remarks=? WHERE EnrollmentID=?");
                ps.setInt(1, getSelectedStudentID());
                ps.setInt(2, getSelectedCourseID());
                ps.setString(3, statusTxt.getText());
                ps.setString(4, remarksTxt.getText());
                ps.setInt(5, Integer.parseInt(enrollmentIdTxt.getText()));
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Enrollment Updated!");
            } else if (e.getSource() == deleteBtn) {
                if (enrollmentIdTxt.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Select an enrollment!");
                    return;
                }
                PreparedStatement ps = con.prepareStatement("DELETE FROM enrollment WHERE EnrollmentID=?");
                ps.setInt(1, Integer.parseInt(enrollmentIdTxt.getText()));
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Enrollment Deleted!");
            } else if (e.getSource() == loadBtn) {
                loadEnrollments();
            } else if (e.getSource() == clearBtn) {
                clearFields();
            }
            loadEnrollments();
            clearFields();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadEnrollments() {
        try (Connection con = DB.getConnection()) {
            model.setRowCount(0);
            String sql = "SELECT e.EnrollmentID, CONCAT(s.FirstName,' ',s.LastName) AS StudentName, " +
                         "c.CourseName, e.EnrollDate, e.Status, e.Remarks " +
                         "FROM enrollment e " +
                         "JOIN student s ON e.StudentID = s.StudentID " +
                         "JOIN course c ON e.CourseID = c.CourseID";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                model.addRow(new Object[] {
                    rs.getInt("EnrollmentID"),
                    rs.getString("StudentName"),
                    rs.getString("CourseName"),
                    rs.getDate("EnrollDate"),
                    rs.getString("Status"),
                    rs.getString("Remarks")
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadStudents() {
        try (Connection con = DB.getConnection()) {
            studentCmb.removeAllItems();
            ResultSet rs = con.createStatement().executeQuery("SELECT StudentID, FirstName, LastName FROM student");
            while (rs.next()) {
                studentCmb.addItem(rs.getInt("StudentID") + " - " + rs.getString("FirstName") + " " + rs.getString("LastName"));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadCourses() {
        try (Connection con = DB.getConnection()) {
            courseCmb.removeAllItems();
            ResultSet rs = con.createStatement().executeQuery("SELECT CourseID, CourseName FROM course");
            while (rs.next()) {
                courseCmb.addItem(rs.getInt("CourseID") + " - " + rs.getString("CourseName"));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private int getSelectedStudentID() {
        String item = (String) studentCmb.getSelectedItem();
        return Integer.parseInt(item.split(" - ")[0]);
    }

    private int getSelectedCourseID() {
        String item = (String) courseCmb.getSelectedItem();
        return Integer.parseInt(item.split(" - ")[0]);
    }

    private void clearFields() {
        enrollmentIdTxt.setText("");
        statusTxt.setText("");
        remarksTxt.setText("");
        if (studentCmb.getItemCount() > 0) studentCmb.setSelectedIndex(0);
        if (courseCmb.getItemCount() > 0) courseCmb.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Enrollment Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(950, 700);
        frame.setLocationRelativeTo(null);
        frame.add(new EnrollmentPanel());
        frame.setVisible(true);
    }
}
