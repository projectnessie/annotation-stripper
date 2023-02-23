/*
 * Copyright (C) 2023 The Authors of this project
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

package testcase.jakarta;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/path")
@jakarta.ws.rs.Path("/path")
@SuppressWarnings("ClassCanBeStatic")
public class ComplexCase {

  @BeanParam
  @jakarta.ws.rs.BeanParam
  String beanParam;

  @GET
  @jakarta.ws.rs.GET
  public String foo(@PathParam("meep") @jakarta.ws.rs.PathParam("meep") String meep) {
    return "";
  }

  @Path("/inner")
  @jakarta.ws.rs.Path("/inner")
  class InnerClass {

    @BeanParam
    @jakarta.ws.rs.BeanParam
    String beanParam;

    @GET
    @jakarta.ws.rs.GET
    public String foo(@PathParam("meep") @jakarta.ws.rs.PathParam("meep") String meep) {
      return "";
    }

    @Path("/inner")
    @jakarta.ws.rs.Path("/inner")
    class InnerInnerClass {

      @BeanParam
      @jakarta.ws.rs.BeanParam
      String beanParam;

      @GET
      @jakarta.ws.rs.GET
      public String foo(@PathParam("meep") @jakarta.ws.rs.PathParam("meep") String meep) {
        return "";
      }
    }
  }

  @Path("/inner")
  @jakarta.ws.rs.Path("/inner")
  static class InnerStaticClass {

    @BeanParam
    @jakarta.ws.rs.BeanParam
    String beanParam;

    @GET
    @jakarta.ws.rs.GET
    public String foo(@PathParam("meep") @jakarta.ws.rs.PathParam("meep") String meep) {
      return "";
    }

    @Path("/inner")
    @jakarta.ws.rs.Path("/inner")
    static class InnerInnerStaticClass {

      @BeanParam
      @jakarta.ws.rs.BeanParam
      String beanParam;

      @GET
      @jakarta.ws.rs.GET
      public String foo(@PathParam("meep") @jakarta.ws.rs.PathParam("meep") String meep) {
        return "";
      }
    }
  }
}
