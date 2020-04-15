/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * Copyright (c) 2020 Sunna Berglind Sigurdardottir
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/

package is.hi.cs.hpcrefactoring.synctoasync;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * The wizard page for SyncToAsync refactoring, creates the UI page.
 */
public class SyncToAsyncRefactoringWizard extends RefactoringWizard {
	//private final SyncToAsyncInfo info;
	
	public SyncToAsyncRefactoringWizard(final SyncToAsyncRefactoring refactoring) {
		super(refactoring, RefactoringWizard.DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
		//this.info = info;
	}

	@Override
	protected void addUserInputPages() {
		setDefaultPageTitle(getRefactoring().getName());
		addPage(new SyncToAsyncInputPage());
	}
}
