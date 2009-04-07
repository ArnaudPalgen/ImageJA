package rmi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


import java.lang.reflect.Method;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Server implements Hello {

	public Server() {}

	int counter = 0;

	public String sayHello() {
		return "Hello, world (" + (++counter) + ")!";
	}

	public static String getStubPath() {
		return System.getProperty("java.io.tmpdir") + "/ImageJ-"
			+ System.getProperty("user.name") + ".stub";
	}

	public static void setPrivate(String path) {
		try {
			// File.setReadable() is Java 6
			Class[] types = { boolean.class, boolean.class };
			Method m = File.class.getMethod("setReadable", types);
			Object[] arguments = { Boolean.FALSE, Boolean.FALSE };
			m.invoke(new File(path), arguments);
			arguments = new Object[] { Boolean.TRUE, Boolean.TRUE };
			m.invoke(new File(path), arguments);
			return;
		} catch (Exception e) {
			System.err.println("Java < 6 detected, trying chmod");
		}
		try {
			String[] command = {
				"chmod", "0600", path
			};
			Runtime.getRuntime().exec(command);
		} catch (Exception e) {}
	}

	public static boolean client(String[] args) {
		String file = args.length < 1 ? Server.getStubPath() : args[0];
		try {
			FileInputStream in = new FileInputStream(file);
			Hello hello =
				(Hello)new ObjectInputStream(in).readObject();
			in.close();

			String response = hello.sayHello();
			System.out.println("response: " + response);
			return true;
		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
		}
		return false;
	}

	public static void main(String[] args) {
		if (client(args))
			return;

		try {
			Server obj = new Server();
			Hello stub = (Hello)
				UnicastRemoteObject.exportObject(obj, 0);

			// Write serialized object
			String path = getStubPath();
			FileOutputStream out = new FileOutputStream(path);
			setPrivate(path);
			new ObjectOutputStream(out).writeObject(stub);
			out.close();

			System.err.println("Server ready");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}
}
