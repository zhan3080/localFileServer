package com.example.localfileserver;

import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.hpplay.nanohttpd.protocols.http.IHTTPSession;
import com.hpplay.nanohttpd.protocols.http.NanoHTTPD;
import com.hpplay.nanohttpd.protocols.http.request.Method;
import com.hpplay.nanohttpd.protocols.http.response.IStatus;
import com.hpplay.nanohttpd.protocols.http.response.Response;
import com.hpplay.nanohttpd.protocols.http.response.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;


public class LelinkFileServer extends NanoHTTPD {

    private static final String TAG = "LelinkFileServer";

    public LelinkFileServer(String hostname, int port) {
        super(hostname, port);
        // TODO Auto-generated constructor stub
    }
    

    @Override
    protected Response serve(IHTTPSession session) {
        // TODO Auto-generated method stub
        Map<String, String> header = session.getHeaders();
        Map<String, String> parms = session.getParms();
        String uri = session.getUri();
        Log.d(TAG, uri);
        return respond(Collections.unmodifiableMap(header), session, uri);
    }

    private Response respond(Map<String, String> headers, IHTTPSession session,
                             String uri) { // 开始获取
        // First let's handle CORS OPTION query
        Log.i(TAG, " respond  " + uri);
        Response r;
        if (Method.OPTIONS.equals(session.getMethod())) {
            r = Response.newFixedLengthResponse(Status.OK, MIME_PLAINTEXT,
                    null, 0);
        } else {
            r = defaultRespond(headers, session, uri);
        }
        return r; // 返回response
    }

    private Response defaultRespond(Map<String, String> headers,
                                    IHTTPSession session, String uri) {
        // Remove URL arguments
        uri = uri.trim().replace(File.separatorChar, '/');
        if (uri.indexOf('?') >= 0) {
            uri = uri.substring(0, uri.indexOf('?'));
        }
        String mimeTypeForFile = getMimeTypeForFile(uri); // 开始获取http的传输类型
        Response response;
        Log.i(TAG, " uri path  " + uri);
        Log.i(TAG, " uri mimeTypeForFile ： " + mimeTypeForFile);
        if (!TextUtils.isEmpty(uri) && uri.startsWith("/content")) {
            uri = uri.substring(1, uri.length());
            Uri rUri = Uri.parse(uri);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                InputStream inputStream;
                try {
                    inputStream = Utils.getApplication().getContentResolver().openInputStream(rUri);
                } catch (Exception e) {
                    Log.w(TAG, e);
                    return getNotFoundResponse();
                }
                Log.i(TAG, " uri mode send stream " + uri);
                if (rUri.toString().contains("image")) {
                    mimeTypeForFile = "image/jpeg";
                } else if (rUri.toString().endsWith("mp4")) {
                    mimeTypeForFile = "video/mp4";
                }
                response = serveFileForStream(rUri, headers, inputStream, mimeTypeForFile);
                return response != null ? response : getNotFoundResponse(); // 开始返回response
            } else {
                uri = Utils.getFilePathByUri(Utils.getApplication(), rUri);
            }
        } else if (!TextUtils.isEmpty(uri) && (uri.contains("slog") || uri.contains("scacheLog"))) {
//            Log.flushMemoryLog();
//            if (uri.contains("slog")) {
//                uri = Preference.getInstance().get(LogWriterThread.KEY_LOG_PATH);
//            } else {
//                uri = Preference.getInstance().get(LogWriterThread.KEY_CACHE_LOG_PATH);
//            }
        }
        Log.i(TAG, " uri mode send stream " + uri);
        File file = new File(uri);
        Log.i(TAG, " serve response file: " + file);
        if (!file.exists()) {
            Log.i(TAG, " uri file.exists() " + file.exists());
            return getNotFoundResponse();
        }
        response = serveFile(uri, headers, file, mimeTypeForFile);
        return response != null ? response : getNotFoundResponse(); // 开始返回response
    }

    protected Response getNotFoundResponse() {
        return Response.newFixedLengthResponse(Status.NOT_FOUND,
                NanoHTTPD.MIME_PLAINTEXT, "Error 404, file not found.");
    }

    /**
     * 根据hearder获取对应的response
     *
     * @param uri
     * @param header
     * @param file
     * @param mime
     * @return
     */
    Response serveFile(String uri, Map<String, String> header, File file,
                       String mime) {
        Response res;
        try {
            // Calculate etag
            String etag = Integer.toHexString((file.getAbsolutePath()
                    + file.lastModified() + "" + file.length()).hashCode());
            Log.i(TAG, " serveFile etag： " + etag);
            // Support (simple) skipping:
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            Log.i(TAG, " serveFile range： " + range);
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range
                                    .substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (Exception ignored) {
                        Log.w(TAG, ignored);
                    }
                }
            }

            // get if-range header. If present, it must match etag or else we
            // should ignore the range request
            String ifRange = header.get("if-range");
            Log.i(TAG, " serveFile ifRange： " + ifRange);
            boolean headerIfRangeMissingOrMatching = (ifRange == null || etag
                    .equals(ifRange));
            Log.i(TAG, " serveFile headerIfRangeMissingOrMatching： " + headerIfRangeMissingOrMatching);
            String ifNoneMatch = header.get("if-none-match");
            Log.i(TAG, " serveFile ifNoneMatch： " + ifNoneMatch);
            boolean headerIfNoneMatchPresentAndMatching = ifNoneMatch != null
                    && ("*".equals(ifNoneMatch) || ifNoneMatch.equals(etag));

            // Change return code and add Content-Range header when skipping is
            // requested
            long fileLen = file.length();
            Log.i(TAG, " serveFile fileLen： " + fileLen);
            if (headerIfRangeMissingOrMatching && range != null
                    && startFrom >= 0 && startFrom < fileLen) {
                // range request that matches current etag
                // and the startFrom of the range is satisfiable
                if (headerIfNoneMatchPresentAndMatching) {
                    // range request that matches current etag
                    // and the startFrom of the range is satisfiable
                    // would return range from file
                    // respond with not-modified
                    res = newFixedLengthResponse(Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }

                    FileInputStream fis = new FileInputStream(file);
                    fis.skip(startFrom);

                    res = Response.newFixedLengthResponse(
                            Status.PARTIAL_CONTENT, mime, fis, newLen);
                    res.addHeader("Accept-Ranges", "bytes");
                    res.addHeader("Content-Length", "" + newLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-"
                            + endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            } else {

                if (headerIfRangeMissingOrMatching && range != null
                        && startFrom >= fileLen) {
                    // return the size of the file
                    // 4xx responses are not trumped by if-none-match
                    res = newFixedLengthResponse(Status.RANGE_NOT_SATISFIABLE,
                            NanoHTTPD.MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", "bytes */" + fileLen);
                    res.addHeader("ETag", etag);
                } else if (range == null && headerIfNoneMatchPresentAndMatching) {
                    // full-file-fetch request
                    // would return entire file
                    // respond with not-modified
                    res = newFixedLengthResponse(Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else if (!headerIfRangeMissingOrMatching
                        && headerIfNoneMatchPresentAndMatching) {
                    // range request that doesn't match current etag
                    // would return entire (different) file
                    // respond with not-modified

                    res = newFixedLengthResponse(Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else {
                    // supply the file
                    res = newFixedFileResponse(file, mime);// 此处开始设置 resepone 回复
                    res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
                    Log.i(TAG, " serveFile res： " + res);
                }
            }
        } catch (Exception ioe) {
            Log.w(TAG, ioe);
            res = getForbiddenResponse("Reading file failed.");
        }

        return res;
    }


    Response serveFileForStream(Uri uri, Map<String, String> header, InputStream is,
                                String mime) {
        Response res;
        try {
            // Calculate etag
//            String etag = Integer.toHexString((uri.toString()
//                    + "" + is.available()).hashCode());
            String etag = "";
            // Support (simple) skipping:
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range
                                    .substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (Exception ignored) {
                        Log.w(TAG, ignored);
                    }
                }
            }

            // get if-range header. If present, it must match etag or else we
            // should ignore the range request
            String ifRange = header.get("if-range");
            boolean headerIfRangeMissingOrMatching = (ifRange == null || etag
                    .equals(ifRange));

            String ifNoneMatch = header.get("if-none-match");
            boolean headerIfNoneMatchPresentAndMatching = ifNoneMatch != null
                    && ("*".equals(ifNoneMatch) || ifNoneMatch.equals(etag));
            headerIfNoneMatchPresentAndMatching = false;
            // Change return code and add Content-Range header when skipping is
            // requested
            long fileLen = is.available();
            if (headerIfRangeMissingOrMatching && range != null
                    && startFrom >= 0 && startFrom < fileLen) {
                // range request that matches current etag
                // and the startFrom of the range is satisfiable
                if (headerIfNoneMatchPresentAndMatching) {
                    // range request that matches current etag
                    // and the startFrom of the range is satisfiable
                    // would return range from file
                    // respond with not-modified
                    res = newFixedLengthResponse(Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }

                    //   FileInputStream fis = new FileInputStream(is);
                    is.skip(startFrom);

                    res = Response.newFixedLengthResponse(
                            Status.PARTIAL_CONTENT, mime, is, newLen);
                    res.addHeader("Accept-Ranges", "bytes");
                    res.addHeader("Content-Length", "" + newLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-"
                            + endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            } else {

                if (headerIfRangeMissingOrMatching && range != null
                        && startFrom >= fileLen) {
                    // return the size of the file
                    // 4xx responses are not trumped by if-none-match
                    res = newFixedLengthResponse(Status.RANGE_NOT_SATISFIABLE,
                            NanoHTTPD.MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", "bytes */" + fileLen);
                    res.addHeader("ETag", etag);
                } else if (range == null && headerIfNoneMatchPresentAndMatching) {
                    // full-file-fetch request
                    // would return entire file
                    // respond with not-modified
                    res = newFixedLengthResponse(Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else if (!headerIfRangeMissingOrMatching
                        && headerIfNoneMatchPresentAndMatching) {
                    // range request that doesn't match current etag
                    // would return entire (different) file
                    // respond with not-modified

                    res = newFixedLengthResponse(Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else {
                    // supply the file
//                    res = newFixedFileResponse(is, mime);// 此处开始设置 resepone 回复
                    res = Response.newFixedLengthResponse(Status.OK, mime,
                            is, is.available());
                    res.addHeader("Accept-Ranges", "bytes");
                    res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
                }
            }
        } catch (Exception ioe) {
            Log.w(TAG, ioe);
            res = getForbiddenResponse("Reading file failed.");
        }

        return res;
    }

    protected Response getForbiddenResponse(String s) {
        return Response.newFixedLengthResponse(Status.FORBIDDEN,
                NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: " + s);
    }

    protected Response getInternalErrorResponse(String s) {
        return Response.newFixedLengthResponse(Status.INTERNAL_ERROR,
                NanoHTTPD.MIME_PLAINTEXT, "INTERNAL ERROR: " + s);
    }

    private Response newFixedFileResponse(File file, String mime)
            throws FileNotFoundException {
        Response res;
        Log.i(TAG, " newFixedFileResponse file exists： " + file.exists());

        FileInputStream in = new FileInputStream(file);
        Log.i(TAG, " newFixedFileResponse file in： " + in);
        Log.i(TAG, " newFixedFileResponse file len： " + file.length());
        long len = file.length();
        Log.i(TAG, " newFixedFileResponse file len-： " + len);
        res = Response.newFixedLengthResponse(Status.OK, mime,in,len);
                //new FileInputStream(file), (int) file.length());
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    public static Response newFixedLengthResponse(IStatus status,
                                                  String mimeType, String message) {
        Response response = Response.newFixedLengthResponse(status, mimeType,
                message);
        response.addHeader("Accept-Ranges", "bytes");
        return response;
    }


}
