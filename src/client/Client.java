package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class Client extends JFrame implements Runnable
{
    //Atributos para la logica de la aplicacion del cliente:
    private String NICKNAME;
    private Integer PORT = 3000;
    private String HOST = "localhost";
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private Thread threadListener;

    /*Atributos del panel de login de la interfaz grafica de la aplicacion del cliente que
     permiten conceder valores a los atributos de la logica:*/
    private JPanel panelLogin;
    private JTextField txtNickname;
    private JTextField txtHost;
    private JTextField txtPort;
    private JButton btnJoin;

    /*Atributos del panel de chat de la interfaz grafica de la aplicacion del cliente que
    permiten conceder valores a los atributos de la logica:*/
    private JPanel panelChat;
    private JLabel lblNicknameClientLogged;
    private JTextArea messagesHistory;
    private JTextField messageToSend;
    private JButton btnSend;


    /*Metodo constructor que se encarga de armar la interfaz grafica
    de la aplicacion del cliente para que se pueda utilizar la conexion
    con el servidor creada y poder enviar y recibir mensajes en
    comunicacion con el servidor*/
    public Client()
    {
        //Le damos un formato al frame para que pueda tener varios paneles
        this.setLayout(new CardLayout());
        //Centramos la ventana
        this.centerTheScreen();

        //Armamos el panel para el logueo del cliente
        buildPanelLogin();
        //Armamos el panel para que el cliente pueda comunicarse con el servidor
        buildPanelChat();
        /*Inicialmente el cliente debe loguearse antes para poder comunicarse por eso
        el panel de login comienza siendo visible y el de chat no*/
        panelChat.setVisible(false);
        panelLogin.setVisible(true);

        //Establecemos las caracteristicas del marco
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setVisible(true);

        /*Creamos un hilo y lo corremos para estar constantemente a la escucha de los mensajes que envia el servidor y poder recibirlos
          para mostrarlos en pantalla*/
        this.threadListener=new Thread(this);
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
        } catch (IOException ex)
        {
            JOptionPane.showMessageDialog(null,ex.getMessage());
        }
    }

    /*Metodo que se va a ejecutar en un hilo que se va a encargar de estar a la escucha de los mensajes provenientes del servidor y agregarlos al
    historial de mensajes para que quede un registros de ellos y asi el cliente poder saber que le envio el servidor*/
    @Override
    public void run()
    {
        try
        {
            //Variable para guardar el mensaje recibido por el cliente
            String messageReceived="";
            //Creamos un buffer para recibir el mensaje
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //Guardamos un mensaje diciendo queeste cliente ya esta inicializado
            this.addMessageInHistory("Client initialized!");

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
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null,"The server has disconnected!");
            disconnect();
        }
    }

    /*Metodo encargado de generar la eventualidad que al momento de presionar el boton de enviar,
    se encargara de escribir el mensaje en el flujo de salida y que este se envie al servidor para
    que lo pueda recibir*/
    private void btnSendActionSendMessage()
    {
        //Creamos una Instancia de la interfaz con el metodo armado para que pueda tener el evento la clase Client
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
                            //El cliente le manda x al servidor para avisarle que se va a desconectar, asi el tambien lo hace
                            bufferedWriter.write(messageToSend.getText());
                            bufferedWriter.newLine();
                            bufferedWriter.flush();
                            messageToSend.setText("");
                            disconnect();
                        }
                        else
                        {
                            //Guardamos el mensaje en el flujo de salida y aclarando que este mensaje lo envia el cliente con el respectivo nickname
                            bufferedWriter.write(NICKNAME + ": " + messageToSend.getText() + ".");
                            bufferedWriter.newLine();
                            bufferedWriter.flush();
                            addMessageInHistory("You: " + messageToSend.getText() + ".");
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

    /*Metodo encargado de generar la eventualidad que al momento de presionar el boton de ingresar,
    se encargara de llenar los atributos para la logica con los valores ingresados y asi tener la informacion requerida para
    poder entablar comunicacion con el servidor*/
    private void btnJoinActionJoinInChat()
    {
        //Creamos una Instancia de la interfaz con el metodo armado para que pueda tener el evento la clase Client
        btnJoin.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if( (txtNickname.getText().isEmpty()) || (txtHost.getText().isEmpty()) || (txtPort.getText().isEmpty()))
                {
                    JOptionPane.showMessageDialog(null,"Complete the fields and try again");
                }
                else
                {
                    try
                    {
                        //Establecemos la informacion del cliente
                        NICKNAME=txtNickname.getText();
                        HOST =txtHost.getText();
                        PORT = Integer.valueOf(txtPort.getText());
                        lblNicknameClientLogged.setText("CLIENT "+NICKNAME);
                        //Creamos un socket para utilizar la conexion por flujos de entrada y salida
                        socket = new Socket(HOST, PORT);
                        /*Creamos un hilo y lo corremos para estar constantemente a la escucha de los mensajes que envia el servidor y poder recibirlos
                        para mostrarlos en pantalla*/
                        threadListener.start();
                        //Inicilizamos el flujo de salida que se va a utilizar para mandarle mensajes al servidor
                        bufferedWriter = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream()));
                        //Ahora el cliente ya esta logueado por lo tanto debe ir al panel de chat para comunicarse con el servidor
                        panelChat.setVisible(true);
                        panelLogin.setVisible(false);
                    }
                    catch (IOException | NumberFormatException ex)
                    {
                        JOptionPane.showMessageDialog(
                                null,
                                "A problem occurred logging into the system." +
                                        "\nCauses:" +
                                        "\n-Server unavailable." +
                                        "\n-Invalid nickname." +
                                        "\n-Invalid IP Address. (Try using 'localhost')" +
                                        "\n-Invalid Port. (Try using port '3000')" +
                                        "\nPlease try again.");
                    }
                }
            }
        });
    }

    //Metodo para armar el panel de Login de la interfaz grafica de la aplicacion de cliente:
    private void buildPanelLogin()
    {
        this.repaint();
        this.panelLogin=new JPanel(new GridLayout(8,1));

        JLabel title=new JLabel("CLIENT LOGIN", JLabel.CENTER);
        this.panelLogin.add(title);

        JLabel lblNickname=new JLabel("Nickname:",JLabel.CENTER);
        this.panelLogin.add(lblNickname);
        this.txtNickname=new JTextField(20);
        txtNickname.setHorizontalAlignment(SwingConstants.CENTER);
        this.panelLogin.add(txtNickname);

        JLabel lblHost=new JLabel("Ip address:",JLabel.CENTER);
        this.panelLogin.add(lblHost);
        this.txtHost =new JTextField(20);
        txtHost.setHorizontalAlignment(SwingConstants.CENTER);
        this.panelLogin.add(txtHost);

        JLabel lblPort=new JLabel("Port:",JLabel.CENTER);
        this.panelLogin.add(lblPort);
        this.txtPort=new JTextField(20);
        this.panelLogin.add(txtPort);

        this.btnJoin=new JButton("Join");
        this.btnJoinActionJoinInChat();
        this.panelLogin.add(btnJoin);
        this.add(panelLogin);
    }

    //Metodo para armar el panel de Chat de la interfaz grafica de la aplicacion de cliente:
    private void buildPanelChat()
    {
        this.repaint();
        this.panelChat=new JPanel(new GridLayout(4,1));

        lblNicknameClientLogged=new JLabel("CLIENT ",JLabel.CENTER);
        this.panelChat.add(lblNicknameClientLogged);

        this.messagesHistory=new JTextArea();
        messagesHistory.setEditable(false);
        this.panelChat.add(messagesHistory);

        this.messageToSend=new JTextField(20);
        messageToSend.setHorizontalAlignment(SwingConstants.CENTER);
        this.panelChat.add(messageToSend);
        this.btnSend=new JButton("Send");
        this.btnSendActionSendMessage();
        this.panelChat.add(btnSend);

        this.add(panelChat);
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
