package com.example.hummingbird_lnm;

import java.io.IOException;
import java.util.List; 
import java.util.ArrayList;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

public final class HostDiscovery {
	private static final int TIMEOUT_MS = 250;
	private static final int WORKERS = 32;
	
    public static List<String> findResponsiveHosts(String subnetPrefix) {

       ExecutorService executor = Executors.newFixedThreadPool(WORKERS);

       int[] probePorts = {

              22, 80, 443, 445, 62078, // most common first
			  53, 135, 139, 548, 554, 
			  631, 1883, 3389, 
              5000, 5357, 7000, 8008, 8009,
              8080, 9100
	    };
		
        try {
            List<Future<String>> futures = new ArrayList<>();

            for(int host = 1; host <= 254; host++) {
                String ip = subnetPrefix + host;
                
				futures.add(executor.submit(() -> {
                    try {
						if (InetAddress.getByName(ip).isReachable(TIMEOUT_MS)) {
							return ip;
						}
					} catch (IOException ignored) {
						// ignore and fall back to tcp scan
					}
					
                    for (int port : probePorts) {
                        PortState state = TCPProber.probe(ip, port, TIMEOUT_MS);
                        if (state == PortState.OPEN || state == PortState.CLOSED) {
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

                    if(ip != null) responsiveHosts.add(ip);
                                        
                } catch (Exception ex) {}
            }

			return responsiveHosts;

        } finally {
            executor.shutdownNow();
        }
    }
}
