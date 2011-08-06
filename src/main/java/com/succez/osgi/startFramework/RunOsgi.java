package com.succez.osgi.startFramework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.FrameworkFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RunOsgi {
	private static final String WEBAPP_BUNDLES_DIR = "/WEB-INF/bundles";

	private static final String FELIX_CACHE_DIR = "felix-cache";

	private static final Logger logger = LoggerFactory.getLogger(RunOsgi.class);
	
	private Felix felix;

	private String webapp;
	
	public RunOsgi(String webapp) {
		this.webapp = webapp;
	}

	public void run() {
		logger.info("begin to run osgi");
		
		try {
			removeFelixCache();
	 
			doStart();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void doStart() throws Exception {
		FrameworkFactory fact = new FrameworkFactory();
		final Felix tmp = (Felix) fact.newFramework(createConfig());
		
		this.felix = tmp;
		
		tmp.init();
		logger.info("OSGi framework starting...");
		tmp.start();
		logger.info("OSGi framework started");

		logger.info("install bundles");
		installBundles();
		
		printBundleList();
	}

	private void installBundles() throws Exception {
		BundleContext bundleContext = this.felix.getBundleContext();

        ArrayList<Bundle> installed = new ArrayList<Bundle>();
        List<URL> bundles = findBundles();
		for (URL url : bundles) {
            logger.info("Installing bundle [" + url + "]");
            Bundle bundle = bundleContext.installBundle(url.toExternalForm());
            installed.add(bundle);
        }

        for (Bundle bundle : installed) {
            logger.info("Starting bundle [" + bundle + "]");
            bundle.start();
        }
        
        if(bundles.size()==0){
        	logger.info("find no bundles");
        }
	}

	@SuppressWarnings("unchecked")
	private List<URL> findBundles() throws MalformedURLException {
		File bundlesDir = new File(getWebApp(), WEBAPP_BUNDLES_DIR);
		File[] bundles = bundlesDir.listFiles();
		if(bundles == null){
			logger.debug("not find bundles in directory:"+bundlesDir);
			return Collections.EMPTY_LIST;
		}

		List<URL> result = new ArrayList<URL>();
		for(File f : bundles){
			result.add(f.toURL());
		}
		return result;
	}

	private void removeFelixCache() throws IOException {
		File f = new File(FELIX_CACHE_DIR);
		if(f.exists()){
			logger.debug("remove felix cache directory:"+f.getAbsolutePath());
			FileUtils.forceDelete(f);
		}
	}

	private void printBundleList() {
		Bundle[] bundles = felix.getBundleContext().getBundles();
		logger.debug("has "+bundles.length+" bundles:");
		for(Bundle b : bundles){
			logger.debug("\t"+b.toString()+",location is:"+b.getResource("META-INF/MANIFEST.MF"));
		}
	}

	public void stop() {
		try {
			doStop();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void doStop() throws Exception {
		if (this.felix != null) {
			this.felix.stop();
		}

		logger.info("OSGi framework stopped");
	}

	private Map<String, Object> createConfig() throws Exception {
		Properties props = new Properties();
		InputStream is = getFrameworkPropertiesFile();
		try{
			props.load(is);
		}finally{
			if(is!=null) is.close();
		}
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		for (Object key : props.keySet()) {
			map.put(key.toString(), props.get(key));
		}

		return map;
	}

	private InputStream getFrameworkPropertiesFile() throws FileNotFoundException {
		return getClass().getResourceAsStream("framework.properties");
	}

	private String getWebApp() {
		return this.webapp;
	}
	 
	public static void main(String args[]) throws Exception{
		String webapp = "F:/workdir/osgi-framework/target/webapp";
		RunOsgi run = new RunOsgi(webapp);
		run.run();
		run.felix.waitForStop(0);
	}
}
