package me.coolmint.toolbox;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static me.coolmint.ngm.util.client.Auth.getHwid;

public class hwid extends JPanel implements ActionListener {
    JButton copy = new JButton("Copy");

    public hwid(){
        copy.addActionListener(this);

        add(new JLabel(getHwid()));
        add(copy);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==copy) {
            StringSelection data = new StringSelection(getHwid());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(data, data);
        }
    }
}
