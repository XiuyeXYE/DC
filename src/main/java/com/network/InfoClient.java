package com.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class InfoClient {
	public static final String REMOTE_IP = "106.13.163.111";
	public static final String LOCAL_IP = "localhost";

	public static void main(String[] args) throws UnknownHostException, IOException {
		Socket s = new Socket(REMOTE_IP, 8888);
		s.setSendBufferSize(1024);
		String networkInfo = "\tLocalSocketAddress:" + s.getLocalSocketAddress() + " ;\n\t" + "LocalAddress:"
				+ s.getLocalAddress() + " ;\n\t" + "LocalPort:" + s.getLocalPort() + " ;\n\t" + "RemoteSocketAddress:"
				+ s.getRemoteSocketAddress() + " ;\n\t" + "InetAddress:" + s.getInetAddress() + " ;\n\t" + "Port:"
				+ s.getPort() + " ;\n";
		System.out.println("Local client info:\n" + networkInfo);
		System.out.println("client connect server...");
		BufferedOutputStream writer = new BufferedOutputStream(s.getOutputStream());
		writer.write("Hello,server,I am client.I accept your message!".getBytes("UTF-8"));
		networkInfo = "Client network info:\n" + networkInfo;
		writer.write(networkInfo.getBytes("UTF-8"));
		writer.flush();
		if (!s.isOutputShutdown()) {
			s.shutdownOutput();
		}
//		writer.close();
		BufferedInputStream reader = new BufferedInputStream(s.getInputStream());
		byte[] readMsg = new byte[1024];
		int len = 0;
		StringBuilder buffer = new StringBuilder();
		while ((len = reader.read(readMsg)) != -1) {
			buffer.append(new String(readMsg, 0, len, "UTF-8"));
		}
		System.out.println("received server msg:\n\t" + buffer);
//		reader.close();
		if (!s.isInputShutdown()) {
			s.shutdownInput();
		}
//		s.close();
	}

}
