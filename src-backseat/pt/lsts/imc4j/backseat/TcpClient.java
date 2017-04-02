/*
 * Below is the copyright agreement for IMCJava.
 * 
 * Copyright (c) 2010-2017, Laboratório de Sistemas e Tecnologia Subaquática
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of IMC, LSTS, IMCJava nor the names of its 
 *       contributors may be used to endorse or promote products derived from 
 *       this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL LABORATORIO DE SISTEMAS E TECNOLOGIA SUBAQUATICA
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package pt.lsts.imc4j.backseat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashSet;

import pt.lsts.imc4j.msg.Abort;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.util.SerializationUtils;

/**
 * @author zp
 *
 */
public class TcpClient extends Thread {

	private Socket socket = null;
	private HashSet<ImcConsumer> consumers = new HashSet<ImcConsumer>();
	private boolean connected = false;
	private InputStream input;
	private OutputStream output;
	public int remoteSrc = 0;
	public int localSrc = 0x555;

	public void connect(String host, int port) throws Exception {
		socket = new Socket(host, port);
		connected = true;
		this.input = socket.getInputStream();
		this.output = socket.getOutputStream();
		start();

	}

	@Override
	public void run() {
		while (connected) {
			synchronized (socket) {
				try {
					while (input.available() >= 22) {
						Message m = SerializationUtils.deserializeMessage(input);
						if (m != null)
							dispatch(m);
					}
				} catch (Exception e) {
					try {
						socket.close();
						socket = null;
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				return;
			}
		}
	}

	public void send(Message m) throws IOException {
		m.dst = remoteSrc;
		m.src = localSrc;
		
		
		synchronized (socket) {
			try {
				output.write(m.serialize());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		System.out.println(m);

	}

	private void dispatch(Message m) {
		if (remoteSrc == 0)
			remoteSrc = m.src;
		
		for (ImcConsumer consumer : consumers)
			consumer.onMessage(m);
		
	}

	public synchronized void register(Object pojo) {
		PeriodicCallbacks.register(pojo);
		consumers.add(ImcConsumer.create(pojo));
	}

	public synchronized void unregister(Object pojo) {
		PeriodicCallbacks.unregister(pojo);
		ImcConsumer c = null;
		for (ImcConsumer consumer : consumers) {
			if (consumer.getPojo() == pojo) {
				c = consumer;
				break;
			}
		}

		if (c != null)
			consumers.remove(c);
	}

	public static void main(String[] args) throws Exception {
		TcpClient client = new TcpClient();
		client.connect("127.0.0.1", 6002);
		client.start();
		Thread.sleep(5000);
		client.send(new Abort());
		client.interrupt();
	}
}
