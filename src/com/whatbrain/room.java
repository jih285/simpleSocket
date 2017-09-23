package com.whatbrain;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
