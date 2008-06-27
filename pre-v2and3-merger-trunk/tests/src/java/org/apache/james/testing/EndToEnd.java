/***********************************************************************
 * Copyright (c) 2000-2004 The Apache Software Foundation.             *
 * All rights reserved.                                                *
 * ------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License"); you *
 * may not use this file except in compliance with the License. You    *
 * may obtain a copy of the License at:                                *
 *                                                                     *
 *     http://www.apache.org/licenses/LICENSE-2.0                      *
 *                                                                     *
 * Unless required by applicable law or agreed to in writing, software *
 * distributed under the License is distributed on an "AS IS" BASIS,   *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or     *
 * implied.  See the License for the specific language governing       *
 * permissions and limitations under the License.                      *
 ***********************************************************************/

package org.apache.james.testing;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.SocketException;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.commons.net.pop3.POP3Client;
import org.apache.commons.net.smtp.SMTPClient;
import org.apache.commons.net.telnet.TelnetClient;

/**
 * A class to do end to end load SMTP testing on James.
 *
 *
 * $Id: EndToEnd.java,v 1.10 2004/01/30 02:22:20 noel Exp $
 */
public class EndToEnd extends TestCase {

    /**
     * The number of worker threads this test invokes
     */
    private int numWorkers = 10;

    /**
     * The size of messages generated by this test case.
     */
    private int messageSize = 1024 * 10;

    /**
     * The number of still-active worker threads.
     */
    private int workingWorkers;

    /**
     * The start time for this test case.
     */
    private Date start;

    /**
     * A set of commands to issue to the James RemoteManager interface
     * to begin the test.
     */
    private String[] script1 =
            { "root", "root", "help", "adduser test test", "listusers", "quit" };

    /**
     * A set of commands to issue to the James RemoteManager interface
     * to clean up after the test.
     */
    private String[] script2 =
            { "root", "root", "listusers", "deluser test", "listusers", "quit" };

    /**
     * Whether all the worker threads have finished.
     */
    private boolean finished = false;

    /**
     * The number of emails delivered as part of this EndToEnd test case.
     */
    private int delivered = 0;

    /**
     * Constructor for RemoteManagerEndToEnd.
     * @param arg0
     */
    public EndToEnd(String arg0) {
        super(arg0);
    }

    /**
     * Main method used to invoke this test case..
     *
     * @param args Ignored
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(EndToEnd.class);
    }

    /**
     * Run the full end to end test, doing the James setup and teardown
     * as well as the mail transmission.
     */
    public void testEndToEnd() {
        TelnetClient client = new TelnetClient();
        BufferedReader in;
        OutputStreamWriter out;
        try {
            client.setDefaultTimeout(500);
            client.connect("127.0.0.1", 4555);
            runTelnetScript(client, script1);
            client.disconnect();
            SMTPTest();
            POP3Test();
            //client.connect("127.0.0.1", 4555);
            //runTelnetScript(client, script2);
            //client.disconnect();

        } catch (SocketException e) {
            e.printStackTrace();
            assertTrue(false);
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    private void runTelnetScript(TelnetClient client, String[] script) throws IOException {
        BufferedReader in;
        OutputStreamWriter out;
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        OutputStream oo = client.getOutputStream();
        out = new OutputStreamWriter(oo);
        print(in, System.out);
        for (int i = 0; i < script.length; i++) {
            out.write(script[i] + "\n");
            System.out.println(" " + script[i] + " \n");
            out.flush();
            print(in, System.out);
        }
    }

    private void echo(OutputStreamWriter out) {}

    /**
     * Helper method to print data from the input reader into a
     * String.
     *
     * @param in the Reader that serves as the source of data
     * @param output This param is unused.  Why is it here?
     */
    private String print(BufferedReader in, OutputStream output) {
        String outString = "";
        try {
            String readString = in.readLine();
            while (readString != null) {
                outString += readString + "\n";
                readString = in.readLine();
            }
        } catch (IOException e) {// e.printStackTrace();
            // assertTrue(false);
        }
        System.out.println(outString);
        return outString + "==";
    }

    /**
     * Carries out theSMTPmail" aspects of the tests, spawning the appropriate
     * worker threads.
     */
    private void SMTPTest() {
        start = new Date();
        StringBuffer mail1 = new StringBuffer();
        mail1.append(
                "Subject: test\nFrom: postmaster@localhost\nTo: test@localhost\n\nTHIS IS A TEST");
        for (int kb = 0; kb < messageSize; kb++) {
            mail1.append("m");
        }
        String mail = mail1.toString();
        SMTPDeliveryWorker[] workers = new SMTPDeliveryWorker[numWorkers];
        Thread[] threads = new Thread[workers.length];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new SMTPDeliveryWorker(new SMTPClient(), mail, this);
            workers[i].setWorkerid(i);
            threads[i] = new Thread((SMTPDeliveryWorker) workers[i]);
        }
        for (int i = 0; i < workers.length; i++) {
            System.out.println("starting worker:" + i);
            ((Thread) threads[i]).start();
            workingWorkers++;
        }
        while (!finished) {// TODO: Shouldn't this busy loop be replaced with an
            // appropriate timed wait?
        }

        long time = (new Date()).getTime() - start.getTime();
        System.err.println("time total " + (int) time);
    }

    /**
     *  End to end test of POP3 functionality.
     */
    private void POP3Test() {
        try {
            POP3Client pclient = new POP3Client();
            pclient.connect("127.0.0.1", 110);
            System.out.println(pclient.getReplyString());
            pclient.login("test", "test");
            System.out.println(pclient.getReplyString());
            pclient.setState(POP3Client.TRANSACTION_STATE);
            pclient.listMessages();
            System.out.println(pclient.getReplyString());
            pclient.disconnect();
            System.out.println(pclient.getReplyString());
        } catch (SocketException e) {
            e.printStackTrace();
            assertTrue(false);
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Record that a particular worker thread has finished.
     *
     * @param workerid the id number of the thread that has finished.
     */
    public void finished(int workerid) {
        workingWorkers--;
        System.out.println("workers still working.." + workingWorkers);
        if (workingWorkers == 0) {
            long time = (new Date()).getTime() - start.getTime();
            System.err.println("time to deliver set " + (int) (time / 1000));
            // System.err.println("messages per second " + (int)(1000/(time/1000)));
            // System.err.println("data rate="+((messageSize*1000)/(time/1000)));
            finished = true;
        }
    }

    /**
     * Prints out the number of delivered emails.
     */
    public void delivered() {
        System.out.println("-" + (++delivered));
    }
}