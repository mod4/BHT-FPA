package de.bht.fpa.icashbox.ui.rcp;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

  @Override
  public void createInitialLayout(IPageLayout layout) {
    layout.setEditorAreaVisible(false);
    layout.setFixed(true);

  }

}
