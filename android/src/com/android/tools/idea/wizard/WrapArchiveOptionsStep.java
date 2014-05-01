/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.android.tools.idea.wizard;

import com.android.tools.idea.gradle.util.GradleUtil;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBLabel;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.io.File;
import java.util.Set;

/**
 * Step for selecting archive to import and specifying Gradle subproject name.
 */
public final class WrapArchiveOptionsStep extends ModuleWizardStep implements AndroidStudioWizardStep {
  private static final Set<String> SUPPORTED_EXTENSIONS = ImmutableSet.of("jar", "aar");
  @Nullable private final Project myProject;
  private final TemplateWizardState myWizardState;
  private AsyncValidator<ValidationStatus> myValidator;
  private JPanel myPanel;
  private JTextField myGradlePath;
  private TextFieldWithBrowseButton myArchivePath;
  private JBLabel myValidationStatus;
  private ValidationStatus myResult = ValidationStatus.EMPTY_PATH;
  private boolean myIsFirstUIUpdate = true;

  public WrapArchiveOptionsStep(@Nullable Project project,
                                @NotNull TemplateWizardState state,
                                @NotNull Disposable parentDisposable) {
    centerControls();
    myProject = project;
    myWizardState = state;
    FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, true, false, false) {
      @Override
      public boolean isFileSelectable(VirtualFile file) {
        String extension = file.getExtension();
        return extension != null && SUPPORTED_EXTENSIONS.contains(extension.toLowerCase());
      }
    };
    myArchivePath.addBrowseFolderListener("Select Package", "Select jar or aar package to import as a new module", project, descriptor);
    myArchivePath.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        pathUpdated(myArchivePath.getText());
        updateDataModel();
      }
    });
    myGradlePath.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        updateDataModel();
      }
    });
    myValidationStatus.setText("");
    myValidationStatus.setIcon(null);
    myValidator = new AsyncValidator<ValidationStatus>(parentDisposable) {
      @Override
      protected void showValidationResult(ValidationStatus result) {
        updateStep(result);
      }

      @NotNull
      @Override
      protected ValidationStatus validate() {
        return validatePath();
      }
    };
    updateDataModel();
  }

  /**
   * This page is too empty, adding more whitespace to the top makes it look less desolate.
   */
  private void centerControls() {
    GridLayoutManager layout = (GridLayoutManager)myPanel.getLayout();
    int height = (int)Math.floor(myGradlePath.getPreferredSize().getHeight() * 3);
    layout.setMargin(new Insets(height, 16, 0, 0));
  }

  private void updateStep(@NotNull ValidationStatus result) {
    myResult = result;
    String message = "";
    Icon icon = null;
    if (!myIsFirstUIUpdate && (result.getMessageType() != MessageType.INFO)) {
      message = getValidationMessage(result);
      icon = result.getMessageType().getDefaultIcon();
    }
    myIsFirstUIUpdate = false;
    myValidationStatus.setText(message);
    myValidationStatus.setIcon(icon);

    fireStateChanged();
  }

  private String getValidationMessage(ValidationStatus result) {
    String gradlePath = myGradlePath.getText().trim();
    switch (result) {
      case MODULE_EXISTS:
        return String.format("Project already contains subproject with name %1$s", gradlePath);
      case OK:
        return "";
      case EMPTY_PATH:
        return "Archive file path is required";
      case EMPTY_GRADLE_PATH:
        return "Subproject name is required";
      case DOES_NOT_EXIST:
        return "File does not exist";
      case INVALID_MODULE_PATH:
        return String.format("Character %1$c is not allowed in Gradle path", gradlePath.charAt(GradleUtil.isValidGradlePath(gradlePath)));
      default:
        throw new IllegalStateException(result.name());
    }
  }

  @NotNull
  private ValidationStatus validatePath() {
    String archive = myArchivePath.getText().trim();
    if (Strings.isNullOrEmpty(archive)) {
      return ValidationStatus.EMPTY_PATH;
    }
    File archiveFile = new File(archive);
    if (!archiveFile.isFile()) {
      return ValidationStatus.DOES_NOT_EXIST;
    }
    String gradlePath = myGradlePath.getText().trim();
    if (Strings.isNullOrEmpty(gradlePath)) {
      return ValidationStatus.EMPTY_GRADLE_PATH;
    }
    if (GradleUtil.isValidGradlePath(gradlePath) >= 0) {
      return ValidationStatus.INVALID_MODULE_PATH;
    }
    if (GradleUtil.hasModule(myProject, gradlePath, true)) {
      return ValidationStatus.MODULE_EXISTS;
    }
    return ValidationStatus.OK;
  }

  private void pathUpdated(@NotNull String newPath) {
    myGradlePath.setText(Files.getNameWithoutExtension(newPath));
  }

  @Override
  public boolean validate() {
    return !MessageType.ERROR.equals(myResult.getMessageType());
  }

  @Override
  public JComponent getComponent() {
    return myPanel;
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myArchivePath.getTextField();
  }

  @Override
  public void updateDataModel() {
    myValidator.invalidate();
  }

  @Override
  public void updateStep() {
    String filePath = myArchivePath.getText().trim();
    String gradlePath = myGradlePath.getText().trim();

    myWizardState.put(WrapArchiveWizardPath.KEY_ARCHIVE, filePath);
    myWizardState.put(WrapArchiveWizardPath.KEY_GRADLE_PATH, gradlePath);
  }

  @Override
  public boolean isValid() {
    return validate();
  }

  private enum ValidationStatus {
    OK, EMPTY_PATH, DOES_NOT_EXIST, INVALID_MODULE_PATH, MODULE_EXISTS, EMPTY_GRADLE_PATH;

    public MessageType getMessageType() {
      switch (this) {
        case OK:
          return MessageType.INFO;
        default:
          return MessageType.ERROR;
      }
    }
  }

}
