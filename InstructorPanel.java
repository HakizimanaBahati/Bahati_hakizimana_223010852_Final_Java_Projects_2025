package com.panel;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Date;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import com.util.DB;

public class InstructorPanel extends JPanel implements ActionListener {

    private JTextField idTxt = new JTextField();
    private JTextField nameTxt = new JTextField();
    private JTextField identifierTxt = new JTextField();
    private JTextField statusTxt = new JTextField();
    private JTextField locationTxt = new JTextField();
    private JTextField contactTxt = new JTextField();
    private JSpinner assignedSinceSpinner = new JSpinner(new SpinnerDateModel());

    private JTextField searchTxt = new JTextField();

    private JButton addBtn = new JButton("Add");
    private JButton updateBtn = new JButton("Update");
    private JButton deleteBtn = new JButton("Delete");
    private JButton loadBtn = new JButton("Load");

    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    public InstructorPanel() {
        setLayout(null);
        setBackground(Color.WHITE);

        JLabel header = new JLabel("Welcome to the Instructor Panel");
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(new Color(30, 144, 255));
        header.setBounds(20, 10, 400, 30);
        add(header);

        // Search
        JLabel searchLabel = new JLabel("Search by Name:");
        searchLabel.setBounds(20, 360, 120, 25);
        add(searchLabel);

        searchTxt.setBounds(150, 360, 250, 25);
        add(searchTxt);
        searchTxt.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

        String[] columns = {"InstructorID", "Name", "Identifier", "Status", "Location", "Contact", "AssignedSince"};
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
                if (!isSelected) c.setBackground(row % 2 == 0 ? new Color(245, 245, 250) : Color.WHITE);
                setHorizontalAlignment(column == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
                return c;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(20, 400, 900, 300);
        add(sp);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        int y = 60;
        addField("Instructor ID", idTxt, y); idTxt.setEditable(false); y += 40;
        addField("Name", nameTxt, y); y += 40;
        addField("Identifier", identifierTxt, y); y += 40;
        addField("Status", statusTxt, y); y += 40;
        addField("Location", locationTxt, y); y += 40;
        addField("Contact", contactTxt, y); y += 40;

        JLabel assignedLabel = new JLabel("Assigned Since");
        assignedLabel.setBounds(20, y, 120, 25);
        assignedSinceSpinner.setBounds(150, y, 250, 25);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(assignedSinceSpinner, "yyyy-MM-dd");
        assignedSinceSpinner.setEditor(editor);
        assignedSinceSpinner.setValue(new Date());
        add(assignedLabel);
        add(assignedSinceSpinner);
        y += 50;

        styleButton(addBtn, new Color(46, 204, 113), 450, 20);
        styleButton(updateBtn, new Color(52, 152, 219), 450, 70);
        styleButton(deleteBtn, new Color(231, 76, 60), 450, 120);
        styleButton(loadBtn, new Color(155, 89, 182), 450, 170);

        addBtn.addActionListener(this);
        updateBtn.addActionListener(this);
        deleteBtn.addActionListener(this);
        loadBtn.addActionListener(this);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int modelRow = table.convertRowIndexToModel(row);
                    idTxt.setText(String.valueOf(model.getValueAt(modelRow, 0)));
                    nameTxt.setText((String) model.getValueAt(modelRow, 1));
                    identifierTxt.setText((String) model.getValueAt(modelRow, 2));
                    statusTxt.setText((String) model.getValueAt(modelRow, 3));
                    locationTxt.setText((String) model.getValueAt(modelRow, 4));
                    contactTxt.setText((String) model.getValueAt(modelRow, 5));

                    Object dateValue = model.getValueAt(modelRow, 6);
                    if (dateValue != null) {
                        assignedSinceSpinner.setValue(dateValue);
                    } else {
                        assignedSinceSpinner.setValue(new Date());
                    }
                }
            }
        });

        loadInstructors();
    }

    private void addField(String lbl, JComponent field, int y) {
        JLabel l = new JLabel(lbl);
        l.setBounds(20, y, 120, 25);
        field.setBounds(150, y, 250, 25);
        add(l);
        add(field);
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
            model.setRowCount(0);
            String sql = "SELECT * FROM Instructor";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("InstructorID"),
                        rs.getString("Name"),
                        rs.getString("Identifier"),
                        rs.getString("Status"),
                        rs.getString("Location"),
                        rs.getString("Contact"),
                        rs.getDate("AssignedSince")
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addBtn) addInstructor();
        else if (e.getSource() == updateBtn) updateInstructor();
        else if (e.getSource() == deleteBtn) deleteInstructor();
        else if (e.getSource() == loadBtn) loadInstructors();
    }

    private void addInstructor() {
        if (nameTxt.getText().trim().isEmpty() ||
            identifierTxt.getText().trim().isEmpty() ||
            statusTxt.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, Identifier, and Status are required!");
            return;
        }

        try (Connection con = DB.getConnection()) {
            PreparedStatement psCheck = con.prepareStatement("SELECT COUNT(*) FROM Instructor WHERE Identifier=?");
            psCheck.setString(1, identifierTxt.getText().trim());
            ResultSet rsCheck = psCheck.executeQuery();
            if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Identifier must be unique!");
                return;
            }

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO Instructor(Name, Identifier, Status, Location, Contact, AssignedSince) VALUES(?, ?, ?, ?, ?, ?)"
            );
            ps.setString(1, nameTxt.getText().trim());
            ps.setString(2, identifierTxt.getText().trim());
            ps.setString(3, statusTxt.getText().trim());
            ps.setString(4, locationTxt.getText().trim());
            ps.setString(5, contactTxt.getText().trim());
            ps.setDate(6, new java.sql.Date(((Date) assignedSinceSpinner.getValue()).getTime()));

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Instructor Added Successfully!");
            loadInstructors();
            clearFields();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void updateInstructor() {
        if (idTxt.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select an instructor first!");
            return;
        }

        try (Connection con = DB.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE Instructor SET Name=?, Identifier=?, Status=?, Location=?, Contact=?, AssignedSince=? WHERE InstructorID=?"
            );
            ps.setString(1, nameTxt.getText().trim());
            ps.setString(2, identifierTxt.getText().trim());
            ps.setString(3, statusTxt.getText().trim());
            ps.setString(4, locationTxt.getText().trim());
            ps.setString(5, contactTxt.getText().trim());
            ps.setDate(6, new java.sql.Date(((Date) assignedSinceSpinner.getValue()).getTime()));
            ps.setInt(7, Integer.parseInt(idTxt.getText()));

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Instructor Updated Successfully!");
            loadInstructors();
            clearFields();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void deleteInstructor() {
        if (idTxt.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select an instructor first!");
            return;
        }

        try (Connection con = DB.getConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM Instructor WHERE InstructorID=?");
            ps.setInt(1, Integer.parseInt(idTxt.getText()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Instructor Deleted Successfully!");
            loadInstructors();
            clearFields();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void clearFields() {
        idTxt.setText("");
        nameTxt.setText("");
        identifierTxt.setText("");
        statusTxt.setText("");
        locationTxt.setText("");
        contactTxt.setText("");
        assignedSinceSpinner.setValue(new Date());
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Instructor Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        frame.add(new InstructorPanel());
        frame.setVisible(true);
    }
}
