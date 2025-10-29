package com.panel;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;
import com.util.DB;

public class EnrollmentStudent extends JPanel implements ActionListener {
    JButton loadBtn = new JButton("Load");
    JTable table;
    DefaultTableModel model;

    public EnrollmentStudent() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 250));

       
        JPanel header = new JPanel();
        header.setBackground(new Color(30, 144, 255));
        header.setPreferredSize(new Dimension(1000, 60));
        JLabel title = new JLabel("Enrollment Information");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        header.add(title);
        add(header, BorderLayout.NORTH);

        
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        styleButton(loadBtn, new Color(46, 204, 113));
        topPanel.add(loadBtn);
        loadBtn.addActionListener(this);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        String[] columns = { "EnrollmentID", "Student Name", "Course Name", "EnrollDate", "Status", "Remarks" };
        model = new DefaultTableModel(columns, 0) {
            
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(230, 230, 230));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
           
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 250));
                }
                return c;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        mainPanel.add(sp, BorderLayout.CENTER); 

        add(mainPanel, BorderLayout.CENTER); 
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setFocusPainted(false);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
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

   
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loadBtn) {
            loadEnrollments();
        }
    }

    private void loadEnrollments() {
        try (Connection con = DB.getConnection()) {
            model.setRowCount(0); 
            String sql = "SELECT e.EnrollmentID, " +
                         "s.FirstName, s.LastName, " +
                         "c.CourseName, e.EnrollDate, e.Status, e.Remarks " +
                         "FROM enrollment e " +
                         "JOIN student s ON e.StudentID = s.StudentID " +
                         "JOIN course c ON e.CourseID = c.CourseID";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                String studentName = rs.getString("FirstName") + " " + rs.getString("LastName");
                model.addRow(new Object[]{
                    rs.getInt("EnrollmentID"),
                    studentName,
                    rs.getString("CourseName"),
                    rs.getDate("EnrollDate"),
                    rs.getString("Status"),
                    rs.getString("Remarks")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading enrollments: " + ex.getMessage());
        }
    }
}
