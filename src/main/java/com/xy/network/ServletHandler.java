/*
 * Copyright 2002 Sun Microsystems, Inc. All  Rights Reserved.
 *  
 * Redistribution and use in source and binary forms, with or 
 * without modification, are permitted provided that the following 
 * conditions are met:
 * 
 * -Redistributions of source code must retain the above copyright  
 *  notice, this list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright 
 *  notice, this list of conditions and the following disclaimer in 
 *  the documentation and/or other materials provided with the 
 *  distribution.
 *  
 * Neither the name of Sun Microsystems, Inc. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY 
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY 
 * DAMAGES OR LIABILITIES  SUFFERED BY LICENSEE AS A RESULT OF OR 
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR 
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE 
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, 
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER 
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF 
 * THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *  
 * You acknowledge that Software is not designed, licensed or 
 * intended for use in the design, construction, operation or 
 * maintenance of any nuclear facility. 
 */

package com.xy.network;

import java.io.*;
import java.net.*;
import java.rmi.Naming;
import java.rmi.server.RMIClassLoader;
import java.rmi.RemoteException;
import java.rmi.RMISecurityManager;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.Enumeration;
import java.rmi.registry.LocateRegistry;

import javax.servlet.http.*;
import javax.servlet.*;

/**
 * The default RMI socket factory contains several "fallback"
 * mechanisms which enable an RMI client to communicate with a remote
 * server.  When an RMI client initiates contact with a remote server,
 * it attempts to establish a connection using each of the following
 * protocols in turn, until one succeeds:
 *
 * 1. Direct TCP connection.
 * 2. Direct HTTP connection.
 * 3. Proxy connection (SOCKS or HTTP).
 * 4. Connection on port 80 over HTTP to a CGI script.
 * 5. Proxy HTTP connection to CGI script on port 80.
 *
 * The RMI ServletHandler can be used as replacement for the
 * java-rmi.cgi script that comes with the Java Development Kit (and
 * is invoked in protocols 4 and 5 above).  The java-rmi.cgi script
 * and the ServletHandler both function as proxy applications that
 * forward remote calls embedded in HTTP to local RMI servers which
 * service these calls.  The RMI ServletHandler enables RMI to tunnel
 * remote method calls over HTTP more efficiently than the existing
 * java-rmi.cgi script.  The ServletHandler is only loaded once from
 * the servlet administration utility.  The script, java-rmi.cgi, is
 * executed once every remote call.
 *
 * The ServletHandler class contains methods for executing as a Java
 * servlet extension.  Because the RMI protocol only makes use of the
 * HTTP post command, the ServletHandler only supports the
 * <code>doPost</code> <code>HttpServlet</code> method.  The
 * <code>doPost</code> method of this class interprets a servlet
 * request's query string as a command of the form
 * "<command>=<parameters>".  These commands are represented by the
 * abstract interface, <code>RMICommandHandler</code>.  Once the
 * <code>doPost</code> method has parsed the requested command, it
 * calls the execute method on one of several command handlers in the
 * <code>commands</code> array.
 *
 * The command that actually proxies remote calls is the
 * <code>ServletForwardCommand</code>.  When the execute method is
 * invoked on the ServletForwardCommand, the command will open a
 * connection on a local port specified by its <code>param</code>
 * parameter and will proceed to write the body of the relevant post
 * request into this connection.  It is assumed that an RMI server
 * (e.g. SampleRMIServer) is listening on the local port, "param."
 * The "forward" command will then read the RMI server's response and
 * send this information back to the RMI client as the body of the
 * response to the HTTP post method.
 *
 * Because the ServletHandler uses a local socket to proxy remote
 * calls, the servlet has the ability to forward remote calls to local
 * RMI objects that reside in the ServletVM or outside of it.
 * 
 * Servlet documentation may be found at the following location:
 *
 * http://jserv.javasoft.com/products/java-server/documentation/
 *        webserver1.0.2/apidoc/Package-javax.servlet.http.html 
 */
public class ServletHandler extends HttpServlet 
    implements Runnable 
{

    /* Variables to hold optional configuration properties. */

    /** codebase from which this servlet will load remote objects.*/
    protected static String initialServerCodebase = null;

    /** name of RMI server class to be created in init method */
    protected static String initialServerClass = null;

    /** name of RMI server class to be created in init method */
    protected static String initialServerBindName = null;

    /**
     * RMICommandHandler is the abstraction for an object that handles
     * a particular supported command (for example the "forward"
     * command "forwards" call information to a remote server on the
     * local machine).
     *
     * The command handler is only used by the ServletHandler so the
     * interface is protected.  
     */
    protected interface RMICommandHandler {
	
	/**
	 * Return the string form of the command to be recognized in the
	 * query string.  
	 */
	public String getName();
	
	/**
	 * Execute the command with the given string as parameter.
	 */
	public void execute(HttpServletRequest req, HttpServletResponse res, 
			    String param) 
	    throws ServletClientException, ServletServerException, IOException;
    }

    /**
     * List of handlers for supported commands. A new command will be
     * created for every service request 
     */
    private static RMICommandHandler commands[] = 
	new RMICommandHandler [] {
            new ServletForwardCommand(),
	    new ServletGethostnameCommand(),
	    new ServletPingCommand(),
	    new ServletTryHostnameCommand()
	};
    
    /* construct table mapping command strings to handlers */
    private static Hashtable commandLookup;
    static {
	commandLookup = new Hashtable();
	for (int i = 0; i < commands.length; ++ i)
	    commandLookup.put(commands[i].getName(), commands[i]);
    }
    
   /**
    * Once loaded, Java Servlets continue to run until they are
    * unloaded or the webserver is stopped.  This example takes
    * advantage of the extended Servlet life-cycle and runs a remote
    * object in the Servlet VM.
    *
    * To initialize this remote object the Servlet Administrator
    * should specify a set of parameters which will be used to
    * download and install an initial remote server (see readme.txt).
    * 
    * If configuration parameters are valid (not blank), the
    * servlet will attempt to load and start a remote object and a
    * registry in which the object may be bound.
    *
    * @param config Standard configuration object for an http servlet.
    *
    * @exception ServletException Calling
    *           <code>super.init(config)</code> may cause a servlet 
    *           exception to be thrown.  
    */
    public void init(ServletConfig config) throws ServletException {
	super.init(config);

	try {
	    setConfigParameters(config);

	    if (!verifyConfigParameters()) {
		// dont export any objects.

                System.err.println("Some optional parameters not set, " + 
				   "remote object not exported; " + 
				   "ServletHandler is runnning.");

		return;
	    }
	    
	    /* RMI requires that a local security manager be
             * responsible for the method invocations from remote
             * clients - we need to make sure a security manager is
             * installed.
	     */
	    if (System.getSecurityManager() == null) {
		System.setSecurityManager(new RMISecurityManager());
	    }

	    // create a registry if one is not running already.
	    try {
		java.rmi.registry.LocateRegistry.createRegistry(1099);
	    } catch (java.rmi.server.ExportException ee) {
		// registry already exists, we'll just use it.
	    } catch (RemoteException re) {
		System.err.println(re.getMessage());
		re.printStackTrace();
	    }
	    
	    /** 
	     * Download and create a server object in a thread so we
	     * do not interfere with other servlets.  Allow init
	     * method to return more quickly.
	     */
	    (new Thread(this)).start();
	    
	    System.out.println("RMI Servlet Handler loaded sucessfully.");
	    
	} catch (Exception e) {
	    System.err.println("Exception thrown in RMI ServletHandler: " + 
			       e.getMessage());
	    e.printStackTrace();
	}
    }

    /**
     * Create the sample RMI server.
     */
    public void run () {
	try {
	    UnicastRemoteObject server = 
		createRemoteObjectUsingDownloadedClass();
	    if (server != null) {
		Naming.rebind(initialServerBindName, server);
		System.err.println("Remote object created successfully.");
	    }
	} catch (Exception e) {
	    System.err.println("Exception received while intalling object:");
	    System.err.println(e.getMessage());
	    e.printStackTrace();
	}
    }

    /**
     * Load and export an initial remote object. The implementation
     * class for this remote object should not be loaded from the
     * servlet's class path; instead it should be loaded from a URL
     * location that will be accessible from a remote client.  In the
     * case of this example, that location will be
     * <code>initialServerCodebase</code> 
     */
    UnicastRemoteObject createRemoteObjectUsingDownloadedClass() 
	throws Exception
    {
	UnicastRemoteObject server = null;
	Class serverClass = null;

	int MAX_RETRY = 5;
	int retry = 0;
	int sleep = 2000;

	while ((retry < MAX_RETRY) && (serverClass == null)) {
	    try {
		System.err.println("Attempting to load remote class...");
		serverClass = RMIClassLoader.
		    loadClass(new URL(initialServerCodebase), 
			      initialServerClass);

		// Before we instantiate the obj. make sure it 
		// is a UnicastRemoteObject.
		if (!Class.forName("java.rmi.server.UnicastRemoteObject").
		    isAssignableFrom(serverClass)) {
		    System.err.println(
		        "This example requires an " + 
			" instance of UnicastRemoteObject," +
			" remote object not exported.");
		} else {
		    System.out.
			println("Server class loaded successfully...");
		    server = ((UnicastRemoteObject) serverClass.
			      newInstance());
		}

	    } catch (java.lang.ClassNotFoundException cnfe) {
		retry ++;
		
		/**
		 * The class for the remote object could not be
		 * loaded, perhaps the webserver has not finished
		 * initializing itself yet. Try to load the class a
		 * few more times...  
		 */
		if (retry >= MAX_RETRY) {
		    System.err.println("Failed to load remote server " + 
				       " class. Remote object not " + 
				       " exported... ");
		} else {
		    System.err.println("Could not load remote class, " + 
				       "trying again...");
		    try {
			Thread.sleep(sleep);
		    } catch (InterruptedException ie) {
		    }
		    continue;
		}
	    }
	}
	return server;
    }

    /* NOTE: If you are using JDK1.2Beta4 or later, it is recommended
     * that you provide your servlet with a destroy method that will
     * unexport any remote objects that your servlet ever exports.  As
     * mentioned in the readme file for this example, it is not
     * possible to unexport remote objects in JDK1.1.x; there is no
     * method in the RMI 1.1 public API that will perform this task.
     * To restart remote objects in the servlet VM, you will have to
     * restart your webserver.  In JDK1.2x, the methods to unexport a
     * remote object are as follows:
     *
     * java.rmi.activation.Activatable.
     *                 unexportObject(Remote obj, boolean force)
     * java.rmi.server.UnicastRemoteObject.
     *                 unexportObject(Remote obj, boolean force) 
     */

    /**
     * Execute the command given in the servlet request query string.
     * The string before the first '=' in the queryString is
     * interpreted as the command name, and the string after the first
     * '=' is the parameters to the command.
     *
     * @param req  HTTP servlet request, contains incoming command and
     *             arguments
     * @param res  HTTP servlet response
     * @exception  ServletException and IOException when invoking
     *             methods of <code>req<code> or <code>res<code>.  
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
	
	try {
	    
	    // Command and parameter for this POST request.
	    String queryString = req.getQueryString();
	    String command, param;
	    int delim = queryString.indexOf("=");
	    if (delim == -1) {
		command = queryString;
		param = "";
	    } else {
		command = queryString.substring(0, delim);
		param = queryString.substring(delim + 1);
	    }
	    
	    System.out.println("command: " + command);
	    System.out.println("param: " + param);

	    // lookup command to execute on the client's behalf
	    RMICommandHandler handler =
		(RMICommandHandler) commandLookup.get(command);
	    
	    // execute the command
	    if (handler != null)
		try {
		    handler.execute(req, res, param);
		} catch (ServletClientException e) {
		    returnClientError(res, "client error: " + 
				      e.getMessage());
		    e.printStackTrace();
		} catch (ServletServerException e) {
		    returnServerError(res, "internal server error: " + 
				      e.getMessage());
		    e.printStackTrace();
		}
	    else
		returnClientError(res, "invalid command: " + 
				  command);
	} catch (Exception e) {
	    returnServerError(res, "internal error: " + 
			      e.getMessage());
	    e.printStackTrace();
	}
    }

    /**
     * Provide more intelligible errors for methods that are likely to
     * be called.  Let unsupported HTTP "do*" methods result in an
     * error generated by the super class.
     *
     * @param req  http Servlet request, contains incoming command and
     *             arguments
     *
     * @param res  http Servlet response
     *
     * @exception  ServletException and IOException when invoking
     *             methods of <code>req<code> or <code>res<code>.
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {

	returnClientError(res,
			  "GET Operation not supported: " +
			  "Can only forward POST requests.");
    }

    public void doPut(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {

	returnClientError(res,
			  "PUT Operation not supported: " +
			  "Can only forward POST requests.");
    }

    public String getServletInfo() {
	return "RMI Call Forwarding Servlet Servlet.<br>\n";
    }

    /**
     * Return an HTML error message indicating there was error in
     * the client's request.
     *
     * @param res Servlet response object through which <code>message</code>
     *            will be written to the client which invoked one of
     *            this servlet's methods.
     * @param message Error message to be written to client.
     */
    private static void returnClientError(HttpServletResponse res, 
					  String message)
	throws IOException {
	
	res.sendError(HttpServletResponse.SC_BAD_REQUEST,
		      "<HTML><HEAD>" + 
		      "<TITLE>Java RMI Client Error</TITLE>" + 
		      "</HEAD>" + 
		      "<BODY>" + 
		      "<H1>Java RMI Client Error</H1>" + 
		      message + 
		      "</BODY></HTML>");

	System.err.println(HttpServletResponse.SC_BAD_REQUEST + 
		      "Java RMI Client Error" + 
		      message);
    }
    
    /**
     * Return an HTML error message indicating an internal error
     * occurred here on the server.  
     *
     * @param res Servlet response object through which <code>message</code>
     *            will be written to the servlet client.
     * @param message Error message to be written to servlet client.
     */
    private static void returnServerError(HttpServletResponse res,
					  String message)
	throws IOException {
	
	res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		      "<HTML><HEAD>" + 
		      "<TITLE>Java RMI Server Error</TITLE>" + 
		      "</HEAD>" + 
		      "<BODY>" + 
		      "<H1>Java RMI Server Error</H1>" + 
		      message + "</BODY></HTML>");

	System.err.println(HttpServletResponse.SC_INTERNAL_SERVER_ERROR + 
		      "Java RMI Server Error: " + 
		      message);
    }

    /**
     * Retrieve parameters from servlet configuration object.
     *
     * @param config Standard configuration object for an HTTP servlet.
     */
    protected synchronized void setConfigParameters(ServletConfig config) {
	try {
	    initialServerCodebase = config.
		getInitParameter("rmiservlethandler.initialServerCodebase");
	    initialServerClass = config.
		getInitParameter("rmiservlethandler.initialServerClass");
	    initialServerBindName = config.
		getInitParameter("rmiservlethandler.initialServerBindName");
	} catch (Exception e) {
	    System.err.println("");
	    System.err.println("Could not access init parameter:");
	    System.err.println(e.getMessage());
	    e.printStackTrace();
	}
    }

    /** 
     * Ensure that servlet configuration parameters are valid. 
     *
     * @return <code>true</code> if all relevant configuration parameters
     *         are valid (i.e. not "") <code>false</code> otherwise.  
     */
    protected synchronized boolean verifyConfigParameters() {
	return ((verifyParameter("rmiservlethandler.initialServerClass ", 
		    initialServerClass)) &&
		(verifyParameter("rmiservlethandler.initialServerBindName ", 
		    initialServerBindName)) &&
		(verifyParameter("rmiservlethandler.initialServerCodebase", 
		    initialServerCodebase)));
    }

    /** 
     * Verify that a single parameter is valid. 
     *
     * @return <code>true</code> if the parameter is valid.
     */
    protected boolean verifyParameter(String parameterName, String parameter) {
	if ((parameter == null) || (parameter.equals(""))) {
	    System.err.println("optional parameter is invalid and " + 
			       "will not be used: \n    " +
			       parameterName + " = " + parameter);
	    return false;
	} else {
	    System.err.println(parameterName + " " +
			       "valid: " + parameter);
	}
	return true;
    }

    /*
     * The ServletHandler class is the only object that needs to access the 
     * CommandHandler subclasses, so we write the commands internal to the
     * servlet handler.
     */

    /**
     * Class that has an execute command to forward request body to
     * local port on the server and send server reponse back to client.  
     */
    protected static class ServletForwardCommand implements RMICommandHandler {
	
	public String getName() {
	    return "forward";
	}
	
	/**
	 * Execute the forward command.  Forwards data from incoming servlet
	 * request to a port on the local machine.  Presumably, an RMI server
	 * will be reading the data that this method sends.
	 *
	 * @param req   The servlet request.
	 * @param res   The servlet response.
	 * @param param Port to which data will be sent.
	 */
	public void execute(HttpServletRequest req, HttpServletResponse res, 
			    String param) 
	    throws ServletClientException, ServletServerException, IOException {
	    
	    int port;
	    try {
		port = Integer.parseInt(param);
	    } catch (NumberFormatException e) {
		throw new ServletClientException("invalid port number: " + 
						 param);
	    }
	    if (port <= 0 || port > 0xFFFF)
		throw new ServletClientException("invalid port: " + port);
	    if (port < 1024)
		throw new ServletClientException("permission denied for port: " 
						 + port);
	    
	    byte buffer[];
	    Socket socket;
	    try {
		socket = new Socket(InetAddress.getLocalHost(), port);
	    } catch (IOException e) {
		throw new ServletServerException("could not connect to " +
						 "local port");
	    }
	    
	    // read client's request body
	    DataInputStream clientIn = 
		new DataInputStream(req.getInputStream());
	    buffer = new byte[req.getContentLength()];
	    try {
		clientIn.readFully(buffer);
	    } catch (EOFException e) {
		throw new ServletClientException("unexpected EOF " + 
						 "reading request body");
	    } catch (IOException e) {
		throw new ServletClientException("error reading request" + 
						 " body");
	    }
	    
	    DataOutputStream socketOut = null;
	    // send to local server in HTTP
	    try {
		socketOut =
		    new DataOutputStream(socket.getOutputStream());
		socketOut.writeBytes("POST / HTTP/1.0\r\n");
		socketOut.writeBytes("Content-length: " +
				     req.getContentLength() + "\r\n\r\n");
		socketOut.write(buffer);
		socketOut.flush();
	    } catch (IOException e) {
		throw new ServletServerException("error writing to server");
	    }
	    
	    // read response from local server
	    DataInputStream socketIn;
	    try {
		socketIn = new DataInputStream(socket.getInputStream());
	    } catch (IOException e) {
		throw new ServletServerException("error reading from " + 
						 "server");
	    }
	    String key = "Content-length:".toLowerCase();
	    boolean contentLengthFound = false;
	    String line;
	    int responseContentLength = -1;
	    do {
		try {
		    line = socketIn.readLine();
		} catch (IOException e) {
		    throw new ServletServerException("error reading from server");
		}
		if (line == null)
		    throw new ServletServerException(
		        "unexpected EOF reading server response");
		
		if (line.toLowerCase().startsWith(key)) {
		    if (contentLengthFound)
			; // what would we want to do in this case??
		    responseContentLength =
			Integer.parseInt(line.substring(key.length()).trim());
		    contentLengthFound = true;
		}
	    } while ((line.length() != 0) &&
		     (line.charAt(0) != '\r') && (line.charAt(0) != '\n'));
	    
	    if (!contentLengthFound || responseContentLength < 0)
		throw new ServletServerException(
		    "missing or invalid content length in server response");
	    buffer = new byte[responseContentLength];
	    try {
		socketIn.readFully(buffer);
	    } catch (EOFException e) {
		throw new ServletServerException(
		    "unexpected EOF reading server response");
	    } catch (IOException e) {
		throw new ServletServerException("error reading from server");
	    }
	    
	    // send local server response back to servlet client
	    res.setStatus(HttpServletResponse.SC_OK);
	    res.setContentType("application/octet-stream");
	    res.setContentLength(buffer.length);
	    
	    try {
		OutputStream out = res.getOutputStream();
		out.write(buffer);
		out.flush();
	    } catch (IOException e) {
		throw new ServletServerException("error writing response");
	    } finally {
		socketOut.close();
		socketIn.close();
	    }
	}
    }
    
    /**
     * Class that has an execute method to return the host name of the
     * server as the response body.
     */
    protected static class ServletGethostnameCommand 
	implements RMICommandHandler 
    {
	
	public String getName() {
	    return "gethostname";
	}
	
	public void execute(HttpServletRequest req, HttpServletResponse res, 
			    String param) 
	    throws ServletClientException, ServletServerException, IOException {
	    
	    byte[] getHostStringBytes = req.getServerName().getBytes();
	    
	    res.setStatus(HttpServletResponse.SC_OK);
	    res.setContentType("application/octet-stream");
	    res.setContentLength(getHostStringBytes.length);
	    
	    OutputStream out = res.getOutputStream();
	    out.write(getHostStringBytes);
	    out.flush();
	}
    }
    
    /**
     * Class that has an execute method to return an OK status to
     * indicate that connection was successful.  
     */
    protected static class ServletPingCommand implements RMICommandHandler {
	
	public String getName() {
	    return "ping";
	}
	
	public void execute(HttpServletRequest req, HttpServletResponse res, 
			    String param) 
	    throws ServletClientException, ServletServerException, IOException {
	    
	    res.setStatus(HttpServletResponse.SC_OK);
	    res.setContentType("application/octet-stream");
	    res.setContentLength(0);
	}
    }
    
    /**
     * Class that has an execute method to return a human readable
     * message describing which host name is available to local Java
     * VMs.  
     */
    protected static class ServletTryHostnameCommand 
	implements RMICommandHandler 
    {
	
	public String getName() {
	    return "hostname";
	}
	

	public void execute(HttpServletRequest req, HttpServletResponse res, 
			    String param) 
	    throws ServletClientException, ServletServerException, IOException {
	    
	    PrintWriter pw = res.getWriter();
	    
	    pw.println("");
	    pw.println("<HTML>" +
		       "<HEAD><TITLE>Java RMI Server Hostname Info" +
		       "</TITLE></HEAD>" +
		       "<BODY>");
	    pw.println("<H1>Java RMI Server Hostname Info</H1>");
	    pw.println("<H2>Local host name available to Java VM:</H2>");
	    pw.print("<P>InetAddress.getLocalHost().getHostName()");
	    try {
		String localHostName = InetAddress.getLocalHost().getHostName();
		
		pw.println(" = " + localHostName);
	    } catch (UnknownHostException e) {
		pw.println(" threw java.net.UnknownHostException");
	    }
	    
	    pw.println("<H2>Server host information obtained through Servlet " + 
		       "interface from HTTP server:</H2>");
	    pw.println("<P>SERVER_NAME = " + req.getServerName());
	    pw.println("<P>SERVER_PORT = " + req.getServerPort());
	    pw.println("</BODY></HTML>");
	}
    }
    
    /**
     * ServletClientException is thrown when an error is detected
     * in a client's request.
     */
    protected static class ServletClientException extends Exception {
	
	public ServletClientException(String s) {
	    super(s);
	}
    }
    
    /**
     * ServletServerException is thrown when an error occurs here on the server.
     */
    protected static class ServletServerException extends Exception {
	
	public ServletServerException(String s) {
	    super(s);
	}
    }
}
