package org.tramaci.ntu;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Date;

	public class Config {

		public int GarbageFreq = 2000;									//Frequenza di Garbage collector per le Onion e le connessioni inattive.
			
		public int MaxConnectionXPort = 10;							//Max. Connessioni per porta per onion
		public int MaxConnectionIdle=600000;						//TTL Per un proxy onion su una porta intattivo
		public NetArea LocalNetArea=null;								//Network loncale di assegnamento ip 127.0.0.0
		public InetAddress LocalNet = null;								//Indirizzo ip (rete) locale tipicamente 127.0.0.0
		public InetAddress TorIP = null;									//Indirizzo IP di tor 127.0.0.1
		public int TorPort= 9150;												//Porta SOCKS4A ti tor 9050
	
		public InetAddress[] NoIP = null;									//Lista IP da non usare in LocalNet
		public int[] NoPort = null;											//Lista di porte da non usare per le onion.
		
		public NetArea NetAllow = null;									//Rete in cui è consentito operare
		public InetAddress[] NetAllowIp = null;						//Ip che possono operare
		public InetAddress[] NetNoAllowIp = null;					//Ip che non possono operare
		public boolean NetDisallowAll=false;							//Disattiva tutto eccetto ciò che può
		public String LogFile = null;
		public int DefaultPort = 80;											//Default port.
		public boolean Debug = false;									//Debug log
		public boolean LogStdout = false;								//Copia log in stdout

		public static void echo(String st) { Main.echo(st); }
		
		@SuppressWarnings("resource")
		public static Config LoadFromFile(String filepath) throws Exception {
			DataInputStream in;
			BufferedReader br;
			Config C = new Config();
			String RunBanner=null;
			
			FileInputStream F = new FileInputStream(filepath);
			int line=0;
	
			try {
					in = new DataInputStream(F);
					br = new BufferedReader(new InputStreamReader(in));
					String li = null;
					while((li=br.readLine())!=null) {
						line++;
						li = li.trim();
						if (li.length()==0) continue;
						if (li.charAt(0) =='#') continue;
						String[] tok = li.split("\\#",2);

						li = tok[0];
						li = li.trim();
						if (li.length()==0) continue;
						tok = li.split("\\s+");
						String cmd = tok[0].toLowerCase();
						boolean fc=false;
						
				
						if (cmd.compareTo("torip")==0) { fc=true; C.TorIP = ParseIp(tok[1]); }
												
						if (cmd.compareTo("netallow")==0) {
							fc=true;
							if (tok[1].toLowerCase().contains("local")) C.NetAllow = C.LocalNetArea; 
							else if (tok[1].toLowerCase().contains("all")) C.NetAllow = null; 
							else C.NetAllow = ParseNet(tok[1]);
							}
						
						if (cmd.compareTo("logfile")==0) {
							fc=true;
								if (tok[1].toLowerCase().compareTo("stdout")==0) C.LogFile=null; else {
									C.LogFile=tok[1];
									try {
										File Fi = new File(C.LogFile);
										if (!Fi.exists()) Main.file_put_bytes(C.LogFile, new byte[] {32} );
										} catch(Exception EP) { throw new Exception("Log file error `"+tok[1]+"`"); }
									}
								}
						
					
						
						if (cmd.compareTo("runbanner")==0) {
								fc=true;
								String tk[] = li.split("\\s+",2);
								if (RunBanner==null) RunBanner="";
								RunBanner+="\n"+tk[1];
								}
			
						if (cmd.compareTo("netdefaultdeny")==0) { fc=true; C.NetDisallowAll=Config.parseY(tok[1]); }
						if (cmd.compareTo("defaultonionport")==0) { 
								fc=true; 
								C.DefaultPort=Integer.parseInt(tok[1]);
								if (C.DefaultPort<1 || C.DefaultPort>65535) throw new Exception("Invalid default port "+C.DefaultPort);
								}
						
						if (cmd.compareTo("maxconnectionidle")==0) { fc=true; C.MaxConnectionIdle=(int)(Integer.parseInt(tok[1])*1000); }
						if (cmd.compareTo("maxconnectionxport")==0) { fc=true; C.MaxConnectionXPort=(int)(Integer.parseInt(tok[1])); }
						if (cmd.compareTo("torport")==0) { fc=true; C.TorPort=(int)(Integer.parseInt(tok[1])); }
						if (cmd.compareTo("debug")==0) { fc=true; C.Debug = Config.parseY(tok[1]); }
						if (cmd.compareTo("logtostdout")==0) { fc=true; C.LogStdout = Config.parseY(tok[1]); }
			
						if (cmd.compareTo("ntu")==0) { // NTU 127.0.0.x port onion port
							if (tok.length!=5) throw new Exception("NTU requires 4 parameter: Local_ip local_port remote_host remote_port");
							if (Main.SOCKS==null) Main.SOCKS = new SocksProxy[0];
							int cx =Main.SOCKS.length;
							SocksProxy[] t = new SocksProxy[cx+1];
							System.arraycopy(Main.SOCKS, 0, t, 0, cx);
							t[cx] = new SocksProxy(C,tok[3].toLowerCase().trim() , Config.ParseIp(tok[1].trim()), Config.parseInt(tok[4], "Remote Port",0,65535), Config.parseInt(tok[2], "Local Port",0,65535));
							Main.SOCKS = t;
							fc=true;
						}
						
						if (cmd.compareTo("noports")==0) try {
							fc=true;
							C.NoPort = new int[tok.length-1];
							int t1 = C.NoPort.length;
							for (int t2=0;t2<t1;t2++) {
								C.NoPort[t2] = Integer.parseInt(tok[1+t2]);
								if (C.NoPort[t2]<0 || C.NoPort[t2]>65535) throw new Exception();
								}
							} catch(Exception FG) { throw new Exception("Invalid port"); }
						
						if (cmd.compareTo("nolocalip")==0) { fc=true; C.NoIP = ParseIPList(tok, false, true,"none empty nothing nobody unused"); }
						if (cmd.compareTo("netallowip")==0) { fc=true; C.NetAllowIp = ParseIPList(tok,true,true,"all"); }
						if (cmd.compareTo("netdenyip")==0) { fc=true; C.NetNoAllowIp = ParseIPList(tok,true,true,"none empty nothing nobody unused"); }
						if (cmd.length()==0) fc=true;
						if (!fc) throw new Exception("Unknown parameter `"+cmd+"`");
					}
					F.close();
					try {	in.close(); } catch(Exception FQ) {}
				
					int cx = C.NoPort.length;
					for (int ax=0;ax<cx;ax++) if (C.NoPort[ax]==C.DefaultPort) {
						echo("\nWarning:\t\nDefault onion port "+C.DefaultPort+" blocked by NoPort!\n\n");
						break;
						}
										
					
					if (RunBanner!=null) {
						RunBanner=RunBanner.replace("\\t", "\t");
						RunBanner=RunBanner.replace("\\r", "\r");
						RunBanner=RunBanner.replace("\\n", "\n");
						RunBanner=RunBanner.replace("\\b",new String(new byte[] {7}));
						RunBanner=RunBanner.replace("\\\\", "\\");
						echo(RunBanner+"\n");
						}
					
					return C;
			} catch(Exception E) {
				try {	F.close(); } catch(Exception FQ) {}
				String em = E.getMessage();
				if (em.compareTo("1")==0) em="Syntax Error";
				throw new Exception("Line: "+line+" "+em);
			}
		
		}
				
		private static InetAddress[] ParseIPList(String[] arr,boolean cannull,boolean canempty,String empty) throws Exception {
			int cx = arr.length;
			if (cx<1) {
					if (canempty) throw new Exception("Syntax error: set 1 or more ip address or `"+empty+"`"); else throw new Exception("Syntax error: set 1 or more ip address");
					}	
			
			if (cx==2 && empty.contains(arr[1].toLowerCase())) {
				if (!canempty) throw new Exception("This can't be empty or nothing!");
				if (cannull) return null; else return new InetAddress[0];
				}
			
			String last="???";
			try {
				
					InetAddress[] re = new InetAddress[arr.length-1];
							int t1 =re.length;
							for (int t2=0;t2<t1;t2++) {
								last=arr[t2+1];
								re[t2] = ParseIp(arr[t2+1]);
								}
							
					return re;	
			} catch(Exception E) { throw new Exception("Invalid IP address `"+last+"`"); }
			
		}
		
		public static int parseInt(String st,String name,int min,int max) throws Exception {
			int p =min-1;
			try { p = Integer.parseInt(st); } catch(Exception E) {}
			if (p<min || p>max) throw new Exception("Invalid "+name+" `"+st+"` (must be between "+min+" "+max+")");
			return p;
		}
		private static boolean parseY(String s) throws Exception {
			s=s.trim();
			s=s.toLowerCase();
			if (s.compareTo("y")==0) return true;
			if (s.compareTo("yes")==0) return true;
			if (s.compareTo("true")==0) return true;
			if (s.compareTo("enabled")==0) return true;
			if (s.compareTo("enable")==0) return true;
			if (s.compareTo("1")==0) return true;
			if (s.compareTo("n")==0) return false;
			if (s.compareTo("no")==0) return false;
			if (s.compareTo("false")==0) return false;
			if (s.compareTo("disabled")==0) return false;
			if (s.compareTo("disable")==0) return false;
			if (s.compareTo("0")==0) return false;
			throw new Exception("Invalid boolean parameter `"+s+"`");
		}
		
		Config()  {
			try {
			
				LocalNet = InetAddress.getByAddress(new byte[] { 127,0,0,0 });
				
				TorIP = InetAddress.getByAddress(new byte[] { 127,0,0,1 });
				
				NoIP = new InetAddress[] { 
										InetAddress.getByAddress(new byte[] { 127,0,0,1}),
										InetAddress.getByAddress(new byte[] { 127,0,0,2})
										};
				
				NoPort = new int[] { 53 };
				} catch(Exception E) { EXC(E,"Conmfig"); }
			}
		
		private static InetAddress ParseIp(String st) throws Exception {
			try {
				String[] tok = st.split("\\.");
				if (tok.length!=4) throw new Exception();
				byte[] b = new byte[4];
				for (int ax=0;ax<4;ax++) {
					int c = Integer.parseInt(tok[ax]);
					if (c<0 || c>254) throw new Exception();
					b[ax]=(byte)(255&c);
				}
				return InetAddress.getByAddress(b);
				} catch(Exception E) {
					throw new Exception("Invalid IP Address `"+st+"`");
				}
		}
		
		private static NetArea ParseNet(String st) throws Exception {
			try {
				String[] tok = st.split("\\/");
				st=tok[0];
				int Nbt = Integer.parseInt(tok[1]);
				if (Nbt<1 || Nbt>31) throw new Exception();
				tok = st.split("\\.");
				if (tok.length!=4) throw new Exception();
				byte[] b = new byte[4];
				for (int ax=0;ax<4;ax++) {
					int c = Integer.parseInt(tok[ax]);
					if (c<0 || c>254) throw new Exception();
					b[ax]=(byte)(255&c);
				}
				//Nbt = 32-Nbt;
				if (Nbt<0 || Nbt>0xFFFFFFFFL) throw new Exception();
				return new NetArea( InetAddress.getByAddress(b) ,Nbt);
				
				} catch(Exception E) {
					throw new Exception("Invalid Network Area `"+st+"`");
				}
		}
		
		
		public void EXC(Exception E,String Dove) {
			String St = "FatalError `"+E.getMessage()+"` in `"+Dove+"`\n";
			Log(St);
		}
		
		public void Log(String St) {
			Date D = new Date();
			String h = (D.getYear()+1900)+"-"+(D.getMonth()+1)+"-"+D.getDate()+" "+D.getHours()+":"+D.getMinutes()+":"+D.getSeconds()+"."+(System.currentTimeMillis() % 1000);
			h+="                                                                                                                              ";
			h = h.substring(0, 25);
			St =h+"\t"+St.trim()+"\n";
			
			if (LogFile==null) echo(St); else {
				
				PrintWriter out = null;
					try {
					    out = new PrintWriter(new BufferedWriter(new FileWriter(LogFile, true)));
					    out.println(St);
					    if (LogStdout) echo(St);
					} catch (Exception e) {
					    echo("Log Error "+St);
					} finally {
					    if (out != null) {
					        try {
					            out.close();
					        } catch (Exception ignore) {
					        }
					    }
					}
				
			}
			
		}
	}