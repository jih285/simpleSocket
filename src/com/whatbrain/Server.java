package com.whatbrain;

import java.net.*;
import java.io.*;
import java.util.*;

public class Server
{
    int port;
    List<Socket> clients;
    ServerSocket server;
    List<room> rooms;

    public class room{
        private String room_name;
        private List<Socket> members;
        public String chatlog;
        public room(String name){
            this.room_name=name;
            this.members= new ArrayList<Socket>();
            this.chatlog="";
        }
        public int size(){
            return this.members.size();
        }
        public void add_member(Socket member)
        {
            this.members.add(member);
        }

        public void remove_member(Socket member)
        {
            members.remove(member);
        }

        public String getRoom_name() {
            return room_name;
        }
        public Socket getMember(int index){
            return this.members.get(index);

        }
        public boolean ifMemberExisit(Socket member)
        {
            return this.members.contains(member);
        }

    }


    public static void main(String[] args)
    {
        new Server();
    }
    public Server()
    {
        try{

            port=8080;
            clients=new ArrayList<Socket>();
            rooms=new ArrayList<room>();
            server=new ServerSocket(port);


            while(true)
            {
                Socket socket=server.accept();
                clients.add(socket);
                Mythread mythread=new Mythread(socket);
                mythread.start();
            }



        }catch(Exception ex)
        {
        }
    }
    public String ShowAllRooms()
    {
        String CurrentRooms="room list: ";
        for (int i=0;i<rooms.size();i++)
        {
            CurrentRooms+=rooms.get(i).getRoom_name()+" ";
        }
        return CurrentRooms;
    }
    class Mythread extends Thread
    {
        Socket ssocket;
        private BufferedReader br;
        private PrintWriter pw;
        public String msg;
        public String[] words;


        public Mythread(Socket s)
        {
            ssocket=s;
        }
        public void run()
        {

            try{
                br = new BufferedReader(new InputStreamReader(ssocket.getInputStream()));

                msg = "Hello " + ssocket.getInetAddress() + ", welcome to Ji's chat lobby.\n"
                        + clients.size() + " users online now.\nPlease choose a chat room or create one. "+ShowAllRooms();

                sendMsgToIndividual(ssocket);


                boolean clientSendCommand=false;
                while ((msg = br.readLine()) != null) {
                    words=msg.split(" ",2);
                    clientSendCommand=false;
                    if (msg.equals("jshowrooms"))
                    {
                        msg=ShowAllRooms();
                        clientSendCommand=true;
                    }
                    else if(words[0].equals("jcreate"))
                    {
                        rooms.add(new room(words[1]));
                        System.out.println("room:"+words[1]+" has been created");
                        msg="room:"+words[1]+" has been created";
                        clientSendCommand=true;
                    }
                    else if (words[0].equals("jswitch"))
                    {
                        boolean room_exist=false;
                        for(int i=0;i<rooms.size();i++)
                        {
                            if (rooms.get(i).getRoom_name().equals(words[1]))
                            {
                                //delete from other room
                                for (int j=0;j<rooms.size();j++)
                                {
                                    if(rooms.get(j).ifMemberExisit(ssocket))
                                        rooms.get(j).remove_member(ssocket);
                                }
                                System.out.println("you are now in chat room "+words[1]);
                                rooms.get(i).add_member(ssocket);
                                msg="you are now in chat room "+words[1]+"\n"+rooms.get(i).chatlog+"\n---------------------------------------chat history--------------------------------------------";
                                room_exist=true;
                                clientSendCommand=true;
                            }
                        }
                        if (!room_exist)
                            System.out.println("Chat room does not exist!");
                    }

                    if (clientSendCommand)
                    {
                        sendMsgToIndividual(ssocket);
                    }
                    else {
                        msg = ssocket.getInetAddress() + "said: " + msg;
                        sendMsgWithinRoom(ssocket);
                    }

                }
            }catch(Exception ex)
            {

            }
        }
        public void sendMsgToIndividual(Socket individual)
        {
            try{
                System.out.println("message: "+msg+"has been sent to "+individual.getInetAddress());

                    pw=new PrintWriter(individual.getOutputStream(),true);
                    pw.println("system: "+msg);
                    pw.flush();
            }catch(Exception ex)
            {
            }
        }
        public void sendMsgWithinRoom(Socket ss)
        {
            try{
                System.out.println(msg);
                for (int q=0;q<rooms.size();q++)
                {
                    if(rooms.get(q).ifMemberExisit(ss))
                    {
                        for(int w =0;w<rooms.get(q).size(); w++)
                        {

                            pw=new PrintWriter(rooms.get(q).getMember(w).getOutputStream(),true);
                            pw.println(msg);
                            pw.flush();

                        }
                        rooms.get(q).chatlog += msg+"\n";


                    }

                }
                /*
                if (room1.contains(ss))
                {
                    for(int i = room1.size() - 1; i >= 0; i--)
                    {
                        pw=new PrintWriter(room1.get(i).getOutputStream(),true);
                        pw.println(msg);
                        pw.flush();
                    }
                }
                else if (room2.contains(ss))
                {
                    for(int i = room2.size() - 1; i >= 0; i--)
                    {
                        pw=new PrintWriter(room2.get(i).getOutputStream(),true);
                        pw.println(msg);
                        pw.flush();
                    }
                }
                */
                /*
                for(int i = clients.size() - 1; i >= 0; i--)
                {
                    pw=new PrintWriter(clients.get(i).getOutputStream(),true);
                    pw.println(msg);
                    pw.flush();
                }
                */
            }catch(Exception ex)
            {
            }
        }
    }

}
