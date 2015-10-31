package servletcontainer;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class HttpServer {
	ServerSocket serverSocket;
	Worker[] threadpool;
	TaskQueue taskQueue;
	String absolutepath;
	StopSign stopSign;
	HashMap<String, HttpServlet> servlets = new HashMap<String, HttpServlet>();
	HttpContext context;
	ScheduledExecutorService scheduledExecutorService;
	HashMap<String, Session> sessionPool = new HashMap<String, Session>();
	public static final int TIMEOUT = 500000;

	public HttpServer(int portnumber, String absolutepath, Handler h)
			throws IOException {
		this.serverSocket = new ServerSocket(portnumber);
		this.stopSign = new StopSign();
		this.taskQueue = new TaskQueue(stopSign);
		this.threadpool = new Worker[10];
		this.absolutepath = absolutepath;
		this.scheduledExecutorService = Executors.newScheduledThreadPool(5);
		// init the http context
		createHttpContext(h);

		// init the servlets
		createServlets(h);

		// init the threadpool
		for (int i = 0; i < this.threadpool.length; i++) {
			this.threadpool[i] = new Worker(this.taskQueue, this.stopSign);
		}
	}

	private void createHttpContext(Handler h) {
		context = new HttpContext();
		for (String param : h.m_contextParams.keySet()) {
			context.setInitParam(param, h.m_contextParams.get(param));
		}
	}

	private void createServlets(Handler h) {
		for (String servletName : h.m_servlets.keySet()) {
			HttpConfig config = new HttpConfig(servletName, context);
			String className = h.m_servlets.get(servletName);
			Class servletClass;
			try {
				servletClass = Class.forName(className);
				HttpServlet servlet = (HttpServlet) servletClass.newInstance();
				HashMap<String, String> servletParams = h.m_servletParams
						.get(servletName);
				if (servletParams != null) {
					for (String param : servletParams.keySet()) {
						config.setInitParam(param, servletParams.get(param));
					}
				}
				servlet.init(config);
				String url = null;
				if (h.m_servlet_url.containsKey(servletName)) {
					url = h.m_servlet_url.get(servletName);
				}
				if (url != null) {
					servlets.put(url, servlet);
				}
			} catch (ClassNotFoundException | InstantiationException
					| IllegalAccessException e) {
				// TODO Auto-generated catch block
				System.out.println("class not found");
				continue;
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				System.out.println("ServletException");
				continue;

			}
		}
		DefaultServlet defaultServlet = new DefaultServlet();
		try {
			defaultServlet.init();
			servlets.put("default", defaultServlet);
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static Handler parseWebdotxml(String webdotxml) throws Exception {
		Handler h = new Handler();
		File file = new File(webdotxml);
		if (file.exists() == false) {
			System.err.println("error: cannot find " + file.getPath());
			System.exit(-1);
		}
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(file, h);
		return h;
	}

	public void start() {
		// start the working threads
		for (Worker t : threadpool) {
			t.start();
		}
		while (!stopSign.isStopped()) {
			Socket clientSocket;
			try {
				clientSocket = serverSocket.accept();
				// clientSocket.setSoTimeout(TIMEOUT);
				Task t = new Task(clientSocket, this);
				taskQueue.insertTask(t);
			} catch (SocketException s) {
				System.out.println("Socket Close!");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		;

	}

	public static void main(String args[]) throws Exception {
		if (args.length == 0) {
			System.out.println("Full name: Wu Jingyuan\nSeas login name: wujingyu");
			return;
		}
		if (args.length != 3) {
			System.out.println("The number of arguments is wrong.");
			return;
		}
		int portnumber = Integer.parseInt(args[0]);
		String absolute_path = args[1];
		String web_xml_path = args[2];
		Handler h = parseWebdotxml(web_xml_path);
		HttpServer httpServer = new HttpServer(portnumber, absolute_path, h);
		httpServer.start();
		for (Worker t : httpServer.threadpool) {
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
