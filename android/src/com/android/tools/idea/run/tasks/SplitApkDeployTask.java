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
package com.android.tools.idea.run.tasks;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.tools.ir.client.InstantRunArtifact;
import com.android.tools.ir.client.InstantRunArtifactType;
import com.android.tools.ir.client.InstantRunBuildInfo;
import com.android.tools.idea.fd.DeployType;
import com.android.tools.idea.fd.InstantRunContext;
import com.android.tools.idea.fd.InstantRunManager;
import com.android.tools.idea.fd.InstantRunStatsService;
import com.android.tools.idea.run.ConsolePrinter;
import com.android.tools.idea.run.InstallResult;
import com.android.tools.idea.run.RetryingInstaller;
import com.android.tools.idea.run.util.LaunchStatus;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SplitApkDeployTask implements LaunchTask {

  private static final Pattern DEVICE_NOT_FOUND_ERROR = Pattern.compile("device '.*' not found");

  private final Project myProject;
  private final InstantRunContext myInstantRunContext;
  private final boolean myDontKill;

  public SplitApkDeployTask(Project project, InstantRunContext context) {
    this(project, context, false);
  }

  public SplitApkDeployTask(Project project, InstantRunContext context, boolean dontKill) {
    myProject = project;
    myInstantRunContext = context;
    myDontKill = dontKill;
  }

  @NotNull
  @Override
  public String getDescription() {
    return "Installing APKs";
  }

  @Override
  public int getDuration() {
    return LaunchTaskDurations.DEPLOY_APK;
  }

  @Override
  public boolean perform(@NotNull IDevice device, @NotNull LaunchStatus launchStatus, @NotNull ConsolePrinter printer) {
    InstantRunBuildInfo buildInfo = myInstantRunContext.getInstantRunBuildInfo();
    assert buildInfo != null;

    List<String> installOptions = Lists.newArrayList(); // TODO: should we pass in pm install options?
    installOptions.add("-t");

    // Embedded devices (Android Things) have all runtime permissions granted since there's no requirement for user interaction/display.
    // However, regular installation will not grant some permissions until the next device reboot. Installing with "-g" guarantees that
    // the permissions are properly granted at install time.
    if (device.supportsFeature(IDevice.HardwareFeature.EMBEDDED)) {
      installOptions.add("-g");
    }

    if (buildInfo.isPatchBuild()) {
      installOptions.add("-p"); // partial install
      installOptions.add(myInstantRunContext.getApplicationId());
    }

    if (myDontKill) {
      installOptions.add("--dont-kill");
    }

    List<InstantRunArtifact> artifacts = buildInfo.getArtifacts();
    List<File> apks = Lists.newArrayListWithExpectedSize(artifacts.size());
    for (InstantRunArtifact artifact : artifacts) {
      if (artifact.type == InstantRunArtifactType.SPLIT_MAIN || artifact.type == InstantRunArtifactType.SPLIT) {
        apks.add(artifact.file);
      }
    }

    RetryingInstaller.Installer installer = new SplitApkInstaller(printer, apks, installOptions);

    RetryingInstaller retryingInstaller =
      new RetryingInstaller(myProject, device, installer, myInstantRunContext.getApplicationId(), printer, launchStatus);
    boolean status = retryingInstaller.install();
    if (status) {
      printer.stdout("Split APKs installed");
    }

    assert myInstantRunContext.getBuildSelection() != null;
    InstantRunStatsService.get(myProject).notifyDeployType(DeployType.SPLITAPK, myInstantRunContext, device);

    return status;
  }

  private static final class SplitApkInstaller implements RetryingInstaller.Installer {
    private final ConsolePrinter myPrinter;
    private final List<File> myApks;
    private final List<String> myInstallOptions;

    public SplitApkInstaller(@NotNull ConsolePrinter printer, @NotNull List<File> apks, @NotNull List<String> installOptions) {
      myPrinter = printer;
      myApks = apks;
      myInstallOptions = installOptions;
    }

    @NotNull
    @Override
    public InstallResult installApp(@NotNull IDevice device, @NotNull LaunchStatus launchStatus) {
      String cmd = getAdbInstallCommand(myApks, myInstallOptions);

      try {
        myPrinter.stdout(cmd);
        InstantRunManager.LOG.info(cmd);

        device.installPackages(myApks, true, myInstallOptions, 5, TimeUnit.MINUTES);
        return new InstallResult(InstallResult.FailureCode.NO_ERROR, null, null);
      }
      catch (InstallException e) {
        InstallResult.FailureCode failureCode = InstallResult.FailureCode.UNTYPED_ERROR;
        // This can happen if the device gets disconnected during installation
        if (e.getMessage() != null && DEVICE_NOT_FOUND_ERROR.matcher(e.getMessage()).matches()) {
          failureCode = InstallResult.FailureCode.DEVICE_NOT_FOUND;
        }
        return new InstallResult(failureCode, e.getMessage(), null);
      }
    }

    @NotNull
    private static String getAdbInstallCommand(@NotNull List<File> apks, @NotNull List<String> installOptions) {
      StringBuilder sb = new StringBuilder();
      sb.append("$ adb install-multiple -r ");
      if (!installOptions.isEmpty()) {
        sb.append(Joiner.on(' ').join(installOptions));
        sb.append(' ');
      }

      for (File f : apks) {
        sb.append(f.getPath());
        sb.append(' ');
      }

      return sb.toString();
    }
  }
}
