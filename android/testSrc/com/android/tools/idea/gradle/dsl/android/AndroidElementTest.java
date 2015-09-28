/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.tools.idea.gradle.dsl.android;

import com.android.tools.idea.gradle.dsl.parser.GradleBuildModelParserTestCase;
import com.google.common.collect.ImmutableList;

/**
 * Tests for {@link AndroidElement}.
 */
public class AndroidElementTest extends GradleBuildModelParserTestCase {
  public void testAndroidBlockWithApplicationStatements() throws Exception {
    String text = "android { \n" +
                  "  buildToolsVersion \"23.0.0\"\n" +
                  "  compileSdkVersion 23\n" +
                  "  defaultPublishConfig \"debug\"\n" +
                  "  flavorDimensions \"abi\", \"version\"\n" +
                  "  generatePureSplits true\n" +
                  "  publishNonDefault false\n" +
                  "  resourcePrefix \"abcd\"\n" +
                  "}";

    writeToBuildFile(text);

    AndroidElement android = getGradleBuildModel().android();
    assertNotNull(android);

    assertEquals("buildToolsVersion", "23.0.0", android.buildToolsVersion());
    assertEquals("compileSdkVersion", "23", android.compileSdkVersion());
    assertEquals("defaultPublishConfig", "debug", android.defaultPublishConfig());
    assertEquals("flavorDimensions", ImmutableList.of("abi", "version"), android.flavorDimensions());
    assertEquals("generatePureSplits", Boolean.TRUE, android.generatePureSplits());
    assertEquals("publishNonDefault", Boolean.FALSE, android.publishNonDefault());
    assertEquals("resourcePrefix", "abcd", android.resourcePrefix());
  }

  public void testAndroidBlockWithAssignmentStatements() throws Exception {
    String text = "android { \n" +
                  "  buildToolsVersion = \"23.0.0\"\n" +
                  "  compileSdkVersion = \"android-23\"\n" +
                  "  defaultPublishConfig = \"debug\"\n" +
                  "  generatePureSplits = true\n" +
                  "}";

    writeToBuildFile(text);

    AndroidElement android = getGradleBuildModel().android();
    assertNotNull(android);

    assertEquals("buildToolsVersion", "23.0.0", android.buildToolsVersion());
    assertEquals("compileSdkVersion", "android-23", android.compileSdkVersion());
    assertEquals("defaultPublishConfig", "debug", android.defaultPublishConfig());
    assertEquals("generatePureSplits", Boolean.TRUE, android.generatePureSplits());
  }

  public void testAndroidApplicationStatements() throws Exception {
    String text = "android.buildToolsVersion \"23.0.0\"\n" +
                  "android.compileSdkVersion 23\n" +
                  "android.defaultPublishConfig \"debug\"\n" +
                  "android.flavorDimensions \"abi\", \"version\"\n" +
                  "android.generatePureSplits true\n" +
                  "android.publishNonDefault false\n" +
                  "android.resourcePrefix \"abcd\"";

    writeToBuildFile(text);

    AndroidElement android = getGradleBuildModel().android();
    assertNotNull(android);

    assertEquals("buildToolsVersion", "23.0.0", android.buildToolsVersion());
    assertEquals("compileSdkVersion", "23", android.compileSdkVersion());
    assertEquals("defaultPublishConfig", "debug", android.defaultPublishConfig());
    assertEquals("flavorDimensions", ImmutableList.of("abi", "version"), android.flavorDimensions());
    assertEquals("generatePureSplits", Boolean.TRUE, android.generatePureSplits());
    assertEquals("publishNonDefault", Boolean.FALSE, android.publishNonDefault());
    assertEquals("resourcePrefix", "abcd", android.resourcePrefix());
  }

  public void testAndroidAssignmentStatements() throws Exception {
    String text = "android.buildToolsVersion = \"23.0.0\"\n" +
                  "android.compileSdkVersion = \"android-23\"\n" +
                  "android.defaultPublishConfig = \"debug\"\n" +
                  "android.generatePureSplits = true";

    writeToBuildFile(text);

    AndroidElement android = getGradleBuildModel().android();
    assertNotNull(android);

    assertEquals("buildToolsVersion", "23.0.0", android.buildToolsVersion());
    assertEquals("compileSdkVersion", "android-23", android.compileSdkVersion());
    assertEquals("defaultPublishConfig", "debug", android.defaultPublishConfig());
    assertEquals("generatePureSplits", Boolean.TRUE, android.generatePureSplits());
  }

  public void testAndroidBlockWithOverrideStatements() throws Exception {
    String text = "android { \n" +
                  "  buildToolsVersion = \"23.0.0\"\n" +
                  "  compileSdkVersion 23\n" +
                  "  defaultPublishConfig \"debug\"\n" +
                  "  flavorDimensions \"abi\", \"version\"\n" +
                  "  generatePureSplits = true\n" +
                  "  publishNonDefault false\n" +
                  "  resourcePrefix \"abcd\"\n" +
                  "}\n" +
                  "android.buildToolsVersion \"21.0.0\"\n" +
                  "android.compileSdkVersion = \"android-21\"\n" +
                  "android.defaultPublishConfig \"release\"\n" +
                  "android.flavorDimensions \"abi1\", \"version1\"\n" +
                  "android.generatePureSplits = false\n" +
                  "android.publishNonDefault true\n" +
                  "android.resourcePrefix \"efgh\"";


    writeToBuildFile(text);

    AndroidElement android = getGradleBuildModel().android();
    assertNotNull(android);

    assertEquals("buildToolsVersion", "21.0.0", android.buildToolsVersion());
    assertEquals("compileSdkVersion", "android-21", android.compileSdkVersion());
    assertEquals("defaultPublishConfig", "release", android.defaultPublishConfig());
    assertEquals("flavorDimensions", ImmutableList.of("abi1", "version1"), android.flavorDimensions());
    assertEquals("generatePureSplits", Boolean.FALSE, android.generatePureSplits());
    assertEquals("publishNonDefault", Boolean.TRUE, android.publishNonDefault());
    assertEquals("resourcePrefix", "efgh", android.resourcePrefix());
  }
}