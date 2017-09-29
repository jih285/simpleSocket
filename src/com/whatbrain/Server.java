package com.whatbrain;

import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
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

            //set timer to check if any room are expired every 24 hours
            Timer timer = new Timer ();
            TimerTask t = new TimerTask () {
                @Override
                public void run () {
                    deleteOldRooms();
                }
            };

            timer.schedule (t, 0l, 1000*60*60*24);

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
        if (rooms.size()==0)
        {
            return "Currently, there is no chat room in this server, please create one by yourself. Thank you";
        }
        String CurrentRooms="room list: ";
        for (int i=0;i<rooms.size();i++)
        {
            CurrentRooms+=(i+1)+". "+rooms.get(i).getRoom_name()+" ";
        }
        return CurrentRooms;
    }
    public void deleteOldRooms()
    {
        Date today=new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        for (int i=0;i<this.rooms.size();i++)
        {
            cal.setTime(rooms.get(i).getLastUpdate());
            cal.add(Calendar.DATE, 7);
            if (today.after(cal.getTime()))
            {
                String removedRoom=rooms.get(i).getRoom_name();
                rooms.remove(rooms.get(i));
                System.out.println("room: "+removedRoom+"has been deleted for inactive for 7 days");
            }

        }
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
            boolean welcomeInfoSend=false;
            try{
                boolean clientSendCommand=false;
                br = new BufferedReader(new InputStreamReader(myuser.getUserSocket().getInputStream()));
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
                        boolean roomexist=false;
                        clientSendCommand=true;
                        if (rooms.size()!=0) {
                            for (int i = 0; i < rooms.size(); i++) {
                                if (rooms.get(i).getRoom_name().equals(words[1]))
                                    roomexist = true;

                            }
                        }
                        if (!roomexist)
                        {
                            rooms.add(new room(words[1]));
                            System.out.println("room:" + words[1] + " has been created");
                            msg = "room:" + words[1] + " has been created";
                        }
                        else
                        {
                            msg ="room: "+words[1]+" has been created, please choose another name";
                        }

                    }

                    else if (words[0].equals("jjoin"))
                    {
                        boolean room_exist=false;
                        clientSendCommand=true;
                        for(int i=0;i<rooms.size();i++)
                        {
                            String chatHistory="";
                            if (rooms.get(i).getRoom_name().equals(words[1]))
                            {
                                System.out.println("user joined chat room "+words[1]);
                                rooms.get(i).add_member(myuser.getUserSocket());
                                myuser.addRoom(rooms.get(i));
                                if (!rooms.get(i).chatlog.equals(""))
                                    chatHistory=rooms.get(i).chatlog+"\n---------------------------------------chat history--------------------------------------------";
                                msg="you have join in chat room "+words[1]+"\n"+chatHistory;
                                room_exist=true;
                            }
                        }
                        if (!room_exist)
                            msg="Chat room does not exist!";
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
                    else if(words[0].equals("deleteoldrooms"))//just for testing, need to delete
                    {
                            deleteOldRooms();
                    }
                    else if (words[0].equals("jleave"))
                    {
                        clientSendCommand=true;
                        msg="room: "+words[1]+"do not exist";
                        if (myuser.findRooms(words[1])!=null)
                        {
                            myuser.leaveRoom(words[1]);
                            msg="you have left room: "+words[1];
                        }
                    }
                    else if (words[0].equals("jrename"))
                    {
                        clientSendCommand=true;
                        myuser.setName(words[1]);
                        msg="your name has been reset as "+words[1];
                    }
                    if (words[0].equals("jshowmyrooms"))
                    {
                        clientSendCommand=true;
                        msg=myuser.getAllRoomnames();
                    }
                    if(!welcomeInfoSend){

                        msg = "Hello " + myuser.getName() + ", welcome to Ji's chat lobby.\n"
                                + clients.size() + " users online now.\n\nYou could choose a chat room to join or create a new one.\n"+ShowAllRooms()+"\n\n useful commands:\n 1. jcreate [roomname]\n 2. jjoin [roomname] //receive messages from a chatroom, but can't speak\n 3. jswitch [roomname] //after join  a chatroom, you can siwtch to this room to say something\n 4. jleave [roomname]//you won't receive any message from this room\n 5. jshowrooms //show all existing rooms\n 6. jshowmyrooms //show the rooms that you join\n 7. rename [newname]";
                        welcomeInfoSend=true;
                        clientSendCommand=true;
                    }

                    if (clientSendCommand)
                    {
                        sendMsgToIndividual(myuser.getUserSocket());
                    }
                    else if (myuser.getCurrentRoom()!=null)
                    {
                        msg = "["+myuser.getName() + "] from ["+myuser.getCurrentRoom().getRoom_name()+"] said: " + msg;
                        sendMsgWithinRoom(myuser);
                    }
                    else
                        {
                            msg="you should switch to a room,then say something";
                            sendMsgToIndividual(myuser.getUserSocket());
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
                        u.getCurrentRoom().updateDate();//update the timestamp
            }catch(Exception ex)
            {
            }
        }
    }

}
