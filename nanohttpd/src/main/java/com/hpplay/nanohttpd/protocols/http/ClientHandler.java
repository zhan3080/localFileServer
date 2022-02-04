package com.hpplay.nanohttpd.protocols.http;

/*
 * #%L
 * NanoHttpd-Core
 * %%
 * Copyright (C) 2012 - 2016 nanohttpd
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import android.util.Log;

import com.hpplay.nanohttpd.NanoHTTPDLog;
import com.hpplay.nanohttpd.protocols.http.tempfiles.ITempFileManager;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;


/**
 * The runnable that will be used for every new client connection.
 */
public class ClientHandler implements Runnable {

    private final String TAG = "ClientHandler";

    private final NanoHTTPD httpd;

    private final InputStream inputStream;

    private final Socket acceptSocket;

    public ClientHandler(NanoHTTPD httpd, InputStream inputStream, Socket acceptSocket) {
        Log.i(TAG, "createClientHandler acceptSocket: " + acceptSocket + " inputStream:" + inputStream);
        this.httpd = httpd;
        this.inputStream = inputStream;
        this.acceptSocket = acceptSocket;
    }

    public void close() {
        NanoHTTPD.safeClose(this.inputStream);
        NanoHTTPD.safeClose(this.acceptSocket);
    }

    @Override
    public void run() {
        OutputStream outputStream = null;
        try {
            Log.i(TAG, "ClientHandler run ");
            outputStream = this.acceptSocket.getOutputStream();
            Log.i(TAG, "ClientHandler outputStream: " + outputStream);
            ITempFileManager tempFileManager = httpd.getTempFileManagerFactory().create();
            Log.i(TAG, "ClientHandler tempFileManager: " + tempFileManager);
            Log.i(TAG, "ClientHandler getInetAddress: " + this.acceptSocket.getInetAddress());
            HTTPSession session = new HTTPSession(httpd, tempFileManager, this.inputStream, outputStream, this.acceptSocket.getInetAddress());
            Log.i(TAG, "ClientHandler isClose:" + this.acceptSocket.isClosed());
            while (!this.acceptSocket.isClosed()) {
                Log.i(TAG, "session execute ");
                session.execute();
            }
        } catch (Exception e) {
            // When the socket is closed by the client,
            // we throw our own SocketException
            // to break the "keep alive" loop above. If
            // the exception was anything other
            // than the expected SocketException OR a
            // SocketTimeoutException, print the
            // stacktrace
            if (!(e instanceof SocketException && "NanoHttpd Shutdown".equals(e.getMessage())) && !(e instanceof SocketTimeoutException)) {
                NanoHTTPDLog.w(TAG, "Communication with the client broken, or an bug in the handler code", e);
            }
        } finally {
            NanoHTTPD.safeClose(outputStream);
            NanoHTTPD.safeClose(this.inputStream);
            NanoHTTPD.safeClose(this.acceptSocket);
            httpd.asyncRunner.closed(this);
        }
    }
}
