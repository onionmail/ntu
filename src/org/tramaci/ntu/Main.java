package org.tramaci.ntu;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Main {
	Config Config = new Config();
	public static SocksProxy[] SOCKS = null;
	//public static int intent = 0;
	
	public void Start(String fc) throws Exception {
		
		
		
		try {
			Config = Config.LoadFromFile(fc);
			} catch(Exception E) {
			echo("Config error "+E.getMessage()+"\n");
			return;
			}
		
		if (!TCPRest(Config.TorIP,Config.TorPort)) {
			echo("\nCan't connet via tor `"+Config.TorIP.toString()+":"+Config.TorPort+"`\n");
			System.exit(2);
			}
		
		if (SOCKS==null || SOCKS.length==0) {
			echo("\nNo NTU Proxy!\n");
			System.exit(2);
			}
		
		
		
		echo("Ok\nService Started\n");
		
	}

public static void main(String args[]) 
      {
		Main N=null;
		try {
			String fc = "etc/ntu.conf";
			echo("\nNTU LocalProxy 1.0\n\t(C) 2013 by EPTO (A)\n\t");
			
			int cx = args.length;
			for (int ax=0;ax<cx;ax++) {
				boolean fm=false;
				String cmd = args[ax].toLowerCase().trim();		
				
				if (cmd.compareTo("-f")==0) { 
						fm=true;
						if ((ax+1)>=cx) {
							echo("Error in command line: -f\n\tFile required!\n");
							Helpex();
							return;
							}
						fc = args[ax+1]; 
						ax++;
						}
				
				if (cmd.compareTo("-?")==0 || cmd.compareTo("-h")==0) { 
						Helpex();  
						return; 
						}
				
				if (!fm) {
					echo("Invalid command line parameter `"+cmd+"`\n");
					Helpex(); 
					return;
					}
				
				}
			
			echo("Load Config '"+fc+"'\n");
			N = new Main();
			N.Start(fc);
			if (N.Config==null) { 
				echo("\nCan't start!\n");
				} 
		} catch(Exception E) { 
			if (N!=null && N.Config!=null) { 
				if (N.Config.Debug) EXC(E,"Main");
				} else EXC(E,"Main");
			echo("Fatal Error: "+E.getMessage()+"\n");
			}
      }

	private static void Helpex() {
		echo("\nUse:\n\tntu -f <config file>\n\n");
		}	 

	public static void echo(String st) { System.out.print(st); }
	
	public  static void EXC(Exception E,String dove) {
		echo("\n\nException: "+dove+" = "+E.toString()+"\n"+E.getMessage()+"\n"+E.getLocalizedMessage()+"\n");
							StackTraceElement[] S = E.getStackTrace();
							for (int ax=0;ax<S.length;ax++) echo("STACK "+ax+":\t "+S[ax].toString()+"\n");
		}
	
	public static void file_put_bytes(String name,byte[]  data) throws Exception {
			FileOutputStream fo = new FileOutputStream(name);
			fo.write(data);
			fo.close();
		}	

		
	public static boolean TCPRest(InetAddress ip,int port) {
		Socket sok=null;
		try {
			sok = new Socket(ip,port);
			sok.close();
			return true;
			} catch(Exception E) {
				if (sok!=null) try { sok.close(); } catch(Exception I) {}
				return false;
			}
	}
}
