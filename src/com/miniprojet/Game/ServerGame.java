package com.miniprojet.Game;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ServerGame extends JFrame implements Runnable{

    Socket socketClient;
    List<Socket> clients = new ArrayList<>(2);
    public static int N=10,NW=3,scoreX,scoreO;
    public static JFrame fScore;
    private int count;
    static  int runCounter;
    public  Collection<String> listChoix = new ArrayList<>();
    public static String path = System.getProperty("user.dir")+"/src/";

    public ServerGame(int port,int size  ,int number_win) {
        try{
            this.N = size;
            this.NW = number_win;

            ServerSocket serverSocket = new ServerSocket(port);

            while (true){
                socketClient = serverSocket.accept();

                new Thread(this).start();
            }

        }catch(Exception e){
            System.out.println(e);
        }
    }

    //Methode pour trouver les combinaisons gagnantes
    private void checkWin(int x, int y, String X){
        int[] t = {1,0,1,1,-1};
        int i,j=0;
        count=0;

        while(j<t.length-1) {
            i=-(x+(NW-1));
            do{
                setScore((x + t[j] * i), (y + t[j+1] * i++),X);
            }while (i < (x+NW) );
            j++;
        }


    }

    private void setScore(int r, int c, String X) {
        Boolean isWin = false;
        //Vérifier si le choix est gagné
        if(listChoix.contains(r+"-"+c+";"+X)){
            if(++count==NW ) {
                isWin = true;
                if(X.equals("X")){
                    scoreX++;
                }
                if(X.equals("O")){
                    scoreO++;
                }
            }
        }
        else {
            count=0;
            isWin = false;
        }

        //Afficher le score et lancer le clip
        if(isWin) {
            this.setAlwaysOnTop(true);
            playSound("clip.wav");
            txtScoreX.setText(""+scoreX);
            txtScoreO.setText(""+scoreO);
        }

    }
    //Methode pour envoyer le choix au tous les joueurs
    public void sendToAll(String message){
        synchronized (clients){
            final int[] x = new int[1];
            final int[] y = new int[1];
            final String[] ch = new String[1];

            clients.forEach(socket -> {
                try {
                    if(socket!=null) {
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                        listChoix.add(message);
                        out.writeUTF(message);

                        String name = message.split(";")[0];//i-j
                        ch[0] = message.split(";")[1];//X
                        x[0] = Integer.parseInt(name.split("-")[0]);//i
                        y[0] = Integer.parseInt(name.split("-")[1]);//j
                    }
                } catch (IOException e) {
                    System.out.println(e);
                }
            });
            checkWin(x[0], y[0], ch[0]);
        }
    }

    //Methode pour lancer le clip aprés gagné

    public static synchronized void playSound(String soundName) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Clip clip = AudioSystem.getClip();
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(path+soundName).getAbsoluteFile());
                    clip.open(audioInputStream);
                    clip.start();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }).start();
    }

    @Override
    public void run() {
        try {
            DataInputStream din = new DataInputStream(socketClient.getInputStream());
            clients.add(socketClient);
            while (din!=null) {
                sendToAll(din.readUTF());
            }
        } catch (IOException e) {
            System.out.println("client sortie "+e);
        }

    }


    public static void main(String[] args) {

            createForm();

    }

    //Attribues de Partie formulaire
    public static TextField txtPort ,txtNbWin,txtSize,txtHost,txtScoreX,txtScoreO;
    public static Button btnOpen;
    public static Checkbox cbX,cbO;
    private static void createForm(){

        JFrame formStart = new JFrame("Tic Tac Toe By Java ");
        formStart.setBounds(400,200,320,300);
        formStart.setLayout(new GridLayout(8,1));
        formStart.setIconImage(new ImageIcon(path+"icon.png").getImage());

        //Partie de configuration du serveur
        
        Label lblTitle = new Label("Serveur Du Jeux",Label.CENTER);
        lblTitle.setFont(new Font("TimesRoman", Font.BOLD, 20));

        Panel pHeaderServer = new Panel(new FlowLayout(FlowLayout.CENTER));
        pHeaderServer.setBackground(new Color(238, 110, 115));
        pHeaderServer.setFont(new Font("TimesRoman", Font.BOLD, 12));
        pHeaderServer.setForeground(Color.white);
        pHeaderServer.add(lblTitle);

        Label lblPort = new Label("Port de jeux :",Label.LEFT);
        txtPort = new TextField("",20);
        txtPort.setBackground(new Color(16, 204, 153));
        
        Panel pPort = new Panel(new FlowLayout(FlowLayout.RIGHT));
        pPort.setBackground(new Color(16, 204, 153));
        pPort.setFont(new Font("TimesRoman", Font.BOLD, 12));
        pPort.setForeground(Color.white);
        pPort.add(lblPort);
        pPort.add(txtPort);
        
        Label lblSize = new Label("Nombre column :",Label.LEFT);
        txtSize = new TextField("",20);
        txtSize.setBackground(new Color(16, 204, 153));
        
        Panel pNbCol = new Panel(new FlowLayout(FlowLayout.RIGHT));
        pNbCol.setBackground(new Color(16, 204, 153));
        pNbCol.setFont(new Font("TimesRoman", Font.BOLD, 12));
        pNbCol.setForeground(Color.white);
        pNbCol.add(lblSize);
        pNbCol.add(txtSize);
        
        Label lblNbWin = new Label("Total pour gagner :",Label.LEFT);
        txtNbWin = new TextField("",20);
        txtNbWin.setBackground(new Color(16, 204, 153));
        
        Panel pNbWin = new Panel(new FlowLayout(FlowLayout.RIGHT));
        pNbWin.setBackground(new Color(16, 204, 153));
        pNbWin.setFont(new Font("TimesRoman", Font.BOLD, 12));
        pNbWin.setForeground(Color.white);
        pNbWin.add(lblNbWin);
        pNbWin.add(txtNbWin);
        
        Button btnStart = new Button("Demarrer Serveur ");
        btnStart.setFont(new Font("TimesRoman", Font.BOLD, 14));
        btnStart.setBackground(new Color(255, 195, 109));
        btnStart.setName("start");
        btnStart.addActionListener(btnClick());
        
        Panel pBtnStart = new Panel(new FlowLayout(FlowLayout.CENTER));
        pBtnStart.setBackground(new Color(255, 195, 109));
        pBtnStart.setForeground(Color.DARK_GRAY);
        pBtnStart.add(btnStart);

        //Partie de configuration du jeu

        Panel pCheckbox = new Panel(new FlowLayout(FlowLayout.CENTER));
        pCheckbox.setBackground(new Color(16, 204, 153));
        pCheckbox.setForeground(Color.DARK_GRAY);

        Panel pRadio = new Panel(new FlowLayout(FlowLayout.CENTER));
        pRadio.setBackground(new Color(16, 204, 153));
        pRadio.setFont(new Font("TimesRoman", Font.BOLD, 12));
        pRadio.setForeground(Color.white);

        CheckboxGroup cb=new CheckboxGroup();
        cbX=new Checkbox ("X", cb, true);
        cbO=new Checkbox ("O", cb, false);
        pRadio.add(cbX);
        pRadio.add(cbO);
        pCheckbox.add(pRadio,0);

        Label lblHost = new Label("Ip adresse :",Label.LEFT);
        txtHost = new TextField("",20);
        txtHost.setBackground(new Color(16, 204, 153));

        Panel pHost = new Panel(new FlowLayout(FlowLayout.RIGHT));
        pHost.setBackground(new Color(16, 204, 153));
        pHost.setFont(new Font("TimesRoman", Font.BOLD, 12));
        pHost.setForeground(Color.white);
        pHost.add(lblHost);
        pHost.add(txtHost);

        btnOpen = new Button("Lancer Jeux");
        btnOpen.setFont(new Font("TimesRoman", Font.BOLD, 14));
        btnOpen.setBackground(new Color(255, 195, 109));
        btnOpen.setName("open");
        btnOpen.setEnabled(false);
        btnOpen.addActionListener(btnOpenClick());

        Panel pBtnOpen = new Panel(new FlowLayout(FlowLayout.CENTER));
        pBtnOpen.setBackground(new Color(255, 195, 109));
        pBtnOpen.setFont(new Font("TimesRoman", Font.BOLD, 12));
        pBtnOpen.setForeground(Color.DARK_GRAY);
        pBtnOpen.add(btnOpen,0);

        formStart.add(pHeaderServer,0);
        formStart.add(pPort,1);
        formStart.add(pNbCol,2);
        formStart.add(pNbWin,3);
        formStart.add(pBtnStart,4);
        formStart.add(pHost,5);
        formStart.add(pRadio,6);
        formStart.add(pBtnOpen,7);
        
        formStart.setDefaultCloseOperation(EXIT_ON_CLOSE);
        formStart.setVisible(true);

    }

    //Methode Pour Lancer le serveur
    private static ActionListener btnClick() {
        return  new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Button b = (Button)e.getSource();
                if(b.getName()=="start"){
                    b.setBackground(new Color(87, 184, 70));
                    b.setName("stop");
                    b.setLabel("En Demarrage");
                    btnOpen.setEnabled(true);

                  new Thread(){
                      @Override
                      public void run() {
                          try {
                              int Port = Integer.parseInt(txtPort.getText());
                              int Size = Integer.parseInt(txtSize.getText());
                              int num_win = Integer.parseInt(txtNbWin.getText());

                              new ServerGame(Port, Size, num_win);

                          }catch (Exception e){

                              txtPort.setText("4478");
                              txtSize.setText("10");
                              txtNbWin.setText("3");
                              txtPort.setEnabled(false);
                              txtSize.setEnabled(false);
                              txtNbWin.setEnabled(false);

                              new ServerGame(4478, 10, 3);
                          }
                      }
                  }.start();
                }
            }
        };
    }

    //Methote Pour lancer le jeu
    private static ActionListener btnOpenClick() {
        return  new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Button b = (Button)e.getSource();
                if(b.getName()=="open"){
                    String Choix;
                    if(cbO.getState()==true) {
                        Choix="O";
                        cbX.setState(true);
                        cbO.setState(false);
                    }
                    else {
                        Choix="X";
                        cbX.setState(false);
                        cbO.setState(true);
                    }
                    cbO.setEnabled(false);
                    cbX.setEnabled(false);

                    if(txtHost.getText().isEmpty()){
                        txtHost.setText("127.0.0.1");
                    }

                    String[] args = {txtHost.getText(),txtPort.getText(),Choix};
                    StartGame.main(args);

                    if(++runCounter==2) {
                        scoreBord();
                        b.setEnabled(false);
                    }
                }
            }
        };
    }

    private static void scoreBord(){

        fScore = new JFrame("score Bord");
        fScore.setBounds(0,0,200,100);
        fScore.setIconImage(new ImageIcon(path+"icon.png").getImage());
        
        Label lblScoreX = new Label("Joueur X : ",Label.LEFT);
        txtScoreX = new TextField("0",5);
        txtScoreX.setEnabled(false);
        txtScoreX.setBackground(new Color(16, 204, 153));
        
        Panel pScoreX = new Panel(new FlowLayout(FlowLayout.RIGHT));
        pScoreX.setBackground(new Color(16, 204, 153));
        pScoreX.setFont(new Font("TimesRoman", Font.BOLD, 12));
        pScoreX.setForeground(Color.white);
        pScoreX.add(lblScoreX);
        pScoreX.add(txtScoreX);
        
        Label lblScoreO = new Label("Joueur O : ",Label.LEFT);
        txtScoreO = new TextField("0",5);
        txtScoreO.setEnabled(false);
        txtScoreO.setBackground(new Color(16, 204, 153));
        
        Panel pScoreO = new Panel(new FlowLayout(FlowLayout.RIGHT));
        pScoreO.setBackground(new Color(16, 204, 153));
        pScoreO.setFont(new Font("TimesRoman", Font.BOLD, 12));
        pScoreO.setForeground(Color.white);
        pScoreO.add(lblScoreO);
        pScoreO.add(txtScoreO);

        fScore.setLayout(new GridLayout(2,1));
        fScore.add(pScoreX,0);
        fScore.add(pScoreO,1);
        fScore.setVisible(true);

    }
}
