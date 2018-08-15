package com.prowiser.api.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface DownLoadService {

    String excel2Client(HttpServletRequest request, HttpServletResponse response, String sign);
}
