package org.tramaci.ntu;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;



public class SocksConnection  {
	public long EndTime=-1;
		
	private Socket CON = null;
	private Socket TOR = null;
	
	private SockThread Cl = null;
	private SockThread Sr = null;
	
	private Config Config = null;
		
	public void Refresh() { EndTime=System.currentTimeMillis()+Config.MaxConnectionIdle; }
	
	SocksConnection(Socket con,Config C,String Onion,int port) throws Exception {

		InputStream TI = null;
		OutputStream TO = null;
		
		Config=C;
		
		Onion = Onion.toLowerCase().trim();
		
		EndTime=System.currentTimeMillis()+Config.MaxConnectionIdle;
		CON=con;
	
		int cx =0;

		if (Config.Debug) Config.Log("OnionConnection ["+Onion+"]\n");
		
		TOR = new Socket(Config.TorIP,Config.TorPort);
		TI = TOR.getInputStream();
		TO = TOR.getOutputStream();
		
		int dx = Onion.length();
		cx = 10+dx;
		byte[] req = new byte[cx];
		req[0] = 4;	//Socks 4
		req[1] = 1; //Connect
		PokeB(2, port, req);
		req[7] = 1; //Socks 4/a ip  0.0.0.1
		int bp = 9; //user "" Hsot:
		for (int ax=0;ax<dx;ax++) req[bp++] = (byte) (255&Onion.codePointAt(ax));

		TO.write(req);
		req = new byte[8];
		TI.read(req);

		if (req[1]!=0x5a) {
			try { CON.close(); } catch(Exception E) {}
			try { TOR.close(); } catch(Exception E) {}
			End();
			int rc = 255&req[1];
			if (rc==91) Config.Log("SOCKS: ["+Onion+"] Request failed!\n"); else Config.echo("SOCKS: ["+Onion+"] Tor: SOCKS Server Error "+rc+"!\n");
			throw new Exception("Socks: Error H"+Long.toHexString((long)(255&req[1])).toUpperCase());
			}
		
		EndTime=System.currentTimeMillis()+Config.MaxConnectionIdle;
		Cl = new SockThread(CON,TOR,this);
		Sr = new SockThread(TOR,CON,this);
	}
	
	public boolean connected() { return CON.isConnected() && TOR.isConnected(); }
		
	public void End() {
		try { CON.close(); } catch(Exception E) {}
		try { TOR.close(); } catch(Exception E) {}
		if (Cl!=null) {
			Cl.running=false;
			try { Cl.interrupt(); } catch(Exception E) {}
			}
		
		if (Sr!=null) {
			Sr.running=false;
			try { Sr.interrupt(); } catch(Exception E) {}
			}
	}

	public static void PokeB(int addr,int valu, byte[] ram) {
		ram[addr+1] = (byte)(255&valu);
		ram[addr] = (byte)(255&(valu>>8));
		
	}
	
}
