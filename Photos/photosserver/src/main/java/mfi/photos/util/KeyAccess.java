package mfi.photos.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import mfi.photos.shared.AES;

public class KeyAccess {

	private static KeyAccess instance;

	private static final Object monitor = new Object();

	private String uptime = null;

	private byte[] key = null;

	static {
		instance = new KeyAccess();
	}

	private KeyAccess() {
		// noop
	}

	public static KeyAccess getInstance() {
		if (instance == null) {
			synchronized (monitor) {
				if (instance == null) {
					instance = new KeyAccess();
				}
			}
		}
		return instance;
	}

	public boolean isKeySet() {
		return this.key != null && this.key.length > 0;
	}

	public String getKey() {
		if (getUptime() == null) {
			throw new IllegalArgumentException("uptime not set");
		}
		if (key == null) {
			return null;
		}
		ByteArrayInputStream in = new ByteArrayInputStream(this.key);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		AES.decrypt(getUptime().toCharArray(), in, out, 0, -1);
		String key = new String(out.toByteArray(), StandardCharsets.UTF_8);
		return key;
	}

	public void setKey(String key) {
		if (getUptime() == null) {
			throw new IllegalArgumentException("uptime not set");
		}
		ByteArrayInputStream in = new ByteArrayInputStream(key.getBytes(StandardCharsets.UTF_8));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		AES.encrypt(0, getUptime().toCharArray(), in, out);
		this.key = out.toByteArray();
	}

	public String getUptime() {
		return uptime;
	}

	public void setUptime(String uptime) {
		this.uptime = uptime;
	}

}
