package com.panel;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import com.util.DB;
import java.util.HashMap;

public class CoursePanel extends JPanel implements ActionListener {

    private JTextField courseIdTxt = new JTextField();
    private JTextField courseNameTxt = new JTextField();
    private JTextField courseCodeTxt = new JTextField();
    private JTextField courseDescTxt = new JTextField();
    private JComboBox<String> instructorCmb = new JComboBox<>();

    private HashMap<String, Integer> instructorMap = new HashMap<>();

    private JTextField searchTxt = new JTextField();

    private JButton addBtn = new JButton("Add");
    private JButton updateBtn = new JButton("Update");
    private JButton deleteBtn = new JButton("Delete");
    private JButton loadBtn = new JButton("Load");

    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    public CoursePanel() {
        setLayout(null);
        setBackground(Color.WHITE);

        
        JLabel header = new JLabel("Welcome to the Course Panel");
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(new Color(30, 144, 255));
        header.setBounds(20, 10, 400, 30);
        add(header);

       
        String[] columns = {"CourseID", "CourseName", "CourseCode", "Description", "Instructor", "CreatedAt"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(52, 152, 219, 100));
        table.setSelectionForeground(Color.BLACK);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) c.setBackground(new Color(52, 152, 219, 100));
                else c.setBackground(row % 2 == 0 ? new Color(245, 245, 250) : Color.WHITE);
                setHorizontalAlignment(column == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
                return c;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(20, 400, 900, 300);
        add(sp);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        
        JLabel searchLabel = new JLabel("Search by Course Name:");
        searchLabel.setBounds(20, 360, 180, 25);
        add(searchLabel);

        searchTxt.setBounds(200, 360, 200, 25);
        add(searchTxt);
        searchTxt.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

       
        int y = 60; 
        addField("Course ID", courseIdTxt, y); courseIdTxt.setEditable(false); y += 40;
        addField("Course Name", courseNameTxt, y); y += 40;
        addField("Course Code", courseCodeTxt, y); y += 40;
        addField("Description", courseDescTxt, y); y += 40;
        addComboField("Instructor", instructorCmb, y); y += 50;

        
        addButtons();

        
        loadInstructors();

       
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int modelRow = table.convertRowIndexToModel(row);
                    courseIdTxt.setText(String.valueOf(model.getValueAt(modelRow, 0)));
                    courseNameTxt.setText((String) model.getValueAt(modelRow, 1));
                    courseCodeTxt.setText((String) model.getValueAt(modelRow, 2));
                    courseDescTxt.setText((String) model.getValueAt(modelRow, 3));
                    String instrName = (String) model.getValueAt(modelRow, 4);
                    if (instrName != null && !instrName.isEmpty()) instructorCmb.setSelectedItem(instrName);
                    else instructorCmb.setSelectedIndex(0);
                }
            }
        });

       
        loadCourses();
    }

    private void addField(String lbl, JComponent field, int y) {
        JLabel l = new JLabel(lbl);
        l.setBounds(20, y, 120, 25);
        field.setBounds(150, y, 250, 25);
        add(l); add(field);
    }

    private void addComboField(String lbl, JComboBox<String> cmb, int y) {
        JLabel l = new JLabel(lbl);
        l.setBounds(20, y, 120, 25);
        cmb.setBounds(150, y, 250, 25);
        add(l); add(cmb);
    }

    private void addButtons() {
        styleButton(addBtn, new Color(46, 204, 113), 450, 20);
        styleButton(updateBtn, new Color(52, 152, 219), 450, 70);
        styleButton(deleteBtn, new Color(231, 76, 60), 450, 120);
        styleButton(loadBtn, new Color(155, 89, 182), 450, 170);

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
        add(btn);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
    }

    private void filterTable() {
        String text = searchTxt.getText();
        if (text.trim().isEmpty()) sorter.setRowFilter(null);
        else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1));
    }

    private void loadInstructors() {
        try (Connection con = DB.getConnection()) {
            instructorCmb.removeAllItems();
            instructorMap.clear();
            instructorCmb.addItem(""); 
            ResultSet rs = con.createStatement().executeQuery("SELECT InstructorID, Name FROM Instructor");
            while (rs.next()) {
                int id = rs.getInt("InstructorID");
                String name = rs.getString("Name");
                instructorCmb.addItem(name);
                instructorMap.put(name, id);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    
    public void actionPerformed(ActionEvent e) {
        try (Connection con = DB.getConnection()) {
            if (e.getSource() == addBtn) {
                if (courseNameTxt.getText().isEmpty() || courseCodeTxt.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Course Name and Code are required!");
                    return;
                }
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO Course(CourseName, CourseCode, Description, InstructorID) VALUES(?, ?, ?, ?)",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(1, courseNameTxt.getText());
                ps.setString(2, courseCodeTxt.getText());
                ps.setString(3, courseDescTxt.getText());
                String instr = (String) instructorCmb.getSelectedItem();
                if (instr == null || instr.isEmpty()) ps.setNull(4, Types.INTEGER);
                else ps.setInt(4, instructorMap.get(instr));
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) courseIdTxt.setText(String.valueOf(keys.getInt(1)));
                JOptionPane.showMessageDialog(this, "Course Added!");
                loadCourses();
                clearFields();

            } else if (e.getSource() == updateBtn) {
                if (courseIdTxt.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Select a course!"); return; }
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE Course SET CourseName=?, CourseCode=?, Description=?, InstructorID=? WHERE CourseID=?");
                ps.setString(1, courseNameTxt.getText());
                ps.setString(2, courseCodeTxt.getText());
                ps.setString(3, courseDescTxt.getText());
                String instr = (String) instructorCmb.getSelectedItem();
                if (instr == null || instr.isEmpty()) ps.setNull(4, Types.INTEGER);
                else ps.setInt(4, instructorMap.get(instr));
                ps.setInt(5, Integer.parseInt(courseIdTxt.getText()));
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Course Updated!");
                loadCourses();
                clearFields();

            } else if (e.getSource() == deleteBtn) {
                if (courseIdTxt.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Select a course!"); return; }
                PreparedStatement ps = con.prepareStatement("DELETE FROM Course WHERE CourseID=?");
                ps.setInt(1, Integer.parseInt(courseIdTxt.getText()));
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Course Deleted!");
                loadCourses();
                clearFields();

            } else if (e.getSource() == loadBtn) {
                loadCourses();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void loadCourses() {
        try (Connection con = DB.getConnection()) {
            model.setRowCount(0);
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT c.CourseID, c.CourseName, c.CourseCode, c.Description, i.Name AS Instructor, c.CreatedAt " +
                "FROM Course c LEFT JOIN Instructor i ON c.InstructorID = i.InstructorID"
            );
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("CourseID"),
                        rs.getString("CourseName"),
                        rs.getString("CourseCode"),
                        rs.getString("Description"),
                        rs.getString("Instructor"),
                        rs.getTimestamp("CreatedAt")
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void clearFields() {
        courseIdTxt.setText("");
        courseNameTxt.setText("");
        courseCodeTxt.setText("");
        courseDescTxt.setText("");
        instructorCmb.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Course Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        frame.add(new CoursePanel());
        frame.setVisible(true);
    }
}
