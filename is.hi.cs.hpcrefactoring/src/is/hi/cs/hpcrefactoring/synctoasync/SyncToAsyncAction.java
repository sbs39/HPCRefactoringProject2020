/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * Copyright (c) 2020 Sunna Berglind Sigurdardottir
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Institute for Software (IFS)- initial API and implementation 
 *     Sergey Prigogin (Google)
 ******************************************************************************/

package is.hi.cs.hpcrefactoring.synctoasync;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.refactoring.actions.RefactoringAction;

/**
 * Launches an SyncToAsync refactoring.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */          
public class SyncToAsyncAction extends RefactoringAction implements IWorkbenchWindowActionDelegate {
    
    public SyncToAsyncAction() {
        super("SyncToAsyncRefactoring label");
    }
    
	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection selection) {
		if (wc.getResource() != null) {
			new SyncToAsyncRefactoringRunner(wc, selection, shellProvider, wc.getCProject()).run();
		}
	}

    @Override
	public void updateSelection(ICElement elem) {
    	super.updateSelection(elem);
    	setEnabled(false);
    }

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run(IAction action) {
		// TODO Auto-generated method stub
		IEditorPart editor= CUIPlugin.getActivePage().getActiveEditor();
		fEditor = (CEditor) editor;
		IWorkingCopy wc= CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(fEditor.getEditorInput());
		ISelectionProvider provider= fEditor.getSelectionProvider();
		ISelection s = provider.getSelection();

		new SyncToAsyncAction().run(fEditor.getSite(), wc, (ITextSelection) s);
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		
	}
}
