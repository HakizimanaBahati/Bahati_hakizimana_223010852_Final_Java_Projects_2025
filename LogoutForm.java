package com.form;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.form.Loginform;

public class LogoutForm extends JPanel {

    private JButton logoutBtn = new JButton("Logout");

    public LogoutForm() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        styleButton(logoutBtn, new Color(231, 76, 60));

        logoutBtn.addActionListener(e -> {
            
            SwingUtilities.getWindowAncestor(this).dispose();
            
            new Loginform();
        });

        add(logoutBtn);
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setFocusPainted(false);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setPreferredSize(new Dimension(150, 50));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { btn.setBackground(bgColor.darker()); }
            public void mouseExited(MouseEvent evt) { btn.setBackground(bgColor); }
        });
    }
}
