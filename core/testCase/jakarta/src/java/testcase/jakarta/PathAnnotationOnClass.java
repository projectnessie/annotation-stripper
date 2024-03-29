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

import javax.validation.constraints.NotNull;
import javax.ws.rs.Path;
import org.projectnessie.model.Content;
import org.projectnessie.model.ContentResponse;
import org.projectnessie.model.Documentation;
import org.projectnessie.model.Reference;

@Path("/path")
@jakarta.ws.rs.Path("/path")
// Just implement an interface to get a dependency to another library to verify that the
// dependenciesClassLoader setting works.
public class PathAnnotationOnClass implements ContentResponse {

  @Override
  public @NotNull @jakarta.validation.constraints.NotNull Documentation getDocumentation() {
    return null;
  }

  @Override
  public @NotNull @jakarta.validation.constraints.NotNull Content getContent() {
    return null;
  }

  @Override
  public Reference getEffectiveReference() {
    return null;
  }
}
