package me.coolmint.toolbox;

import javax.swing.*;

public class Tool extends JFrame {
    public JTabbedPane createTabbedPane(){
        JTabbedPane pane = new JTabbedPane();

        pane.add("Welcome", new Welcome());
        pane.add("Hwid", new hwid());
        //TODO: auth 서버 json 에 저장하는거 만들기
        //pane.add("AuthServer", new AuthServer());

        return pane;
    }

    public Tool(){
        setTitle("NGM-Toolbox");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 150);
        setLocationRelativeTo(null);
        setResizable(false);

        add(createTabbedPane());

        setVisible(true);
    }

    public static void main(String[] args){
        new Tool();
    }
}
