package com.example.hummingbird_lnm;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

public final class LocalNetMonitor {
	private static final int TIMEOUT_MS = 750;
	private static final int MAX_WORKERS = 8;
	
	private static final List<ServicePort> COMMON_TCP_PORTS = Arrays.asList(
	    new ServicePort(22, "SSH"),
		new ServicePort(80, "HTTP"),
		new ServicePort(443, "HTTPS"),
		new ServicePort(445, "SMB"),
		new ServicePort(631, "IPP"),
		new ServicePort(3389, "RDP"),
		new ServicePort(8080, "HTTP-alt")
	);
	
	public static final class ServicePort {
		public final int port;
		public final String service;
		
		public ServicePort(int port, String service) {
			this.port = port;
			this.service = service;
		}
	}
	
	public static final class PortResult {
		public final int port;
		public final String service;
		public final boolean open;
		
		public PortResult(int port, String service, boolean open) {
			this.port = port;
			this.service = service;
			this.open = open;
		}
		
		@Override
		public String toString() {
			return String.format("TCP/%d (%s): %s", port, service, open ? "open" : "closed, filtered, or unavailable");
		}
	}
	
	public static boolean isTCPOpen(String host, int port, int timeoutMs) {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, port), timeoutMs);
			return true;
		} catch (IOException ex) {
			return false;
		}
	}
	
	public static boolean isTCPOpen(String host, int port) {
		return isTCPOpen(host, port, TIMEOUT_MS);
	}
	
	public static List<PortResult> scanCommonTCPPorts(String host) {
		ExecutorService executor = Executors.newFixedThreadPool(MAX_WORKERS);
		
		try {
			List<Future<PortResult>> futures = new ArrayList<>();
			
			for (ServicePort servicePort : COMMON_TCP_PORTS) {
				futures.add(executor.submit(() -> {
					boolean open = isTCPOpen(host, servicePort.port);
					
					return new PortResult(
					    servicePort.port,
						servicePort.service,
						open
					);
				}));
			}
			
			List<PortResult> results = new ArrayList<>();
			
			for (Future<PortResult> future : futures) {
				try {
					results.add(future.get());
				} catch (Exception e) {
					results.add(new PortResult(
					    -1,
						"Scan worker error",
						false
					));
				}
			}
			
			return results;
		} finally {
			executor.shutdownNow();
		}
	}
	
	public static List<String> findResponsiveHosts(String subnetPrefix) {
		final int timeoutMs = 250;
		final int workers = 16;
		
		ExecutorService executor = Executors.newFixedThreadPool(workers);
		
		try {
			List<Future<String>> futures = new ArrayList<>();
			
			for(int host = 1; host <= 254; host++) {
				String ip = subnetPrefix + host;
				
				futures.add(executor.submit(() -> {
					int[] probePorts = {22, 23, 80, 443, 445, 8080};
					
					for (int port : probePorts) {
						if (isTCPOpen(ip, port, timeoutMs)) {
							return ip;
						}
					}
					
					return null;
				}));
				
			}
			
			List<String> responsiveHosts = new ArrayList<>();
			
			for (Future<String> future : futures) {
				try {
					String ip = future.get();
					
					if(ip != null) {
						responsiveHosts.add(ip);
					}
				} catch (Exception ex) {}
			}
			
			return responsiveHosts;
		} finally {
			executor.shutdownNow();
		}
	}
}
