package com.example.hummingbird_lnm;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
	
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	private TextView resultsText;
	private TextView statusText;
	private EditText subnetInput;
	private ArrayAdapter<String> deviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		
		resultsText = findViewById(R.id.resultsText);
		statusText = findViewById(R.id.statusText);
		subnetInput = findViewById(R.id.subnetInput);
		
		Button discoverButton = findViewById(R.id.discoverButton);
		ListView deviceList = findViewById(R.id.deviceList);
		
		deviceAdapter = new ArrayAdapter<>(
		    this,
			android.R.layout.simple_list_item_1,
			new ArrayList<>()
		);
		
		deviceList.setAdapter(deviceAdapter);
		
		discoverButton.setOnClickListener(view -> {
			String subnetPrefix = subnetInput.getText().toString().trim();
			
			if(!isValidSubnetPrefix(subnetPrefix)) {
				statusText.setText("Enter a prefix such as 192.168.1.");
				return;
			}
			
			discoverButton.setEnabled(false);
			deviceAdapter.clear();
			resultsText.setText("");
			statusText.setText("Checking " + subnetPrefix + "1-254...");
			
			executor.execute(() -> {
				List<String> hosts = LocalNetMonitor.findResponsiveHosts(subnetPrefix);
				
				runOnUiThread(() -> {
					deviceAdapter.clear();
					deviceAdapter.addAll(hosts);
					deviceAdapter.notifyDataSetChanged();
					
					statusText.setText(
					    "Found " + hosts.size() + " responsive device(s)."
					);
					
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
				List<LocalNetMonitor.PortResult> results = LocalNetMonitor.scanCommonTCPPorts(target);
				
				runOnUiThread(() -> {
					for(LocalNetMonitor.PortResult result : results) {
						resultsText.append(result + "\n");
					}
				});
			});
		});
    }
	
	private boolean isValidSubnetPrefix(String value) {
		return value.matches(
		    "^(25[0-5]|2[0-4]\\d|1?\\d?\\d)\\." +
			"(25[0-5]|2[0-4]\\d|1?\\d?\\d)\\." +
			"(25[0-5]|2[0-4]\\d|1?\\d?\\d)\\.$"
		);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		executor.shutdownNow();
	}
}

