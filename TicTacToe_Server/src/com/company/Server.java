package com.company;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Scanner;

public class Server implements eListener{

    char[][] map = new char[3][3];
    String command = "";
    Thread th2;
    int k = 0;
private final ArrayList<Connection> connections = new ArrayList<>();

    public Server() throws IOException {

        System.out.println("Server Running");
        try(ServerSocket serverSocket = new ServerSocket(3006)) {
            resetMap();
            showMap();
            th2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    Scanner write = new Scanner(System.in);
                    for (; ; ) {
                        if (write.hasNext()) {
                            command = write.nextLine();
                            System.out.println("cmd : " + command);
                            try {
                                exCommand(command);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            th2.start();

            for (;;){
                new Connection(this, serverSocket.accept());
            }
        }
    }


    @Override
    public void onConnectionReady(Connection connection) throws IOException {
        k++;
        if(k<3) {
            connections.add(connection);
            System.out.println("Connection accepted [" + connection + "]");
            sendNotif(connection, "Was connected to game" + connection + "]");
        }

        else{
            connection.sendMessage("Sorry Game is Full");
            connection.disconnect();
        }
    }

    @Override
    public void onReceiveString(Connection connection, String string) throws IOException {
        System.out.println("["+connection+"]: "+string);
        int x = Integer.parseInt(getX(string));
        int y = Integer.parseInt(getY(string));
        char k = getSign(string);
        map[x][y]=k;
        sendToClients(string);
        if(chekEnd(k)){
            if (checkDraw()){
                System.out.println("DRAW");
                   endGame();
            }
           else {
                endGame(connection);
            }
        }
    }

    @Override
    public void onDisconnect(Connection connection) throws IOException {
           // connection.disconnect();
            connections.remove(connection);
            System.out.println("Disconnected [" + connection+"]");
            sendToClients("Player [" + connection+"]" + "have disconnected");
            resetMap();
            sendToClients("NEWGAME");
            k--;
    }

    public void sendToClients(String message) throws IOException {
        for(Connection a: connections){
            System.out.println("was send message \"" + message+"\" To ["+a+"]");
            a.sendMessage(message);
        }
    }

    public void sendNotif(Connection connection, String message) throws IOException {
        for(Connection a: connections){
            if(a!=connection) a.sendMessage(message);
        }
    }

    public void endGame(Connection winner) throws IOException {
        winner.sendMessage("WIN");
        for(Connection a: connections){
            if(a!=winner) a.sendMessage("LOSE");
        }
        sendToClients("NEWGAME");
        resetMap();
    }

    public void endGame() throws IOException {
        sendToClients("DRAW");
        sendToClients("NEWGAME");
        resetMap();
    }




    public void resetMap(){
        for(int i = 0; i <3; i++){
            for(int j = 0; j <3; j++){
                map[i][j]=(char)(1);
            }
        }
    }

    private String getX(String string){
        return String.valueOf(string.charAt(0));
    }

    private String getY(String string){
        return String.valueOf(string.charAt(2));
    }

    private char getSign(String string){ char k;
        k = string.charAt(4);
        return k;
    }


    private void showMap(){
        System.out.println("----------");
        for(int i = 0; i <3; i++){
            System.out.print("| ");
            for(int j = 0; j <3; j++){
                System.out.print(map[i][j]+" ");
            }
            System.out.println("| ");
        }
        System.out.println("----------");
    }

    private boolean checkDiag(char symb){
        boolean check = true;
        for (int i = 0; i<3; i++){
            check = check & (map[i][i] == symb);
         //   System.out.println("Diag1 = "+ check);
        }
        boolean check2 = true;
        for (int i = 0; i<3; i++){
            check2 = check2 & (map[i][2-i] == symb);
           // System.out.println("Diag2 = "+ check2);
        }
        return check || check2;
    }

    private boolean checkLines(char symb){
        boolean check1, check2;
        for (int i=0; i<3; i++) {
            check1 = true;
            check2 = true;
            for (int j=0; j<3; j++) {
                check1 &= (map[i][j] == symb);
                check2 &= (map[j][i] == symb);
            }
            if (check1 || check2) return true;
        }
        return false;
    }

    private boolean checkDraw(){
        boolean check = true;
        for (int i=0; i<3; i++) {
            for (int j=0; j<3; j++) {
              if (map[i][j]==(char)1) check = false;
            }
        }
        return check;
    }


    private boolean chekEnd(char symb){
        boolean check1 = checkDiag(symb);
        boolean check2 = checkLines(symb);
        boolean check3 = checkDraw();
        //System.out.println("ResultOfDiags= " + check1+"ResultOfLines= " + check2 +"ResultOfDraw= " + check3);
        return check1 || check2 || check3;
    }

    private void exCommand(String cmd) throws IOException {
        if (cmd.equals("STOP")){
            sendToClients("From Server: Bye Bye...");
            System.exit(0);
        }
        else if (cmd.equals("RESTART")){
            sendToClients("NEWGAME");
            resetMap();
        }
       else if (cmd.equals("SHOWMAP")){
            showMap();
        }

       else if (cmd.equals("GETCONNECTIONNUMBER")){
           System.out.println(k);
        }

        else {
            sendToClients("From Server: "+cmd);
        }
    }

}
