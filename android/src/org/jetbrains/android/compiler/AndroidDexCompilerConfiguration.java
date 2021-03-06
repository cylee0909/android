/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
package org.jetbrains.android.compiler;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene.Kudelevsky
 */
@State(
  name = "AndroidDexCompilerConfiguration",
  storages = @Storage("androidDexCompiler.xml")
)
public class AndroidDexCompilerConfiguration implements PersistentStateComponent<AndroidDexCompilerConfiguration> {
  public String VM_OPTIONS = "";
  public int MAX_HEAP_SIZE = 700;
  public boolean OPTIMIZE = true;
  public boolean FORCE_JUMBO = false;
  public boolean CORE_LIBRARY = false;
  public String PROGUARD_VM_OPTIONS = "";

  @Override
  public AndroidDexCompilerConfiguration getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull AndroidDexCompilerConfiguration state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public static AndroidDexCompilerConfiguration getInstance(final Project project) {
    return ServiceManager.getService(project, AndroidDexCompilerConfiguration.class);
  }
}
