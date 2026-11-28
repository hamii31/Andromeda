package com.example.hummingbird_lnm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

public final class PortScanner {
	private static final int MAX_WORKERS = 8;
	
    public static final class PortResult {
		public final int port;
		public final String service;
		public final PortState state;
		
		public PortResult(int port, String service, PortState state) {
			this.port = port;
			this.service = service;
			this.state = state;
		}
		
		@Override
		public String toString() {
			return String.format("TCP/%d (%s): %s", port, service, state);
		}
	}
	
    public static List<PortResult> scanCommonTCPPorts(String host) {
		ExecutorService executor = Executors.newFixedThreadPool(MAX_WORKERS);
		
		try {
			List<Future<PortResult>> futures = new ArrayList<>();
			
			for (CommonPorts.ServicePort servicePort : CommonPorts.COMMON_TCP_PORTS) {
				futures.add(executor.submit(() -> {
					PortState state = TCPProber.probe(host, servicePort.port);
					
					return new PortResult(
					    servicePort.port,
						servicePort.service,
						state
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
						PortState.UNREACHABLE
					));
				}
			}
			
			return results;
		} finally {
			executor.shutdownNow();
		}
	}
}