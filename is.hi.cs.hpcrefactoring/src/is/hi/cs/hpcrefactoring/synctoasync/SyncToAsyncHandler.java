package is.hi.cs.hpcrefactoring.synctoasync;

import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SyncToAsyncHandler extends AbstractHandler {
	protected CEditor fEditor;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor= CUIPlugin.getActivePage().getActiveEditor();
		fEditor = (CEditor) editor;
		IWorkingCopy wc= CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(fEditor.getEditorInput());
		ISelectionProvider provider= fEditor.getSelectionProvider();
		ISelection s = provider.getSelection();

		new SyncToAsyncAction().run(fEditor.getSite(), wc, (ITextSelection) s);
		return null;
	}
}
