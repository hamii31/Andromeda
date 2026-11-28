package com.example.hummingbird_lnm;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

public final class TCPProber {
	private static final int TIMEOUT_MS = 750;
	
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
	
}