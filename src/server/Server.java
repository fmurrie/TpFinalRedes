package server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class Server extends JFrame implements Runnable
{
    //Atributos para la logica de la aplicacion del servidor:
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private Thread threadListener;

    /*Atributos de la interfaz grafica de la aplicacion del servidor que
     permiten conceder valores a los atributos de la logica:*/
    private JPanel panel;
    private JTextArea messagesHistory;
    private JTextField messageToSend;
    private JButton btnSend;

    /*Metodo constructor que se encarga de armar la interfaz grafica
    de la aplicacion del servidor para la nueva conexion de un cliente con el servidor
    y asi poder responder a los mensajes que envie el cliente conectado*/
    public Server(Socket socket)
    {
        //Centramos la ventana
        this.centerTheScreen();
        //Le damos un formato al panel para que los componentes de la pantalla esten ordenados
        this.panel=new JPanel(new GridLayout(4,1));

        //Establecemos un titulo para distinguir que esta es la ventana del servidor
        JLabel title=new JLabel("SERVER",JLabel.CENTER);
        this.panel.add(title);

        /*Armamos un cuadro detexto donde se ira guardando el historial de mensajes, con un
        formato scrollable*/
        messagesHistory=new JTextArea();
        messagesHistory.setEditable(false);
        this.panel.add(messagesHistory);

        //Agregamos el campo de texto donde se va a escribir el mensaje para enviarle al cliente
        messageToSend=new JTextField();
        messageToSend.setHorizontalAlignment(SwingConstants.CENTER);
        this.panel.add(messageToSend);

        //Agregamos el boton que enviara el mensaje al cliente
        this.btnSend=new JButton("Send");
        this.btnSendActionSendMessage();
        this.panel.add(btnSend);

        //Agregamos el panel dentro del marco
        this.add(panel);

        //Establecemos las caracteristicas del marco
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setVisible(true);

        //Inicializamos el socket con la instancia recibida por haber aceptado la nueva conexion con el server socket
        this.socket = socket;

        try
        {
            //Inicilizamos el flujo de salida que se va a utilizar para mandarle mensajes al cliente
            bufferedWriter = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream()));
            //Inicilizamos el flujo de entrada que se va a utilizar para recibir mensajes del cliente
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            /*Creamos un hilo y lo corremos para estar constantemente a la escucha de los mensajes que envia el cliente y poder recibirlos
            para mostrarlos en pantalla*/
            this.threadListener=new Thread(this);
            this.threadListener.start();
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(null,"A problem occurred during the connectation.");
        }
    }

    //Metodo que cierra el socket cuando el cliente se desconecta o el servidor deja de ejecutarse
    public void disconnect()
    {
        try
        {
            //Cerramos el socket para cortar con los flujos de entrada y salida y terminar la comunicacion cliente/servidor
            socket.close();
            this.setVisible(false);
            this.dispose();
            //System.exit(0);
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(null,"A problem occurred during the disconnectation.");
        }
    }

    /*Metodo que se va a ejecutar en un hilo que se va a encargar de estar a la escucha de los mensajes provenientes de un cliente y agregarlos al
    historial de mensajes para que quede un registros de ellos y asi el servidor poder saber que responder*/
    @Override
    public void run()
    {
        try
        {
            //Variable para guardar el mensaje recibido por el cliente
            String messageReceived = "";

            //Enviamos un mensaje al cliente para que sepa que se ha podido conectar correctamente con esta instancia del servidor
            bufferedWriter.write("Connected to the server!");
            bufferedWriter.newLine();
            bufferedWriter.flush();

            //Creamos este bucle para que el hilo este constantemente a la escucha de mensajes nuevos
            while(true)
            {
                //Guardamos el mensaje recibido por el flujo de entrada que utiliza el buffer
                messageReceived=bufferedReader.readLine();
                //Solo guardamos los mensajes si no son nulos, es decir, si traen contenido en formato string
                if(messageReceived!=null)
                {
                    if(!messageReceived.equalsIgnoreCase("x"))
                    {
                        this.addMessageInHistory(messageReceived);
                    }
                    else
                    {
                        this.disconnect();
                    }
                }
            }
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(null,"The client has disconnected!");
            disconnect();
        }

    }

    /*Metodo encargado de generar la eventualidad que al momento de presionar el boton de enviar,
    se encargara de escribir el mensaje en el flujo de salida y que este se envie al cliente para
    que lo pueda recibir*/
    private void btnSendActionSendMessage()
    {
        //Creamos una Instancia de la interfaz con el metodo armado para que pueda tener el evento la clase Server
        btnSend.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if(!messageToSend.getText().isEmpty())
                {

                    try
                    {
                        if(messageToSend.getText().equalsIgnoreCase("x"))
                        {
                            //El servidor le manda x al cliente para avisarle que se va a desconectar, asi el tambien lo hace
                            bufferedWriter.write(messageToSend.getText());
                            bufferedWriter.newLine();
                            bufferedWriter.flush();
                            messageToSend.setText("");
                            disconnect();
                        }
                        else
                        {
                            //Guardamos el mensaje en el flujo de salida y aclarando que este mensaje lo envia el servidor
                            bufferedWriter.write("Server: "+messageToSend.getText()+".");
                            bufferedWriter.newLine();
                            bufferedWriter.flush();
                            addMessageInHistory("You: "+messageToSend.getText()+".");
                            messageToSend.setText("");
                        }

                    } catch (IOException ex)
                    {
                        JOptionPane.showMessageDialog(null,"A problem occurred sending the message.\nProbably the connection is lost.");
                    }
                }
            }
        });
    }

    //Metodo para centrar la ventana en el medio de la pantalla, dandole dimensinoes de ancho y alto
    private void centerTheScreen()
    {
        setBounds(600,300,300,400);
        this.setLocationRelativeTo(null);
    }

    //Metodo que agrega un mensaje enviado o recibido al historial de mensajes
    public void addMessageInHistory(String message)
    {
        String oldMessages=this.messagesHistory.getText();
        this.messagesHistory.setText(message+"\n"+oldMessages);
    }
}
