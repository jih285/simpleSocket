package com.whatbrain;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class user {
    private String name;
    private List<room> myrooms;
    private Socket userSocket;
    private room currentRoom;

    public user(Socket s,String name){
        this.name=name;
        userSocket=s;
        myrooms=new ArrayList<room>();
    }

    public Socket getUserSocket() {
        return this.userSocket;
    }

    public String getName() {
        return this.name;
    }
    public void setName(String nname)
    {
        this.name=nname;
    }
    public void addRoom(room room)
    {
        this.myrooms.add(room);
    }
    public void setCurrentRoom(room room)
    {
        this.currentRoom=room;
    }
    public room findRooms(String targetRoom)
    {
        for (int i=0;i<this.myrooms.size();i++)
        {
            if (this.myrooms.get(i).getRoom_name().equals(targetRoom))
            {
                return myrooms.get(i);
            }
        }
        return null;
    }
    public room getCurrentRoom()
    {
        return this.currentRoom;
    }
    public void leaveRoom(String rname){
        if (this.currentRoom!=null)
        {
            if (this.currentRoom.getRoom_name().equals(rname))
                this.currentRoom=null;
        }
        this.findRooms(rname).remove_member(this.userSocket);
        myrooms.remove(this.findRooms(rname));
    }
    public String getAllRoomnames()
    {
        String names="";
        for (int i=0;i<myrooms.size();i++)
        {
            names+=myrooms.get(i).getRoom_name()+" ";
        }
        return names;
    }
}
