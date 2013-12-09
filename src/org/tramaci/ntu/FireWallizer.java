package org.tramaci.ntu;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;


public class FireWallizer {
	
	public static boolean IPCan(Config C,SocketAddress ip)  {
		InetSocketAddress isa = (InetSocketAddress) ip;
		return IPCan(C,isa.getAddress());
	}
	
	public static boolean IPCan(Config C,InetAddress ip)  {
		
		if (C.NetAllowIp!=null) {
			int cx = C.NetAllowIp.length;
			for (int ax=0;ax<cx;ax++) {
				if (C.NetAllowIp[ax]==null) continue;
				if (C.NetAllowIp[ax].equals(ip)) return true;
				}
			}
		
		if (C.NetNoAllowIp!=null) {
			int cx = C.NetNoAllowIp.length;
			for (int ax=0;ax<cx;ax++) {
				if (C.NetNoAllowIp[ax]==null) continue;
				if (C.NetNoAllowIp[ax].equals(ip)) return false;
				}
			}
		
		if (C.NetAllow!=null) return C.NetAllow.isInNet(ip);
		
		if (C.NetDisallowAll) return false;
		return true;
	}	
}
