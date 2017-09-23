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
                user newUser=new user(socket,socket.getInetAddress().toString());
                Mythread mythread=new Mythread(newUser);
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
        user myuser;
        private BufferedReader br;
        private PrintWriter pw;
        public String msg;
        public String[] words;


        public Mythread(user u)
        {
            myuser=u;
        }
        public void run()
        {

            try{
                br = new BufferedReader(new InputStreamReader(myuser.getUserSocket().getInputStream()));

                msg = "Hello " + myuser.getUserSocket().getInetAddress() + ", welcome to Ji's chat lobby.\n"
                        + clients.size() + " users online now.\nPlease choose a chat room or create one. "+ShowAllRooms();

                sendMsgToIndividual(myuser.getUserSocket());


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

                    else if (words[0].equals("jjoin"))
                    {
                        boolean room_exist=false;
                        for(int i=0;i<rooms.size();i++)
                        {
                            if (rooms.get(i).getRoom_name().equals(words[1]))
                            {
                                /* delete from other room
                                for (int j=0;j<rooms.size();j++)
                                {
                                    if(rooms.get(j).ifMemberExisit(ssocket))
                                        rooms.get(j).remove_member(ssocket);
                                }
                                */
                                System.out.println("user joined chat room "+words[1]);
                                rooms.get(i).add_member(myuser.getUserSocket());
                                myuser.addRoom(rooms.get(i));
                                msg="you have join in chat room "+words[1]+"\n"+rooms.get(i).chatlog+"\n---------------------------------------chat history--------------------------------------------";
                                room_exist=true;
                                clientSendCommand=true;
                            }
                        }
                        if (!room_exist)
                            System.out.println("Chat room does not exist!");
                    }
                    else if (words[0].equals("jswitch"))
                    {
                        clientSendCommand=true;
                        if (myuser.findRooms(words[1])!=null) {
                            myuser.setCurrentRoom(myuser.findRooms(words[1]));
                            msg="you can speak in room: "+words[1];
                        }
                        else{
                            msg="You should join the room first";
                        }


                    }
                    else if (words[0].equals("jleave"))
                    {
                        clientSendCommand=true;
                        if (myuser.findRooms(words[1])!=null)
                        {
                            myuser.leaveRoom(words[1]);
                            msg="you have left room: "+words[1];
                        }
                    }
                    if (words[0].equals("jshowmyrooms"))
                    {
                        clientSendCommand=true;
                        msg=myuser.getAllRoomnames();
                    }

                    if (clientSendCommand)
                    {
                        sendMsgToIndividual(myuser.getUserSocket());
                    }
                    else {
                        msg = myuser.getName() + "said: " + msg;
                        sendMsgWithinRoom(myuser);
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
        public void sendMsgWithinRoom(user u)
        {
            try{
                System.out.println(msg);

                        for(int w =0;w<u.getCurrentRoom().size(); w++)
                        {

                            pw=new PrintWriter(u.getCurrentRoom().getMember(w).getOutputStream(),true);
                            pw.println(msg);
                            pw.flush();
                        }
                        u.getCurrentRoom().chatlog += msg+"\n";
            }catch(Exception ex)
            {
            }
        }
    }

}
