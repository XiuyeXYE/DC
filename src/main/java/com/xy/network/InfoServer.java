package com.xy.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class InfoServer {
	public static void main(String[] args) throws IOException {
		ServerSocket server = new ServerSocket(8888);
		System.out.println("server listening...");
		while (true) {
			Socket s = server.accept();
			String networkInfo = "\tLocalSocketAddress:" + s.getLocalSocketAddress() + " ;\n\t" + "LocalAddress:"
					+ s.getLocalAddress() + " ;\n\t" + "LocalPort:" + s.getLocalPort() + " ;\n\t"
					+ "RemoteSocketAddress:" + s.getRemoteSocketAddress() + " ;\n\t" + "InetAddress:"
					+ s.getInetAddress() + " ;\n\t" + "Port:" + s.getPort() + " ;\n";
			System.out.println("Local server info:\n" + networkInfo);
			s.setSendBufferSize(1024);
			System.out.println("one client connected");
			BufferedInputStream reader = new BufferedInputStream(s.getInputStream());
			byte[] readMsg = new byte[1024];
			int len = 0;
			StringBuilder buffer = new StringBuilder();
			while ((len = reader.read(readMsg)) != -1) {
				buffer.append(new String(readMsg, 0, len, "UTF-8"));
			}
			System.out.println("received client msg:\n\t" + buffer);
			if (!s.isInputShutdown()) {
				s.shutdownInput();
			}
//			reader.close();
			BufferedOutputStream writer = new BufferedOutputStream(s.getOutputStream());
			writer.write("Hello,client,I am server.I accept your message!\n".getBytes("UTF-8"));
			networkInfo = "Server network info:\n" + networkInfo;
			writer.write(networkInfo.getBytes("UTF-8"));
			writer.flush();
//			writer.close();
			if (!s.isOutputShutdown()) {
				s.shutdownOutput();
			}
//			s.close();
		}
	}
}
