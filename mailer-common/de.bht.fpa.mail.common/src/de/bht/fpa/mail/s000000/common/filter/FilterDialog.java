package de.bht.fpa.mail.s000000.common.filter;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.bht.fpa.mail.s000000.common.SelectionHelper;
import de.bht.fpa.mail.s000000.common.filter.internal.entry.FilterEntryComponent;
import de.bht.fpa.mail.s000000.common.filter.internal.entry.IFilterEntryChangedListener;
import org.eclipse.swt.widgets.Group;

/**
 * This {@link Dialog} allows to create a filter combination. The user can
 * choose the grouping (union, intersection) and add an unlimited amount of
 * child filters. <br/>
 * A typical usage of this class is
 * 
 * <pre>
 * ...
 * Shell shell = ...
 * FilterDialog filterDialog = new FilterDialog(shell);
 * filterDialog.open(); // opens modal window
 * 
 * FilterGroupType filterGroupType = filterDialog.getFilterGroupType(); // the grouping type, union or intersection
 * List<FilterCombination> filterCombinations = filterDialog.getFilterCombinations(); // the list of child filters
 * for (FilterCombination filterCombination : filterCombinations) {
 *    FilterType filterType = filterCombination.getFilterType(); // sender, receiver, subject, ...
 *    FilterOperator filterOperator = filterCombination.getFilterOperator(); // contains, contains not, is, ...
 *    Object filterValue = filterCombination.getFilterValue();
 *  }
 * ...
 * </pre>
 * 
 * @author siamakhaschemi
 * 
 */
public final class FilterDialog extends Dialog {

  private static final int HEIGHT = 300;
  private static final int WIDTH = 600;
  private static final int NR_OF_COLUMNS = 3;

  private List<FilterCombination> filterCombinations;
  private FilterGroupType filterGroupType;

  private final List<FilterEntryComponent> filterEntryComponents = new LinkedList<FilterEntryComponent>();
  private final IFilterEntryChangedListener filterEntryChangedListener = new IFilterEntryChangedListener() {
    @Override
    public void onAddFilter(FilterEntryComponent filterEntryComponent) {
      int indexOf = filterEntryComponents.indexOf(filterEntryComponent);
      addNewFilterEntryAtIndex(indexOf);
      container.layout();
    }

    @Override
    public void onRemoveFilter(FilterEntryComponent filterEntryComponent) {
      filterEntryComponents.remove(filterEntryComponent);
      filterEntryComponent.dispose();
      container.layout();
    }
  };
  private Group filterEntriesGroup;
  private Composite container;

  /**
   * Create the dialog.
   * 
   * @param parentShell
   */
  public FilterDialog(Shell parentShell) {
    super(parentShell);
    setShellStyle(SWT.MAX | SWT.RESIZE | SWT.TITLE);
  }

  /**
   * Create contents of the dialog.
   * 
   * @param parent
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    container = (Composite) super.createDialogArea(parent);
    container.setLayout(new GridLayout(1, false));

    addUnionIntersection();
    addFilterEntryGroup();
    addNewFilterEntryAtIndex(0);
    return container;
  }

  private void addFilterEntryGroup() {

    filterEntriesGroup = new Group(container, SWT.NONE);
    GridLayout layout = new GridLayout(1, false);
    layout.marginHeight = 0;
    filterEntriesGroup.setLayout(layout);
    filterEntriesGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
  }

  private void addUnionIntersection() {
    Composite unionIntersectionComponent = new Composite(container, SWT.NONE);
    unionIntersectionComponent.setLayout(new GridLayout(NR_OF_COLUMNS, false));

    Label lblBeiErfllen = new Label(unionIntersectionComponent, SWT.NONE);
    lblBeiErfllen.setText("If");

    ComboViewer comboViewer = new ComboViewer(unionIntersectionComponent, SWT.READ_ONLY);
    comboViewer.setContentProvider(ArrayContentProvider.getInstance());
    comboViewer.setLabelProvider(new LabelProvider() {
      @Override
      public String getText(Object element) {
        return ((FilterGroupType) element).value();
      }
    });
    comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        filterGroupType = SelectionHelper.handleStructuredSelectionEvent(event, FilterGroupType.class);
      }
    });
    comboViewer.setInput(FilterGroupType.values());
    comboViewer.getCombo().select(0);
    filterGroupType = FilterGroupType.UNION;

    Label lblDerFolgendenBedingungen = new Label(unionIntersectionComponent, SWT.NONE);
    lblDerFolgendenBedingungen.setText("of the following conditions are met:");
  }

  private void addNewFilterEntryAtIndex(int index) {
    FilterEntryComponent filterEntryComponent = new FilterEntryComponent(filterEntriesGroup);
    filterEntryComponent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    filterEntryComponent.setFilterChangedListener(filterEntryChangedListener);
    filterEntryComponents.add(index, filterEntryComponent);
  }

  /**
   * Create contents of the button bar.
   * 
   * @param parent
   */
  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
  }

  /**
   * Return the initial size of the dialog.
   */
  @Override
  protected Point getInitialSize() {
    return new Point(WIDTH, HEIGHT);
  }

  @Override
  protected void okPressed() {
    filterCombinations = new LinkedList<FilterCombination>();
    for (FilterEntryComponent filterEntryComponent : filterEntryComponents) {
      filterCombinations.add(filterEntryComponent.getFilterCombination());
    }
    super.okPressed();
  }

  @Override
  protected void cancelPressed() {
    filterCombinations = new LinkedList<FilterCombination>();
    super.cancelPressed();
  }

  /**
   * Returns the result of this filter dialog, which is a list of filters
   * created by the user, or an empty list if user canceled the dialog ( see
   * {@link FilterDialog#cancelPressed()}).
   * 
   * @return list of {@link FilterCombination}s or an empty list if user
   *         canceled the dialog.
   */
  public List<FilterCombination> getFilterCombinations() {
    return filterCombinations;
  }

  /**
   * Returns the grouping type of the filter.
   * 
   * @return
   */
  public FilterGroupType getFilterGroupType() {
    return filterGroupType;
  }
}