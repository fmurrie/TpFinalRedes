package server;

import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerRunnable
{
	public static void main(String[] args)
	{
		try
		{
			/*Se crea un socket servidor que utiliza el puerto 3000 para
			escuchar las posibles nuevas conexiones*/
			ServerSocket serverSocket= new ServerSocket(3000);
			JOptionPane.showMessageDialog(null,"The server will start run! \nIt will be in invisible mode waiting for client connections.");
			/*Creamos este bucle para que el socket servidor este constantemente a la escucha de
			nuevas conexiones de clientes y las acepte para iniciar una conversacion cliente/servidor*/
			while (true)
			{
				/*Si el socket servidor recibe una nueva conexion la aceptamos y la guardamos creando el socket que
				utilizara esta nueva conexion del servidor para entablar la comunicacion con el cliente*/
				Socket socket=serverSocket.accept();
				//Se abre un mensaje de aviso para afirmar que sea conectado un nuevo cliente
				JOptionPane.showMessageDialog(null,"New client has connected:\n"+socket);
				//Nueva instancia de conexion al servidor para un cliente que se ha conectado
				new Server(socket);
			}
		}
		catch (IOException ex)
		{
			JOptionPane.showMessageDialog(null,"The server is in status unavailable!");
		}
	}	
}

