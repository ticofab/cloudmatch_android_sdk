package com.codebutler.android_websockets;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Base64;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.message.BasicLineParser;
import org.apache.http.message.BasicNameValuePair;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class WebSocketClient {
    private final URI mURI;
    private final Listener mListener;
    private Socket mSocket;
    private Thread mThread;
    private final HandlerThread mHandlerThread;
    private final Handler mHandler;
    private final List<BasicNameValuePair> mExtraHeaders;
    private final HybiParser mParser;
    private boolean mConnected;

    private final Object mSendLock = new Object();

    private static TrustManager[] sTrustManagers;

    public static void setTrustManagers(final TrustManager[] tm) {
        sTrustManagers = tm;
    }

    public WebSocketClient(final URI uri, final Listener listener, final List<BasicNameValuePair> extraHeaders) {
        mURI = uri;
        mListener = listener;
        mExtraHeaders = extraHeaders;
        mConnected = false;
        mParser = new HybiParser(this);

        mHandlerThread = new HandlerThread("websocket-thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    public Listener getListener() {
        return mListener;
    }

    public void connect() {
        if (mThread != null && mThread.isAlive()) {
            return;
        }

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final int port = (mURI.getPort() != -1) ? mURI.getPort()
                            : ((mURI.getScheme().equals("wss") || mURI.getScheme().equals("https")) ? 443 : 80);

                    String path = TextUtils.isEmpty(mURI.getPath()) ? "/" : mURI.getPath();
                    if (!TextUtils.isEmpty(mURI.getQuery())) {
                        path += "?" + mURI.getQuery();
                    }

                    final String originScheme = mURI.getScheme().equals("wss") ? "https" : "http";
                    final URI origin = new URI(originScheme, "//" + mURI.getHost(), null);

                    final SocketFactory factory = (mURI.getScheme().equals("wss") || mURI.getScheme().equals(
                            "https")) ? getSSLSocketFactory() : SocketFactory.getDefault();
                    mSocket = factory.createSocket(mURI.getHost(), port);

                    final PrintWriter out = new PrintWriter(mSocket.getOutputStream());
                    final String secretKey = createSecret();
                    out.print("GET " + path + " HTTP/1.1\r\n");
                    out.print("Upgrade: websocket\r\n");
                    out.print("Connection: Upgrade\r\n");
                    out.print("Host: " + mURI.getHost() + "\r\n");
                    out.print("Origin: " + origin.toString() + "\r\n");
                    out.print("Sec-WebSocket-Key: " + secretKey + "\r\n");
                    out.print("Sec-WebSocket-Version: 13\r\n");
                    if (mExtraHeaders != null) {
                        for (final NameValuePair pair : mExtraHeaders) {
                            out.print(String.format("%s: %s\r\n", pair.getName(), pair.getValue()));
                        }
                    }
                    out.print("\r\n");
                    out.flush();

                    final HybiParser.HappyDataInputStream stream = new HybiParser.HappyDataInputStream(
                            mSocket.getInputStream());

                    // Read HTTP response status line.
                    final StatusLine statusLine = parseStatusLine(readLine(stream));
                    if (statusLine == null) {
                        throw new HttpException("Received no reply from server.");
                    } else if (statusLine.getStatusCode() != HttpStatus.SC_SWITCHING_PROTOCOLS) {
                        throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
                    }

                    // Read HTTP response headers.
                    String line;
                    while (!TextUtils.isEmpty(line = readLine(stream))) {
                        final Header header = parseHeader(line);
                        if (header.getName().equals("Sec-WebSocket-Accept")) {
                            final String expected = expectedKey(secretKey);
                            if (expected == null) {
                                throw new SocketException("SHA-1 algorithm not found");
                            } else if (!expected.equals(header.getValue())) {
                                throw new SocketException("Invalid Sec-WebSocket-Accept, expected: " + expected
                                        + ", got: " + header.getValue());
                            }
                        }
                    }

                    mListener.onConnect();

                    mConnected = true;

                    // Now decode websocket frames.
                    mParser.start(stream);

                } catch (final EOFException ex) {
                    mListener.onDisconnect(0, "EOF");
                    mConnected = false;

                } catch (final ConnectException ex) {
                    mListener.onDisconnect(0, "Connection Refused");
                    mConnected = false;

                } catch (final SSLException ex) {
                    // Connection reset by peer
                    mListener.onDisconnect(0, "SSL");
                    mConnected = false;

                } catch (final IOException e) {
                    mListener.onError(e);

                } catch (final URISyntaxException e) {
                    mListener.onError(e);

                } catch (final KeyManagementException e) {
                    mListener.onError(e);

                } catch (final NoSuchAlgorithmException e) {
                    mListener.onError(e);

                } catch (final HttpException e) {
                    mListener.onError(e);
                }
            }
        });
        mThread.start();
    }

    public void disconnect() {
        if (mSocket != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mSocket != null) {
                        try {
                            mSocket.close();
                        } catch (final IOException ex) {
                            mListener.onError(ex);
                        }
                        mSocket = null;
                    }
                    mConnected = false;
                }
            });
        }
    }

    public void send(final String data) {
        sendFrame(mParser.frame(data));
    }

    public void send(final byte[] data) {
        sendFrame(mParser.frame(data));
    }

    public boolean isConnected() {
        return mConnected;
    }

    private StatusLine parseStatusLine(final String line) {
        if (TextUtils.isEmpty(line)) {
            return null;
        }
        return BasicLineParser.parseStatusLine(line, new BasicLineParser());
    }

    private Header parseHeader(final String line) {
        return BasicLineParser.parseHeader(line, new BasicLineParser());
    }

    // Can't use BufferedReader because it buffers past the HTTP data.
    private String readLine(final HybiParser.HappyDataInputStream reader) throws IOException {
        int readChar = reader.read();
        if (readChar == -1) {
            return null;
        }
        final StringBuilder string = new StringBuilder("");
        while (readChar != '\n') {
            if (readChar != '\r') {
                string.append((char) readChar);
            }

            readChar = reader.read();
            if (readChar == -1) {
                return null;
            }
        }
        return string.toString();
    }

    private String expectedKey(final String secret) {
        // concatenate, SHA1-hash, base64-encode
        try {
            final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            final String secretGUID = secret + GUID;
            final MessageDigest md = MessageDigest.getInstance("SHA-1");
            final byte[] digest = md.digest(secretGUID.getBytes());
            return Base64.encodeToString(digest, Base64.DEFAULT).trim();
        } catch (final NoSuchAlgorithmException e) {
            return null;
        }
    }

    private String createSecret() {
        final byte[] nonce = new byte[16];
        for (int i = 0; i < 16; i++) {
            nonce[i] = (byte) (Math.random() * 256);
        }
        return Base64.encodeToString(nonce, Base64.DEFAULT).trim();
    }

    void sendFrame(final byte[] frame) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (mSendLock) {
                        final OutputStream outputStream = mSocket.getOutputStream();
                        outputStream.write(frame);
                        outputStream.flush();
                    }
                } catch (final IOException e) {
                    mListener.onError(e);
                }
            }
        });
    }

    public interface Listener {
        public void onConnect();

        public void onMessage(String message);

        public void onMessage(byte[] data);

        public void onDisconnect(int code, String reason);

        public void onError(Exception error);
    }

    private SSLSocketFactory getSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        final SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, sTrustManagers, null);
        return context.getSocketFactory();
    }
}