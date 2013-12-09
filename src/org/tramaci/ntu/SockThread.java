package org.tramaci.ntu;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


public class SockThread extends Thread{
	private Socket conc = null;
	private Socket cons = null;
	private InputStream C = null;
	private OutputStream S = null;
	private SocksConnection SOK = null;
	public boolean running=true;
	SockThread(Socket cli,Socket srv, SocksConnection sok) throws Exception  {
		super();
		conc = cli;
		cons = srv;
		SOK=sok;
		C = conc.getInputStream();
		S = cons.getOutputStream();
		start();
	}
	
	public void run() {
		running=true;
		while(running) try {
				int i = C.available();
				if (i<1) i=1;
				if (i>8192) i=8192;
				byte[] buf = new byte[i];
				SOK.Refresh();
				C.read(buf);
				if (conc.isClosed() || cons.isClosed()) break;
				S.write(buf);
				buf=null;
				} catch(Exception E) { 
					running=false;
					break;
				}
			try { conc.close(); } catch(Exception E) {}
			try { cons.close(); } catch(Exception E) {}
			SOK.EndTime = 0;
			running=false;
			}
}
