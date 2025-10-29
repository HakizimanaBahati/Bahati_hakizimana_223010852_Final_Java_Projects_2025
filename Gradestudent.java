package com.panel;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;
import com.util.DB;

public class Gradestudent extends JPanel implements ActionListener {
    JTable table;
    DefaultTableModel model;
    JButton loadBtn = new JButton("Load");

    public Gradestudent() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 250));

       
        JPanel header = new JPanel();
        header.setBackground(new Color(30, 144, 255));
        header.setPreferredSize(new Dimension(1000, 60));
        JLabel title = new JLabel("Student Grades");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        header.add(title);
        add(header, BorderLayout.NORTH);

        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

      
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        styleButton(loadBtn, new Color(46, 204, 113));
        loadBtn.setPreferredSize(new Dimension(140, 40));
        topPanel.add(loadBtn);
        loadBtn.addActionListener(this);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        
        String[] columns = { "GradeID", "Assignment Title", "Grade Value", "Grade Letter", "Feedback", "Created At" };
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);
        table.setSelectionBackground(new Color(184, 207, 229));
        table.setSelectionForeground(Color.BLACK);

        
        JTableHeader headerTable = table.getTableHeader();
        headerTable.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerTable.setBackground(new Color(230, 230, 230));
        headerTable.setForeground(Color.DARK_GRAY);

       
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        
        JScrollPane sp = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        mainPanel.add(sp, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

       
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 250));
                } else {
                    c.setBackground(new Color(184, 207, 229));
                }
                return c;
            }
        });


        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                resizeColumns();
            }
        });
    }

    private void resizeColumns() {
        int totalWidth = table.getParent().getWidth();
        if (totalWidth <= 0) return;

        int[] colWeights = { 1, 3, 2, 2, 4, 3 }; 
        TableColumnModel colModel = table.getColumnModel();
        int totalWeight = 0;
        for (int w : colWeights) totalWeight += w;

        for (int i = 0; i < colModel.getColumnCount(); i++) {
            int colWidth = (int)((double)colWeights[i] / totalWeight * totalWidth);
            colModel.getColumn(i).setPreferredWidth(colWidth);
        }
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setFocusPainted(false);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { btn.setBackground(bgColor.darker()); }
            public void mouseExited(MouseEvent evt) { btn.setBackground(bgColor); }
        });
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loadBtn) loadGrades();
    }

    private void loadGrades() {
        try (Connection con = DB.getConnection()) {
            model.setRowCount(0);
            String sql = "SELECT g.GradeID, a.Title AS AssignmentTitle, g.GradeValue, g.GradeLetter, g.Feedback, g.CreatedAt " +
                         "FROM grade g " +
                         "JOIN assignment a ON g.AssignmentID = a.AssignmentID";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("GradeID"),
                        rs.getString("AssignmentTitle"),
                        rs.getDouble("GradeValue"),
                        rs.getString("GradeLetter"),
                        rs.getString("Feedback"),
                        rs.getString("CreatedAt")
                });
            }
            resizeColumns(); 
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading grades: " + ex.getMessage());
        }
    }
}
