/*******************************************************************************
 * Copyright (c) 2020 Sunna Berglind Sigurdardottir
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *******************************************************************************/

package unittest;

import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.changes.CCompositeChange;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.ui.ide.IDE;

import is.hi.cs.hpcrefactoring.synctoasync.SyncToAsyncRefactoring;

import java.nio.file.Files;
import java.nio.file.Paths;

public class SyncToAsyncRefactoringTests {
	public HPCRefactoringTestProject testProject;
	public IProject iproject;
	
	public void testSyncToAsyncRefactoring(String filePath, String projectName, String MPIType, 
			String requestString, String beforeFilePath, String afterFilePath, int selectedNodeOffset, 
			String errorMessage) throws Exception {
		
		//Initialize testproject
		testProject = new HPCRefactoringTestProject("/HPCRefactoringTests/src/unittest/", projectName);
		IPath path = new Path(filePath);	
		IFile file = testProject.project.getFile(path);
		IResource resource = testProject.project.findMember(path);
		iproject = resource.getProject();
		ICProject icProject = CoreModel.getDefault().create(iproject);
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart edPart = IDE.openEditor(page, file);
		ITranslationUnit tu = (ITranslationUnit) CDTUITools.getEditorInputCElement(edPart.getEditorInput());
		ICElement element = CoreModel.getDefault().create(path);

		//Initialize the refactoring and set necessary parameters
		SyncToAsyncRefactoring refactoring = new SyncToAsyncRefactoring(element, null, icProject);
		CRefactoringContext refactoringContext = new CRefactoringContext(refactoring);
		refactoring.setContext(refactoringContext);
		IASTTranslationUnit testast = refactoring.getAST(tu);
		refactoring.setInfoName(requestString);
		CheckConditionsContext context= new CheckConditionsContext();
		context.add(new ValidateEditChecker(refactoring.getValidationContext()));
		ResourceChangeChecker resourceChecker = new ResourceChangeChecker();
		IResourceChangeDescriptionFactory deltaFactory = resourceChecker.getDeltaFactory();
		context.add(resourceChecker);
		ModificationCollector modificationCollector = new ModificationCollector(deltaFactory);
		
		//Find "selected" node from offset and set as target 
		IASTNodeSelector nodeSelector = testast.getNodeSelector(null);
		IASTNode selectedNode = nodeSelector.findNode(selectedNodeOffset, 8);
		refactoring.target = (IASTExpression) selectedNode.getParent();
		
		//Collect and apply the changes
		refactoring.gatherModifications(new NullProgressMonitor(), modificationCollector);
		CCompositeChange change = modificationCollector.createFinalChange();
		change.perform(new NullProgressMonitor());
		
		//Get the before/after files from comparison
		String expected = new String (Files.readAllBytes(Paths.get(afterFilePath)));
		String result = new String (Files.readAllBytes(Paths.get(beforeFilePath)));

		//Compare result to expected file
		assertEquals(errorMessage, result, expected);
	}
}
