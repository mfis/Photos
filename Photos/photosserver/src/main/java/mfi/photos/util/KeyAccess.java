package mfi.photos.util;

import mfi.photos.shared.AES;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class KeyAccess {

	private static KeyAccess instance;

	private static final Object monitor = new Object();

	private String uptime;

	private byte[] key;

	private KeyAccess() {
		key = null;
		uptime = String.valueOf(System.currentTimeMillis());
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

	public void reset() {
		instance = new KeyAccess();
	}

	public boolean isKeySet() {
		return this.key != null && this.key.length > 0;
	}

	public String getKey(){
		if (key == null) {
			return null;
		}
		try(
			final var in = new ByteArrayInputStream(this.key);
			final var out = new ByteArrayOutputStream()){
				AES.decrypt(getUptime().toCharArray(), in, out, 0, -1);
				String key = new String(out.toByteArray(), StandardCharsets.UTF_8);
				return key;
		} catch (IOException e) {
			throw new IllegalStateException("StreamHandling failed.", e);
		}
	}

	public void setKey(String key) {
		try(
			final var in = new ByteArrayInputStream(key.getBytes(StandardCharsets.UTF_8));
			final var out = new ByteArrayOutputStream()){
				AES.encrypt(0, getUptime().toCharArray(), in, out);
				this.key = out.toByteArray();
		} catch (IOException e) {
			throw new IllegalStateException("StreamHandling failed.", e);
		}
	}

	public String getUptime() {
		return uptime;
	}

	public void setUptime(String uptime) {
		this.uptime = uptime;
	}

}
