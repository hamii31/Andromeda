package com.example.hummingbird_lnm;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
	
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	private TextView resultsText;
	private TextView statusText;
	private ArrayAdapter<String> deviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		
		resultsText = findViewById(R.id.resultsText);
		statusText = findViewById(R.id.statusText);
		
		Button discoverButton = findViewById(R.id.discoverButton);
		ListView deviceList = findViewById(R.id.deviceList);
		
		deviceAdapter = new ArrayAdapter<>(
		    this,
			android.R.layout.simple_list_item_1,
			new ArrayList<>()
		);
		
		deviceList.setAdapter(deviceAdapter);
		
		discoverButton.setOnClickListener(view -> {
			discoverButton.setEnabled(false);
			deviceAdapter.clear();
			resultsText.setText("");
			statusText.setText("Discovering devices on local networks...");
			
			executor.execute(() -> {
				List<String> hosts = LocalNetMonitor.findResponsiveHostsOnLocalNetworks();
				
				runOnUiThread(() -> {
					deviceAdapter.clear();
					deviceAdapter.addAll(hosts);
					deviceAdapter.notifyDataSetChanged();
					
					statusText.setText("Found " + hosts.size() + " responsive device(s).");
					
					discoverButton.setEnabled(true);
				});
			});
		});
		
		deviceList.setOnItemClickListener((parent, view, position, id) -> {
			String target = deviceAdapter.getItem(position);
			
			if (target == null) {
				return;
			}
			
			resultsText.setText("Scanning " + target + "..\n\n");
			
			executor.execute(() -> {
				List<PortScanner.PortResult> results = PortScanner.scanCommonTCPPorts(target);
				
				runOnUiThread(() -> {
					for(PortScanner.PortResult result : results) {
						resultsText.append(result + "\n");
					}
				});
			});
		});
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		executor.shutdownNow();
	}
}