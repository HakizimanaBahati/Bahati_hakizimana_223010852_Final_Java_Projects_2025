package com.form;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.panel.*;

public class EMS extends JFrame {
    private JPanel sideMenu, mainPanel;
    private CardLayout cardLayout = new CardLayout();
    private JButton activeButton = null;
    private boolean menuExpanded = true;
    private int menuWidthExpanded = 220;
    private int menuWidthCollapsed = 60;

    public EMS(String role, int userId) {
        setTitle("Education Management System");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Side menu
        sideMenu = new JPanel();
        sideMenu.setLayout(new GridBagLayout());
        sideMenu.setBackground(new Color(30, 144, 255));
        sideMenu.setPreferredSize(new Dimension(menuWidthExpanded, getHeight()));

        // Toggle button
        JButton toggleBtn = new JButton("≡");
        toggleBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        toggleBtn.setForeground(Color.WHITE);
        toggleBtn.setFocusPainted(false);
        toggleBtn.setBackground(new Color(30, 144, 255));
        toggleBtn.setBorder(null);
        toggleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleBtn.addActionListener(e -> toggleMenu());

        GridBagConstraints gbcMenu = new GridBagConstraints();
        gbcMenu.gridx = 0;
        gbcMenu.gridy = 0;
        gbcMenu.gridwidth = 1;
        gbcMenu.insets = new Insets(5, 0, 10, 0);
        sideMenu.add(toggleBtn, gbcMenu);

        // Main panel
        mainPanel = new JPanel(cardLayout);

        int gridY = 1;
        if (role.equalsIgnoreCase("admin")) {
            gridY = addMenuItem("Users", new UserPanel(), gridY);
            gridY = addMenuItem("Instructor", new InstructorPanel(), gridY);
            gridY = addMenuItem("Courses", new CoursePanel(), gridY);
            gridY = addMenuItem("Students", new StendentPanen(), gridY);
            gridY = addMenuItem("Assignments", new AssignmentPanel(), gridY);
            gridY = addMenuItem("Enrollments", new EnrollmentPanel(), gridY);
            gridY = addMenuItem("Grades", new GradePanel(), gridY);
            gridY = addMenuItem("Student Grades", new Gradestudent(), gridY);
            gridY = addMenuItem("Enrolled Students", new EnrollmentStudent(), gridY);
            gridY = addMenuItem("Logout", new LogoutForm(), gridY);
        } else if (role.equalsIgnoreCase("instructor")) {
            gridY = addMenuItem("Courses", new CoursePanel(), gridY);
            gridY = addMenuItem("Assignments", new AssignmentPanel(), gridY);
            gridY = addMenuItem("Enrollments", new EnrollmentPanel(), gridY);
            gridY = addMenuItem("Grades", new GradePanel(), gridY);
            gridY = addMenuItem("Logout", new LogoutForm(), gridY);
        } else if (role.equalsIgnoreCase("student")) {
            gridY = addMenuItem("My Grades", new Gradestudent(), gridY);
            gridY = addMenuItem("Enrolled Courses", new EnrollmentStudent(), gridY);
            gridY = addMenuItem("Logout", new LogoutForm(), gridY);//
        }

        add(sideMenu, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        setVisible(true);
    }

   
    private int addMenuItem(String name, JPanel panel, int gridY) {
        JButton btn = new JButton(name);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(30, 144, 255));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 20, 10, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effects
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                if (btn != activeButton) btn.setBackground(new Color(0, 120, 215));
            }

            public void mouseExited(MouseEvent evt) {
                if (btn != activeButton) btn.setBackground(new Color(30, 144, 255));
            }
        });

      
        btn.addActionListener(e -> {
            cardLayout.show(mainPanel, name);
            setActiveButton(btn);
        });

        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        sideMenu.add(btn, gbc);

        
        mainPanel.add(panel, name);

        
        if (activeButton == null) setActiveButton(btn);

        return gridY + 1;
    }

    private void setActiveButton(JButton btn) {
        if (activeButton != null)
            activeButton.setBackground(new Color(30, 144, 255));
        activeButton = btn;
        activeButton.setBackground(new Color(0, 102, 204));
    }

    private void toggleMenu() {
        menuExpanded = !menuExpanded;
        int width = menuExpanded ? menuWidthExpanded : menuWidthCollapsed;
        sideMenu.setPreferredSize(new Dimension(width, getHeight()));

        for (Component comp : sideMenu.getComponents()) {
            if (comp instanceof JButton && comp != sideMenu.getComponent(0)) {
                JButton btn = (JButton) comp;
                btn.setHorizontalAlignment(menuExpanded ? SwingConstants.LEFT : SwingConstants.CENTER);
                btn.setText(menuExpanded ? btn.getText() : "");
            }
        }
        sideMenu.revalidate();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EMS("admin", 1));
    }
}
