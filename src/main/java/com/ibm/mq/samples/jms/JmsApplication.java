/*
* (c) Copyright IBM Corporation 2018
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.ibm.mq.samples.jms;


import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.TextMessage;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

/**
 * A minimal and simple application for Point-to-point messaging.
 *
 * Application makes use of fixed literals, any customisations will require
 * re-compilation of this source file. Application assumes that the named queue
 * is empty prior to a run.
 *
 * Notes:
 *
 * API type: JMS API (v2.0, simplified domain)
 *
 * Messaging domain: Point-to-point
 *
 * Provider type: IBM MQ
 *
 * Connection mode: Client connection
 *
 * JNDI in use: No
 *
 */
public class JmsApplication {

	// System exit status value (assume unset value to be 1)
	private static int status = 1;

	// Create variables for the connection to MQ
//	private static final String HOST = "localhost"; // Host name or IP address
//	private static final int PORT = 1414; // Listener port for your queue manager
	private static final String CHANNEL = "MEBT.REMOTE.CONN.TLS"; // Channel name
	private static final String QMGR = "AEQT"; // Queue manager name
	private static final String APP_USER = "admin"; // User name that application uses to connect to MQ
	private static final String APP_PASSWORD = "passw0rd"; // Password that the application uses to connect to MQ
	private static final String QUEUE_NAME = "EAU.ISO20OUT.REQUEST.QL.MEBGLBK"; // Queue that the application uses to put and get messages to and from


	/**
	 * Main method
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		// Variables
		JMSContext context = null;
		Destination destination = null;
		JMSProducer producer = null;
		JMSConsumer consumer = null;

		String host = "localhost";
		if(args.length > 0){
			host = args[0];
		}
		int port = 1414;
		if(args.length > 1){
			port = Integer.valueOf(args[1]);
		}
		String channel = CHANNEL;
		if(args.length > 2){
			channel = args[2];
		}
		boolean auth = true;
		if(args.length > 3){
			auth = Boolean.valueOf(args[3]);
		}


		System.out.println("Host: " + host + " : port: " + port);


		try {
			// Create a connection factory
			JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
			JmsConnectionFactory cf = ff.createConnectionFactory();

			// Set the properties
			cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, host);
			cf.setIntProperty(WMQConstants.WMQ_PORT, port);

			cf.setStringProperty(WMQConstants.WMQ_CHANNEL, channel);
			cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
			cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, QMGR);
			cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, "JmsApplication (JMS)");


			if(auth){
				cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
				cf.setStringProperty(WMQConstants.USERID, APP_USER);
				cf.setStringProperty(WMQConstants.PASSWORD, APP_PASSWORD);
			} else {
				cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, false);
			}

			if(args.length > 4){
//			System.setProperty("javax.net.ssl.trustStore",args[3]);
//				cf.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SPEC, "TLS_RSA_WITH_AES_256_CBC_SHA256");
				cf.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SPEC, args[4]);
			}

			// Create JMS objects
			context = cf.createContext();
			destination = context.createQueue("queue:///" + QUEUE_NAME);

			long uniqueNumber = System.currentTimeMillis() % 1000;
			TextMessage message = context.createTextMessage("Your lucky number today is " + uniqueNumber);

			producer = context.createProducer();
			producer.send(destination, message);
			System.out.println("Sent message:\n" + message);

			consumer = context.createConsumer(destination); // autoclosable
			String receivedMessage = consumer.receiveBody(String.class, 15000); // in ms or 15 seconds

			System.out.println("\nReceived message:\n" + receivedMessage);

			recordSuccess();
		} catch (JMSException jmsex) {
			recordFailure(jmsex);
		}

		System.exit(status);

	} // end main()

	/**
	 * Record this run as successful.
	 */
	private static void recordSuccess() {
		System.out.println("SUCCESS");
		status = 0;
		return;
	}

	/**
	 * Record this run as failure.
	 *
	 * @param ex
	 */
	private static void recordFailure(Exception ex) {
		if (ex != null) {
			if (ex instanceof JMSException) {
				processJMSException((JMSException) ex);
			} else {
				System.out.println(ex);
			}
		}
		System.out.println("FAILURE");
		status = -1;
		return;
	}

	/**
	 * Process a JMSException and any associated inner exceptions.
	 *
	 * @param jmsex
	 */
	private static void processJMSException(JMSException jmsex) {
		System.out.println(jmsex);
		Throwable innerException = jmsex.getLinkedException();
		if (innerException != null) {
			System.out.println("Inner exception(s):");
		}
		while (innerException != null) {
			System.out.println(innerException);
			innerException = innerException.getCause();
		}
		return;
	}

}
