using System;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.Text;
// State object for receiving data from remote device.    
class Program
{
    public static void sendMessage(object obj)
    {
        Socket socket = (Socket)obj;
        string input = Console.ReadLine();
        byte[] data = Encoding.ASCII.GetBytes(input);
        socket.Send(data, data.Length, SocketFlags.None);
        //Thread thread = new Thread(new ParameterizedThreadStart(ReceiveMessage));
        //thread.Start(socket);
    }
    public static void ReceiveMessage(object obj)
    {
        Socket socket = (Socket)obj;
        byte[] data = new byte[1024];
        int len = socket.Receive(data);
        string dataString = Encoding.ASCII.GetString(data, 0, len);
        Console.WriteLine("Receive Data:{0} from {1}", dataString, socket.RemoteEndPoint);
        //Thread thread = new Thread(new ParameterizedThreadStart(SendMessage));
        //thread.Start(socket);
    }
    static void Main(string[] args)
    {
        new Client();
    }
    public class Client
    {
        public Socket socket=null;
        byte[] data;
        byte[] rdata;
        string input, stringData, rstringData;

        public void rrrun()
        {
            while (true) { 
            rdata = new byte[1024];
            int recv = socket.Receive(rdata);
            stringData = Encoding.ASCII.GetString(rdata, 0, recv);
            Console.WriteLine(stringData);
            }
        }

        public Client()
        {

            try
            {
                data = new byte[1024];
                rdata = new byte[1024];
                //定义主机的IP及端口
                IPAddress ip = IPAddress.Parse("127.0.0.1");
                IPEndPoint ipEnd = new IPEndPoint(ip, 8080);
                //定义套接字类型
                socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
                socket.Connect(ipEnd);

                Thread t = new Thread(rrrun);
                t.Start();

                while (true)
                {
                    input = Console.ReadLine();
                    if (input == "exit")
                    {
                        break;
                    }
                    data = Encoding.ASCII.GetBytes(input + "\r");
                    socket.Send(data, data.Length, SocketFlags.None);

                }
                Console.WriteLine("disconnect from server");
                socket.Shutdown(SocketShutdown.Both);
                socket.Close();
            }
            catch (SocketException e)
            {
                Console.WriteLine("Fail to connect server");
                Console.WriteLine(e.ToString());
                return;
            }


        }



    }
}