package com.company.jifiit.Hotel;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class MultipartRequest extends Request<String> {

    private final Response.Listener<String> mListener;
    private final Map<String, String> mStringParams;
    private final Map<String, File> mFileParams;

    private static final String BOUNDARY = "----JifiitBoundary" + System.currentTimeMillis();

    public MultipartRequest(String url,
                            Map<String, String> stringParams,
                            Map<String, File> fileParams,
                            Response.Listener<String> listener,
                            Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        this.mListener = listener;
        this.mStringParams = stringParams;
        this.mFileParams = fileParams;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data; boundary=" + BOUNDARY;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            // ---- Text fields ----
            if (mStringParams != null) {
                for (Map.Entry<String, String> entry : mStringParams.entrySet()) {
                    bos.write(("--" + BOUNDARY + "\r\n").getBytes());
                    bos.write(("Content-Disposition: form-data; name=\""
                            + entry.getKey() + "\"\r\n\r\n").getBytes());
                    bos.write((entry.getValue() == null ? "" : entry.getValue()).getBytes("UTF-8"));
                    bos.write("\r\n".getBytes());
                }
            }

            // ---- Files ----
            if (mFileParams != null) {
                for (Map.Entry<String, File> entry : mFileParams.entrySet()) {
                    File file = entry.getValue();
                    if (file == null || !file.exists()) continue;

                    bos.write(("--" + BOUNDARY + "\r\n").getBytes());
                    bos.write(("Content-Disposition: form-data; name=\""
                            + entry.getKey() + "\"; filename=\""
                            + file.getName() + "\"\r\n").getBytes());
                    bos.write(("Content-Type: application/octet-stream\r\n\r\n").getBytes());

                    FileInputStream fis = new FileInputStream(file);
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = fis.read(buf)) != -1) {
                        bos.write(buf, 0, n);
                    }
                    fis.close();

                    bos.write("\r\n".getBytes());
                }
            }

            bos.write(("--" + BOUNDARY + "--\r\n").getBytes());
            return bos.toByteArray();

        } catch (IOException e) {
            VolleyLog.e("MultipartRequest getBody: " + e.getMessage());
            return null;
        }
    }
}