package appchat.model;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.apache.log4j.BasicConfigurator;

public class User1 extends JFrame implements ActionListener {

	private JPanel contentPane;
	private JButton btnNewButton;
	private JTextArea txtChat;
	private static JTextArea textArea;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					User1 frame = new User1();
					listen();
					frame.setVisible(true);
					frame.revalidate();
					frame.repaint();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public User1() {
		setTitle("May 1");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 966, 518);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "N\u1ED9i dung xem", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setBounds(0, 0, 940, 401);
		contentPane.add(panel);
		panel.setLayout(null);
		
		textArea = new JTextArea();
		textArea.setBounds(10, 21, 920, 369);
		panel.add(textArea);
		
		txtChat = new JTextArea();
		txtChat.setBounds(84, 412, 763, 56);
		contentPane.add(txtChat);
		
		JLabel lblNewLabel = new JLabel("Nhập text chat");
		lblNewLabel.setBounds(0, 412, 84, 56);
		contentPane.add(lblNewLabel);
		
		 btnNewButton = new JButton("Gửi");
		btnNewButton.setBounds(857, 412, 83, 56);
		contentPane.add(btnNewButton);
		btnNewButton.addActionListener(this);
	}
	public void send(String messeage) throws NamingException, JMSException {
		//thiết lập môi trường cho JMS logging
		BasicConfigurator.configure();
		//thiết lập môi trường cho JJNDI
		Properties settings=new Properties();
		settings.setProperty(Context.INITIAL_CONTEXT_FACTORY, 
		"org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		settings.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
		//tạo context
		Context ctx=new InitialContext(settings);
		//lookup JMS connection factory
		Object obj=ctx.lookup("TopicConnectionFactory");
		ConnectionFactory factory=(ConnectionFactory)obj;
		//tạo connection
		Connection con=factory.createConnection("admin","admin");
		//nối đến MOM
		con.start();
		//tạo session
		Session session=con.createSession(
		/*transaction*/false,
		/*ACK*/Session.AUTO_ACKNOWLEDGE
		);
		Destination  destination=(Destination) 
		ctx.lookup("dynamicTopics/user2");
		//tạo producer
		MessageProducer producer = session.createProducer(destination);
		Message msg=session.createTextMessage(messeage);
		//gửi
		producer.send(msg);
		//shutdown connection
		session.close();
		con.close();
		System.out.println("Finished...");
		}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj=e.getSource();
		if(obj.equals(btnNewButton)) {
			try {
				send(txtChat.getText());
				textArea.append("\n"+txtChat.getText()+"\n");
			} catch (NamingException | JMSException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
	}
	public static void listen() throws NamingException, JMSException {
		BasicConfigurator.configure();
		// thiết lập môi trường cho JJNDI
		Properties settings = new Properties();
		settings.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		settings.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
		// tạo context
		Context ctx = new InitialContext(settings);
		// lookup JMS connection factory
		Object obj = ctx.lookup("TopicConnectionFactory");
		ConnectionFactory factory = (ConnectionFactory) obj;
		// tạo connection
		Connection con = factory.createConnection("admin", "admin");
		// nối đến MOM
		con.start();
		// tạo session
		Session session = con.createSession(/* transaction */false, /* ACK */Session.CLIENT_ACKNOWLEDGE);
		// tạo consumer
		Destination destination = (Destination) ctx.lookup("dynamicTopics/thanthidet");
		MessageConsumer receiver = session.createConsumer(destination);
		// receiver.receive();//blocked method
		// Cho receiver lắng nghe trên queue, chừng có message thì notify
		receiver.setMessageListener(new MessageListener() {
			@Override
			// có message đến queue, phương thức này được thực thi
			public void onMessage(Message msg) {// msg là message nhận được
				try {
					if (msg instanceof TextMessage) {
						TextMessage tm = (TextMessage) msg;
						String txt = tm.getText();
						textArea.append("\n"+"user2:"+txt+"\n");
						msg.acknowledge();// gửi tín hiệu ack
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		
	}
}

