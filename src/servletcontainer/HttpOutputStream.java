package servletcontainer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.servlet.ServletOutputStream;

public class HttpOutputStream extends ServletOutputStream{

	DataOutputStream out;
	
	public HttpOutputStream(Socket clientSocket) throws IOException{
		out = new DataOutputStream(clientSocket.getOutputStream());
	}
	@Override
	public void write(int arg0) throws IOException {
		out.write(arg0);
	}
	
	public void close() throws IOException{
		out.close();
	}

}
