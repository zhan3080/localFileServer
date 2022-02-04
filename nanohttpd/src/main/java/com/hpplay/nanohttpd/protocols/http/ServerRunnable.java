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

import com.hpplay.nanohttpd.NanoHTTPDLog;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * The runnable that will be used for the main listening thread.
 */
public class ServerRunnable implements Runnable {

    private final String TAG = "ServerRunnable";

    private NanoHTTPD httpd;

    private final int timeout;

    private IOException bindException;

    private boolean hasBinded = false;

    private IServerListener mServerListener;

    private int[] allowedPorts;

    public interface IServerListener {
        void onStart();

        void onStop();
    }

    public void setServerListener(IServerListener serverListener) {
        mServerListener = serverListener;
    }

    public void setAllowedPorts(int... allowedPorts) {
        this.allowedPorts = allowedPorts;
    }

    public ServerRunnable(NanoHTTPD httpd, int timeout) {
        this.httpd = httpd;
        this.timeout = timeout;
    }

    @Override
    public void run() {
        if (null == allowedPorts || allowedPorts.length <= 0) {
            if (!bindSocket(httpd.hostname, httpd.myPort)) {
                this.bindException = httpd.resetMyServerSocket();
                if (this.bindException != null) {
                    return;
                }
                // 指定端口创建不成功，则由系统去分配端口
                boolean isBind = bindSocket(httpd.hostname, 0);
                if (!isBind) {
                    this.bindException = new IOException(" Socket bind failed");
                    return;
                }
            }
        } else {
            // 指定端口范围
            boolean isBind = false;
            for (int port : allowedPorts) {
                isBind = bindSocket(httpd.hostname, port);
                if (isBind) {
                    NanoHTTPDLog.i(TAG, "port " + port + "is bind");
                    break;
                }
            }
            if (!isBind) {
                // 所有端口都绑定失败，抛出异常
                this.bindException = new IOException("java.lang.Exception: Socket" + Arrays.toString(allowedPorts) + " bind failed");
                return;
            }
        }
        if (mServerListener != null) {
            mServerListener.onStart();
        }
        do {
            try {
                final Socket finalAccept = httpd.getMyServerSocket().accept();
                if (this.timeout > 0) {
                    finalAccept.setSoTimeout(this.timeout);
                }
                NanoHTTPDLog.i(TAG, "CommunicationfinalAccept:" + finalAccept);
                final InputStream inputStream = finalAccept.getInputStream();
                NanoHTTPDLog.i(TAG, "CommunicationfinalAccept inputStream:" + inputStream);
                httpd.asyncRunner.exec(httpd.createClientHandler(finalAccept, inputStream));
            } catch (IOException e) {
                NanoHTTPDLog.w(TAG, "Communication with the client broken", e);
                break;
            }
        } while (!httpd.getMyServerSocket().isClosed());

        if (mServerListener != null) {
            mServerListener.onStop();
        }
    }

    private boolean bindSocket(String hostName, int port) {
        try {
            httpd.getMyServerSocket().bind(hostName != null ? new InetSocketAddress(hostName, port) : new InetSocketAddress(port));
            httpd.myPort = httpd.getMyServerSocket().getLocalPort();
            return hasBinded = true;
        } catch (IOException e) {
            NanoHTTPDLog.w(TAG, e);
        }
        return false;
    }

    public IOException getBindException() {
        return bindException;
    }

    public boolean hasBinded() {
        return hasBinded;
    }
}
