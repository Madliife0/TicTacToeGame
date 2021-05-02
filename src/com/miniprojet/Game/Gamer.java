package com.miniprojet.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;

public class Gamer implements Runnable {
    Socket socketClient;
    final String X;
    public JFrame formGamer;
    Panel pButtons;
    public int N,NW;
    Label labelGamer;
    Collection<String> listChoix = new ArrayList<>();

    public Gamer(String host,int port,String character ) {
            this.X = character;
            this.N = ServerGame.N;
            this.NW = ServerGame.NW;
            try {
            socketClient = new Socket(host,port);
        } catch (IOException e) {
            System.out.println(e);
        }
        createForm(N,X);

       new Thread(this).start();

    }

    void createForm(int N,String X){

        formGamer = new JFrame("Tic Tac Toe By Java");
        formGamer.setLayout(new BorderLayout());
        formGamer.setForeground(Color.WHITE);
        formGamer.setIconImage(new ImageIcon(ServerGame.path+"icon.png").getImage());

        Panel pGamer = new Panel(new FlowLayout(0));
        pGamer.setBackground(new Color(255, 195, 109));
        pGamer.setForeground(Color.DARK_GRAY);

        labelGamer = new Label("",Label.CENTER);
        labelGamer.setFont(new Font("TimesRoman", Font.BOLD, 18));
        if(X.equals("X")) {
            labelGamer.setText("Joueur "+X);
            formGamer.setBounds(20,200,400,400);
        }else {
            labelGamer.setText("Joueur O");
            formGamer.setBounds(800,200,400,400);
        }
        pGamer.add(labelGamer);

        pButtons = new Panel(new GridLayout(N,N));
        pButtons.setBackground(new Color(255, 195, 109));
        pButtons.setForeground(Color.DARK_GRAY);

        for (int i=0 ; i<N;i++){
            for (int j=0 ; j<N;j++){
                Button btn = new Button("");
                btn.setName(i+"-"+j);
                btn.setFont(new Font("TimesRoman", Font.PLAIN, 14));
                btn.setBackground(new Color(16, 204, 153));
                btn.setForeground(Color.WHITE);
                btn.addActionListener(btnClick(i,j));
                pButtons.add(btn,j);
            }
        }

        formGamer.add(pGamer,BorderLayout.NORTH);
        formGamer.add(pButtons,BorderLayout.CENTER);
        formGamer.setVisible(true);

    }

    private ActionListener btnClick(int i, int j) {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    DataOutputStream dout = new DataOutputStream(socketClient.getOutputStream());
                    Button b = (Button)e.getSource();

                    if(b.getLabel().isEmpty()){
                        dout.writeUTF(i+"-"+j+";"+X);
                        b.setLabel(X);

                        activated(false);
                        listChoix.add(i+"-"+j+";"+X);

                        if(isRemplir()){
                            if(JOptionPane.showConfirmDialog(formGamer,"Fin de jeu ")==0){
                            System.exit(0);
                            }
                        }
                    }
                }catch (Exception ex){
                    System.out.println("btnEvent"+ex);
                }
            }
        };
    }

    private synchronized void activated(Boolean active){

        for (int i=0 ; i<pButtons.getComponentCount();i++){
            Button b = ((Button)pButtons.getComponent(i));
            b.setEnabled(active);

            if(active)
                b.setBackground(new Color(16, 204, 153));
            else
                b.setBackground(new Color(238, 110, 115));
        }
    }

    private boolean isRemplir(){
        int nbVide=0;
        for (int i=0 ; i<pButtons.getComponentCount();i++){
            Button b = ((Button)pButtons.getComponent(i));
           if(b.getLabel().equals("")){
               nbVide++;
           }
        }

        if(nbVide==0)
            return true;
        else
            return false;
    }

    private synchronized void setChoix(String choix){

        String name = choix.split(";")[0];//i-j
        String ch = choix.split(";")[1];//X

        for (int i=0 ; i<pButtons.getComponentCount();i++){
            Button b = ((Button)pButtons.getComponent(i));
            Font font= b.getFont();
            b.setFont(font.deriveFont((float)(formGamer.getSize().getWidth()/20 * 1)));

            if(b.getName().equals(name)){
                if(b.getLabel().isEmpty()) {
                    b.setLabel(ch);
                    if(ch==X)
                        activated(true);
                    else
                        activated(true);
                }
            }
        }
    }

    @Override
    public void run() {
        while(true){
            try{
                DataInputStream in = new DataInputStream(socketClient.getInputStream());
                setChoix(in.readUTF());

            } catch (IOException e) {
                System.out.println(e);
            }

        }
    }

    }

class StartGame {

    public static void main(String[] args) {

        new Gamer(args[0], Integer.parseInt(args[1]), args[2]);

    }
}
