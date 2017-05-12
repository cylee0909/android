/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.uibuilder.analytics;

import com.android.tools.idea.rendering.RenderResult;
import com.android.tools.idea.uibuilder.model.NlModel;
import com.google.wireless.android.sdk.stats.LayoutEditorEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for usage tracking in the layout editor. Not that implementations of these methods should aim to return immediately.
 */
public interface NlUsageTracker {
  /**
   * Logs a layout editor event in the usage tracker. Note that rendering actions should be logged through the
   * {@link #logRenderResult(NlModel.ChangeType, RenderResult, long)} method so it contains additional information about the render result.
   */
  void logAction(@NotNull LayoutEditorEvent.LayoutEditorEventType eventType);

  /**
   * Logs a render action.
   *
   * @param trigger The event that triggered the render action or null if not known.
   */
  void logRenderResult(@Nullable NlModel.ChangeType trigger, @NotNull RenderResult result, long totalRenderTimeMs);
}