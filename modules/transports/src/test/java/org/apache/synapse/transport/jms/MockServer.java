/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.transport.jms;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.transport.testkit.listener.Channel;
import org.apache.synapse.transport.testkit.server.AsyncEndpoint;
import org.apache.synapse.transport.testkit.server.Endpoint;
import org.apache.synapse.transport.testkit.server.Server;

public class MockServer extends Server<JMSListenerSetup> {
    static Log log = LogFactory.getLog(MockServer.class);
    
    private Channel<?> channel;
    
    public MockServer(JMSListenerSetup setup) {
        super(setup);
    }

    @Override
    public void start(Channel<?> channel) throws Exception {
        this.channel = channel;
        channel.getSetup().setUp();
        channel.setUp();
    }

    @Override
    public void stop() throws Exception {
        channel.tearDown();
        channel.getSetup().tearDown();
    }

    @Override
    public AsyncEndpoint createAsyncEndpoint(String contentType) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public Endpoint createEchoEndpoint(String contentType) throws Exception {
        // TODO: should not have cast here
        final JMSRequestResponseChannel channel = (JMSRequestResponseChannel)this.channel;
        JMSListenerSetup setup = getSetup();
        
        Destination destination = channel.getDestination();
        Destination replyDestination = channel.getReplyDestination();
        final Connection connection = setup.getConnectionFactory(destination).createConnection();
        connection.start();
        final Connection replyConnection = setup.getConnectionFactory(replyDestination).createConnection();
        final Session replySession = replyConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        final MessageProducer producer = replySession.createProducer(replyDestination);
        MessageConsumer consumer = connection.createSession(false, Session.AUTO_ACKNOWLEDGE).createConsumer(destination);
        consumer.setMessageListener(new MessageListener() {
            public void onMessage(Message message) {
                try {
                    log.info("Message received: ID = " + message.getJMSMessageID());
                    Message reply;
                    if (message instanceof BytesMessage) {
                        reply = replySession.createBytesMessage();
                        IOUtils.copy(new BytesMessageInputStream((BytesMessage)message), new BytesMessageOutputStream((BytesMessage)reply));
                    } else if (message instanceof TextMessage) {
                        reply = replySession.createTextMessage();
                        ((TextMessage)reply).setText(((TextMessage)message).getText());
                    } else {
                        // TODO
                        throw new UnsupportedOperationException("Unsupported message type");
                    }
                    reply.setJMSCorrelationID(message.getJMSMessageID());
                    Thread.sleep(50);
                    producer.send(reply);
                    log.info("Message sent: ID = " + reply.getJMSMessageID());
                } catch (Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        
        return new Endpoint() {
            public String getEPR() throws Exception {
                return "jms:/" + channel.getDestinationName() + "?transport.jms.DestinationType=" + channel.getDestinationType() + "&java.naming.factory.initial=org.mockejb.jndi.MockContextFactory&transport.jms.ConnectionFactoryJNDIName=QueueConnectionFactory";
            }

            public void remove() throws Exception {
                connection.close();
                replyConnection.close();
            }
        };
    }
}
