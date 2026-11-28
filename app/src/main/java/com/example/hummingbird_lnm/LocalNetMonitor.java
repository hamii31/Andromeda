package com.example.hummingbird_lnm;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.InterfaceAddress;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

public final class LocalNetMonitor {
	private static final int TIMEOUT_MS = 750;
	private static final int MAX_WORKERS = 8;
	private static final List<ServicePort> COMMON_TCP_PORTS = Arrays.asList(
        new ServicePort(20, "FTP-data"),
        new ServicePort(21, "FTP"),
        new ServicePort(22, "SSH"),
        new ServicePort(23, "Telnet"),
        new ServicePort(25, "SMTP"),
        new ServicePort(53, "DNS"),
        new ServicePort(80, "HTTP"),
		new ServicePort(81, "HTTP-alt"),
        new ServicePort(88, "Kerberos"),
        new ServicePort(110, "POP3"),
        new ServicePort(111, "RPCbind"),
		new ServicePort(115, "SFTP (legacy)"),
        new ServicePort(135, "MS-RPC"),
        new ServicePort(139, "NetBIOS-SSN"),
        new ServicePort(143, "IMAP"),
        new ServicePort(179, "BGP"),
        new ServicePort(389, "LDAP"),
        new ServicePort(443, "HTTPS"),
		new ServicePort(444, "HTTPS-alt"),
        new ServicePort(445, "SMB"),
	    new ServicePort(464, "Kerberos pass change"),
        new ServicePort(465, "SMTPS"),
		new ServicePort(513, "rlogin"),
		new ServicePort(514, "rsh/Syslog"),
        new ServicePort(515, "LPD"),
        new ServicePort(548, "AFP"),
        new ServicePort(554, "RTSP"),
        new ServicePort(587, "SMTP-Submission"),
		new ServicePort(593, "Microsoft RPC"),
        new ServicePort(631, "IPP"),
        new ServicePort(636, "LDAPS"),
		new ServicePort(749, "Kerberos admin"),
		new ServicePort(853, "DNS TLS"),
        new ServicePort(873, "rsync"),
		new ServicePort(989, "FTPS-data"),
		new ServicePort(990, "FTPS"),
        new ServicePort(993, "IMAPS"),
        new ServicePort(995, "POP3S"),
		new ServicePort(1099, "Java RMI registry"),
		new ServicePort(1194, "OpenVPN"),
        new ServicePort(1433, "MSSQL"),
		new ServicePort(1434, "MSSQL Browser"),
        new ServicePort(1521, "Oracle"),
		new ServicePort(1522, "Oracle-alt"),
        new ServicePort(1723, "PPTP"),
		new ServicePort(1812, "RADIUS-auth"),
		new ServicePort(1813, "RADIUS-accounting"),
		new ServicePort(1830, "Oracle Connection Manager"),
        new ServicePort(1883, "MQTT"),
		new ServicePort(1935, "RTMP"),
		new ServicePort(2003, "Graphite plaintext protocol"),
		new ServicePort(2004, "Graphite pickle protocol"),
        new ServicePort(2049, "NFS"),
		new ServicePort(2525, "SMTP-alt submission"),
        new ServicePort(2375, "Docker"),
        new ServicePort(2376, "Docker-TLS"),
		new ServicePort(2377, "Docker Swarm"),
		new ServicePort(3000, "Grafana / Gitea / Dev server"),
		new ServicePort(3001, "Dev server"),
		new ServicePort(3002, "Dev server"),
		new ServicePort(3003, "Dev server"),
		new ServicePort(3004, "Dev server"),
		new ServicePort(3005, "Dev server"),
		new ServicePort(3050, "Firebird"),
        new ServicePort(3268, "LDAP-GC"),
        new ServicePort(3269, "LDAPS-GC"),
        new ServicePort(3306, "MySQL"),
		new ServicePort(3307, "MySQL-alt"),
        new ServicePort(3389, "RDP"),
		new ServicePort(4000, "GraphQL / Dev server"),
		new ServicePort(4222, "NATS client connections"),
		new ServicePort(4223, "NATS TLS client connections"),
        new ServicePort(5000, "UPnP / HTTP-alt / Docker Registry"),
		new ServicePort(5001, "App service"),
		new ServicePort(5044, "Logstash Beats input"),
        new ServicePort(5357, "WSDAPI"),
        new ServicePort(5432, "PostgreSQL"),
		new ServicePort(5433, "PostgreSQL-alt"),
		new ServicePort(5601, "Kibana"),
		new ServicePort(5671, "AMQP TLS"),
		new ServicePort(5672, "AMQP / RabbitMQ"),
        new ServicePort(5900, "VNC"),
		new ServicePort(5901, "VNC"),
		new ServicePort(5902, "VNC"),
		new ServicePort(5984, "CouchDB"),
        new ServicePort(5985, "WinRM"),
        new ServicePort(5986, "WinRM-TLS"),
        new ServicePort(6379, "Redis"),
		new ServicePort(6380, "Redis TLS"),
        new ServicePort(6443, "Kubernetes-API"),
		new ServicePort(7000, "Apache Cassandra"),
		new ServicePort(7001, "Apache Cassandra TLS"),
		new ServicePort(7199, "Apache Cassandra JMX"),
		new ServicePort(7473, "Neo4j HTTPS"),
		new ServicePort(7474, "Neo4j HTTP"),
		new ServicePort(7687, "Neo4j Bolt"),
		new ServicePort(7990, "Bitbucket Server HTTP"),
		new ServicePort(7999, "Bitbucket Server SSH"),
        new ServicePort(8000, "HTTP-alt"),
		new ServicePort(8001, "HTTP-alt"),
        new ServicePort(8008, "HTTP-alt"),
        new ServicePort(8009, "AJP/Chromecast"),
        new ServicePort(8080, "HTTP-proxy"),
		new ServicePort(8081, "HTTP-alt"),
		new ServicePort(8082, "HTTP-alt"),
		new ServicePort(8083, "HTTP-alt"),
		new ServicePort(8085, "Web app service"),
		new ServicePort(8088, "Web app service"),
		new ServicePort(8090, "Web app service"),
		new ServicePort(8123, "Clickhouse HTTP"),
		new ServicePort(8181, "HTTP-alt"),
		new ServicePort(8200, "HashiCorp Vault"),
        new ServicePort(8443, "HTTPS-alt"),
		new ServicePort(8500, "Consul HTTP API / UI"),
		new ServicePort(8554, "RTSP"),
		new ServicePort(8600, "Consul DNS"),
        new ServicePort(8883, "MQTT-TLS"),
        new ServicePort(8888, "HTTP-alt"),
		new ServicePort(9000, "Web app service"),
		new ServicePort(9042, "Apache Cassandra CQL"),
		new ServicePort(9090, "Prometheus admin"),
		new ServicePort(9091, "Prometheus Pushgateway"),
		new ServicePort(9092, "Apache Kafka"),
		new ServicePort(9093, "Apache Kafka TLS"),
		new ServicePort(9160, "Apache Cassandra Thrift"),
        new ServicePort(9100, "JetDirect / Prometheus Node Exporter"),
        new ServicePort(9200, "Elasticsearch"),
		new ServicePort(9300, "Elasticsearch transport protocol"),
		new ServicePort(9418, "Git native protocol"),
		new ServicePort(9443, "HTTPS-alt"),
		new ServicePort(9600, "Logstash mon API"),
        new ServicePort(10000, "Webmin"),
		new ServicePort(10250, "Kubernetes kubelet API"),
		new ServicePort(10255, "Kubernetes kubelet read-only API"),
		new ServicePort(10257, "Kubernetes controller manager metrics"),
		new ServicePort(10259, "Kubernetes scheduler metrics"),
		new ServicePort(11211, "Memcached"),
		new ServicePort(12201, "Graylog GELF"),
		new ServicePort(15671, "RabbittMQ management TLS"),
		new ServicePort(15672, "RabbittMQ management UI"),
        new ServicePort(27017, "MongoDB"),
		new ServicePort(27018, "MongoDB shard server"),
		new ServicePort(27019, "MongoDB config server"),
		new ServicePort(32400, "Plex Media Server"),
		new ServicePort(47001, "Windows Remote Management"),
		new ServicePort(50000, "Common device or printer admin service"),
		new ServicePort(61613, "STOMP"),
		new ServicePort(61614, "STOMP TLS"),
		new ServicePort(61616, "Apache ActiveMQ OpenWire"),
        new ServicePort(62078, "iPhone-sync")
    );

	
	public static final class ServicePort {
		public final int port;
		public final String service;
		
		public ServicePort(int port, String service) {
			this.port = port;
			this.service = service;
		}
	}
    
    public enum PortState { OPEN, CLOSED, FILTERED, UNREACHABLE }
	
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
    
    public static PortState probe(String host, int port, int timeoutMs) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(host, port), timeoutMs);
            return PortState.OPEN;
        } catch (SocketTimeoutException e) {
            return PortState.FILTERED;
        } catch (ConnectException e) {
            Throwable c = e.getCause();
            if (c instanceof android.system.ErrnoException && ((android.system.ErrnoException) c).errno == android.system.OsConstants.ECONNREFUSED) {
                return PortState.CLOSED; // RST: host is alive
            }
            return PortState.UNREACHABLE;
        } catch (IOException e) {
            return PortState.UNREACHABLE;
        }
    }
	
	public static PortState probe(String host, int port) {
		return probe(host, port, TIMEOUT_MS);
	}
	
	public static List<PortResult> scanCommonTCPPorts(String host) {
		ExecutorService executor = Executors.newFixedThreadPool(MAX_WORKERS);
		
		try {
			List<Future<PortResult>> futures = new ArrayList<>();
			
			for (ServicePort servicePort : COMMON_TCP_PORTS) {
				futures.add(executor.submit(() -> {
					PortState state = probe(host, servicePort.port);
					
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
	
    public static List<String> findResponsiveHosts(String subnetPrefix) {
       final int timeoutMs = 250;
       final int workers = 16;

       ExecutorService executor = Executors.newFixedThreadPool(workers);

       int[] probePorts = {
              22, 53, 80, 135, 139, 443, 445,
              548, 554, 631, 1883, 3389, 
              5000, 5357, 7000, 8008, 8009,
              8080, 9100, 62078};
        try {
            List<Future<String>> futures = new ArrayList<>();

            for(int host = 1; host <= 254; host++) {
                String ip = subnetPrefix + host;
                
				futures.add(executor.submit(() -> {
                    try {
                        if (InetAddress.getByName(ip).isReachable(timeoutMs)) {
                            return ip;
                        }
                    } catch (IOException ignored) {
                        // ignore and fall back to tcp scan
                    }
                    
                    for (int port : probePorts) {
                        PortState state = probe(ip, port, timeoutMs);
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
					
					responsiveHosts.addAll(findResponsiveHosts(subnetPrefix));
				}
			}
		} catch (SocketException ex) {
			// Return all results before the error
		}
		
		return responsiveHosts;
	}
}
