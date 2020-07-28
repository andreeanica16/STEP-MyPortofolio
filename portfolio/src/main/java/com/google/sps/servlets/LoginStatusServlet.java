// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import java.util.*;
import java.io.IOException;
import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.Comment;
import com.google.sps.data.DataSent;
import com.google.sps.data.UserInfo;

/**  Servlet responsible with giving information about the authentication status*/
@WebServlet("/loginStatus")
public class LoginStatusServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      UserService userService = UserServiceFactory.getUserService();
      boolean isUserLoggedIn = userService.isUserLoggedIn();

      UserInfo userLoginInfo = new UserInfo(isUserLoggedIn);

      if (!isUserLoggedIn) {
          String loginUrl = userService.createLoginURL("/index.html#submitComment");
          userLoginInfo.setLoginUrl(loginUrl);
      } else {
          String logoutUrl = userService.createLogoutURL("/index.html#submitComment");
          userLoginInfo.setLogoutUrl(logoutUrl);
      }

      Gson gson = new Gson();
      String json = gson.toJson(userLoginInfo);

      response.setContentType("application/json;");
      response.getWriter().println(json);
  }
}