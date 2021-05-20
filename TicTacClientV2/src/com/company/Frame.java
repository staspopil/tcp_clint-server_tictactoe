package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

public class Frame extends JFrame implements ActionListener, eListener {

    private static final String IP_ADDR = "192.168.8.145";
    private static final int PORT = 3006;
    private int WIDTH = 400;
    private int HEIGHT = 600;
    Connection connection;
    JButton[][] buts = new JButton[3][3];
    JTextArea log = new JTextArea("LOG\n");
    JButton button= new JButton("Hall");
    JPanel GamePanel = new JPanel();
    JPanel TextPanel = new JPanel();
    JPanel NameSign = new JPanel();
    JTextArea Name = new JTextArea("Vasea");
    JTextArea Sign = new JTextArea("x");
    Socket socket = new Socket(IP_ADDR,PORT);
    JScrollPane scrollpane = new JScrollPane(log,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    public Frame() throws IOException {
        this.setLayout(new GridLayout(2,1));
        GamePanel.setLayout(new GridLayout(3,3));
        TextPanel.setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH,HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        log.setEditable(false);
        log.setLineWrap(true);//Автоматический перенос
        //gamepanel
        add(GamePanel);
        showButts(GamePanel);

        add(TextPanel);
        TextPanel.add(scrollpane,BorderLayout.CENTER);
        TextPanel.add(NameSign,BorderLayout.PAGE_START);
        NameSign.add(Name, BorderLayout.PAGE_START);
        NameSign.add(Sign, BorderLayout.PAGE_START);
        setVisible(true);
        connection = new Connection(this,socket);
    }


    public void showButts(JPanel panel){
        int x = 10;
        int y = 10;
        for (int i = 0; i<3; i++){
            for (int j = 0; j<3; j++){
                buts[i][j] = new JButton(String.valueOf((char)1));
                buts[i][j].setBounds(x,y,100,100);
                x+=100;
                panel.add(buts[i][j]);
                buts[i][j].addActionListener(this);
            }
            x=10;
            y+=100;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for(int i = 0; i<3;i++) {
            for(int j = 0; j<3;j++) {
                if (e.getSource() == buts[i][j]) {
                    try {
                        connection.sendMessage(i+" "+j+" "+Sign.getText()+" "+Name.getText());
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onConnectionReady(Connection connection) {
        printMessage("U connected to the game, choose the sign,and write the name");
    }

    @Override
    public void onReceiveString(Connection connection, String string) throws IOException {
        if (string.equals("WIN")){
            printMessage("You Won, gj");
        }

       else if (string.equals("LOSE")){
            printMessage("Looser, you lost");
        }

        else if(string.equals("DRAW")){
            printMessage("\nDRAW");
        }

       else if(string.equals("NEWGAME")){
           printMessage("\nNEWGAME");
           resetButs();
        }


        else if (string.charAt(1)==' ')  {
            int x = Integer.parseInt(getX(string));
            int y = Integer.parseInt(getY(string));
            printMessage("Player [" + getNameFromString(string) + "] Moves " + getX(string) + " " + getY(string) + " " + getSign(string));
            buts[x][y].setText(getSign(string));
        }

        else {
            printMessage(string);
        }
    }

    @Override
    public void onDisconnect(Connection connection) throws IOException {
        connection.disconnect();
    }

    private synchronized void printMessage(String mes){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(mes+"\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    private String getNameFromString(String s){
        String name = null;
        int end = s.lastIndexOf(' ');
        name = s.substring(end,s.length());
        return name;
    }


    private void resetButs(){
        for(int i = 0; i<3; i++){
            for(int j = 0; j<3; j++){
                buts[i][j].setText(String.valueOf((char)1));
            }
        }
    }


    private String getX(String string){
        return String.valueOf(string.charAt(0));
    }

    private String getY(String string){
        return String.valueOf(string.charAt(2));
    }

    private String getSign(String string){
        String k ="";
         k = k+string.charAt(4);
        return k;
    }
}
