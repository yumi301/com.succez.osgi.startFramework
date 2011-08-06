package com.succez.osgi.startFramework;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.FrameworkFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

public final class CopyOfRunOsgi {
	private Felix felix;

	public void run() throws Exception {
		HashMap<String, String> configuration = new HashMap<String, String>();
		
		//每次启动osgi framework时清空上次的cache。测试阶段一般要这么做。
		configuration.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

		FrameworkFactory fact = new FrameworkFactory();
		Felix tmp = (Felix) fact.newFramework(configuration);
		
		this.felix = tmp;
		
		tmp.init();
		tmp.start();

		install("com.succez.osgi.helloworld-1.0.0-SNAPSHOT.jar");
		install("org.apache.felix.gogo.runtime-0.8.0.jar");
		install("org.apache.felix.gogo.command-0.8.0.jar");
		install("org.apache.felix.gogo.shell-0.8.0.jar");
	}
	
	public void install(String file) throws Exception{
		BundleContext bundleContext = this.felix.getBundleContext();
		URL url = this.getClass().getResource(file);
		InputStream is = this.getClass().getResourceAsStream(file);
		try{
			Bundle bundle = bundleContext.installBundle(url.toExternalForm(), is);
			bundle.start();
		}finally{
			is.close();
		}
	}

	public void stop() throws BundleException {
		if (this.felix != null) {
			this.felix.stop();
		}
	}
	 
	public static void main(String args[]) throws Exception{
		CopyOfRunOsgi run = new CopyOfRunOsgi();
		run.run();
		run.felix.waitForStop(0);
	}
}
