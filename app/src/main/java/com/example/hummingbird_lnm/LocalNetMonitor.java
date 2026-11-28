package com.example.hummingbird_lnm;

import java.net.InterfaceAddress;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

public final class LocalNetMonitor {

	public static List<String> findResponsiveHostsOnLocalNetworks() {
		List<String> responsiveHosts = new ArrayList<>();
		
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			
			while(interfaces.hasMoreElements()) {
				NetworkInterface netInterface = interfaces.nextElement();
				
				if (!netInterface.isUp() || netInterface.isLoopback() || netInterface.isVirtual()) continue;
				
				for (InterfaceAddress interAddress : netInterface.getInterfaceAddresses()) {
					InetAddress address = interAddress.getAddress();
					
					if(!(address instanceof Inet4Address)) continue;
					
					int prefixLength = interAddress.getNetworkPrefixLength();
					
				    // Find only /24 lans
					if (prefixLength != 24) continue;
					
					String localIp = address.getHostAddress();
					String subnetPrefix = localIp.substring(0, localIp.lastIndexOf('.') + 1);
					
					responsiveHosts.addAll(HostDiscovery.findResponsiveHosts(subnetPrefix));
				}
			}
		} catch (SocketException ex) {
			// Return all results before the error
		}
		
		return responsiveHosts;
	}
}