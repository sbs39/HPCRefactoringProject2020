/*******************************************************************************
 * Copyright (c) 2020 Sunna Berglind Sigurdardottir
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package is.hi.cs.hpcrefactoring.synctoasync;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.utils.NodeHelper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.text.edits.TextEditGroup;
import org.eclipse.cdt.internal.ui.refactoring.utils.SelectionHelper;

public class SyncToAsyncRefactoring extends CRefactoring {
	public IASTExpression selectedExpression;
	public IASTExpression target;
	private final SyncToAsyncInfo info;
	private String addStatusVariable;
	
	public SyncToAsyncRefactoring(ICElement element, ISelection selection, ICProject project) {
		super(element, selection, project);
		this.info = new SyncToAsyncInfo();
	}

	@Override
	protected RefactoringDescriptor getRefactoringDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
			throws CoreException, OperationCanceledException {
		gatherModifications(pm, collector);
	}
	
	public void setInfoName(String name) {
		info.setName(name);
	}
	
	public void gatherModifications(IProgressMonitor pm, ModificationCollector collector) throws CoreException {
		IASTTranslationUnit ast = target.getTranslationUnit();
		String constName = info.getName();
		
		IASTDeclaration nodes;
		ASTRewrite rewriter = collector.rewriterForTranslationUnit(ast);
		TextEditGroup editGroup = new TextEditGroup("SyncToAsync");
		
		ArrayList<String> MPIRequestNames = getMPIVariableNames("MPI_Request", false);
		if (!MPIRequestNames.contains(constName)) {
			nodes = createMPIVariableDeclaration("MPI_Request", constName);
			rewriter.insertBefore(findCurrentFunctionNode(target), findCurrentFunctionNode(target).getChildren()[0], nodes, editGroup);
		}
		
		IASTNode correctLocationNode = getCorrectParentNode();
		
		//Change send/recv to isend/irecv
		nodes = syncToAsync(constName);
		rewriter.insertBefore(correctLocationNode.getParent(), correctLocationNode, nodes, editGroup);
		rewriter.remove(correctLocationNode, editGroup);
		
		//Add MPI_Wait 
		nodes = createWaitString(constName, ast);
		IASTNode waitLocation = findWaitLocationNode(correctLocationNode, ast);	
		rewriter.insertBefore(correctLocationNode.getParent(), waitLocation, nodes, editGroup);
		
		if (addStatusVariable != null) {
			nodes = createMPIVariableDeclaration("MPI_Status", addStatusVariable);
			rewriter.insertBefore(findCurrentFunctionNode(target), findCurrentFunctionNode(target).getChildren()[0], nodes, editGroup);
		}
	}
	
	//Retrieve the node that includes the whole MPI_Send/MPI_Recv statement
	private IASTNode getCorrectParentNode() {
		IASTNode MPIExpression = target.getParent().getParent();
		IASTNode MPIExpressionParent;
		
		String MPIExpressionString = MPIExpression.getRawSignature();
		if (MPIExpressionString.contains("="))
			MPIExpressionParent = target.getParent().getParent().getParent();
		else
			MPIExpressionParent = target.getParent().getParent();
		
		return MPIExpressionParent;
	}
	
	private IASTNode findMsgBufferParameterNode() {
		IASTNode MPIExpression = target.getParent().getParent();
		IASTNode msgBufferParameterNode = null;
		
		IASTNode[] MPIExpressionChildren = MPIExpression.getChildren();
		if (MPIExpressionChildren.length > 0) {
			for(int i = 0; i < MPIExpressionChildren.length; i++) {
				if (MPIExpressionChildren[i].getRawSignature().contains("MPI_Send") || MPIExpressionChildren[i].getRawSignature().contains("MPI_Recv")) {
					MPIExpressionChildren = MPIExpressionChildren[i].getChildren();
				}
			}
		}		
		msgBufferParameterNode = MPIExpressionChildren[1];
		return msgBufferParameterNode;
	}
	
	//Find where to locate MPI_Wait according to usage of message buffer parameter
	private IASTNode findWaitLocationNode(IASTNode correctLocationNode, IASTTranslationUnit ast) {
		IASTNode blockItem = CPPVisitor.getContainingBlockItem(correctLocationNode);

		IASTNode[] blockItemChildren = blockItem.getChildren();
		IASTNode msgBufferParameterNode = findMsgBufferParameterNode();
		IASTNode waitLocation = null;
		IASTName[] names = findReferenceNamesInAST(msgBufferParameterNode.getRawSignature().replace("&", ""), ast);
		
		int startLooking = 0;
		for (int i = 0; i < blockItemChildren.length; i++)
		{
			if (blockItemChildren[i].equals(correctLocationNode)) {
				startLooking = i;
				break;
			}
		}
		
		outerloop:
		for (int i = startLooking+1; i < blockItemChildren.length; i++) {
			if (blockItemChildren[i].getRawSignature().contains(msgBufferParameterNode.getRawSignature().replace("&", ""))) {
				for (int j = 0; j < names.length; j++) {
					if(blockItemChildren[i].contains(names[j])) {
						waitLocation = blockItemChildren[i];
						break outerloop;
					}
				}	
			}
		}
	
		return waitLocation;
	}
	
	private IASTName[] findReferenceNamesInAST(String lookUpName, IASTTranslationUnit ast) {
		IScope targetScope = CVisitor.getContainingScope(target);
		IBinding[] binding = targetScope.find(lookUpName, ast);
		
		IASTName[] names = null;
		if (binding.length > 0)
			names = ast.getReferences(binding[0]);
		
		//Fallback - looks through all nodes of the correct length of the ast
		if(names == null) {
			IASTNodeSelector nodeSelector = ast.getNodeSelector(null);
			String astString = ast.getRawSignature();
			IASTNode selectedNode;
			ArrayList<IASTName> tempList = new ArrayList<IASTName>();
			for (int i = 0; i < astString.length(); i++) {
				selectedNode = nodeSelector.findNode(i, lookUpName.length());
				if (selectedNode != null) {
					if (selectedNode.getRawSignature().equals(lookUpName)) {
						tempList.add((IASTName) selectedNode);
					}
					selectedNode = null;
				}
			}
			if (tempList.size() > 0) {
				IASTName[] tempNames = new IASTName[tempList.size()];
				for (int j = 0; j < tempList.size(); j++) {
					tempNames[j] = tempList.get(j);
				}
				names = tempNames;
			}
		}
			
		return names;
	}
	
	//Modify string with MPI_Send/MPI_Recv to MPI_Isend/MPI_Irecv to later add to AST
	private IASTSimpleDeclaration syncToAsync(String MPIRequestString) throws CoreException {
		IASTNode MPIExpression = target.getParent().getParent();
		String MPIExpressionString = MPIExpression.getRawSignature();
		
		if (MPIExpressionString.contains("MPI_Recv")) {
			int indexBegin = MPIExpressionString.lastIndexOf(",");
			int indexEnd = MPIExpressionString.lastIndexOf(")");
			MPIExpressionString = MPIExpressionString.substring(0, indexBegin) + MPIExpressionString.substring(indexEnd);
		}
		String newMPIString = MPIExpressionString.replace("MPI_Send", "MPI_Isend").replace("MPI_Recv", "MPI_Irecv");
		if (MPIExpressionString.contains(";"))
			newMPIString = newMPIString.substring(0, newMPIString.length() - 2) + ", &" + MPIRequestString + newMPIString.substring(newMPIString.length()-2, newMPIString.length()-1);
		else
			newMPIString = newMPIString.substring(0, newMPIString.length() - 1) + ", &" + MPIRequestString + newMPIString.substring(newMPIString.length()-1);
		
		ICPPNodeFactory factory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
		IASTName RequestName = factory.newName(newMPIString.toCharArray());
		IASTDeclarator declarator = factory.newDeclarator(RequestName);
		IASTSimpleDeclSpecifier declSpec = factory.newSimpleDeclSpecifier();
		IASTSimpleDeclaration simple = factory.newSimpleDeclaration(declSpec);

		simple.addDeclarator(declarator);
		return simple;
	}
	
	private IASTSimpleDeclaration createWaitString(String MPIRequestString, IASTTranslationUnit ast) throws CoreException {
		String MPIStatusName = getMPIVariableNames("MPI_Status", false).get(0);
		
		String waitString = "MPI_Wait(&"+MPIRequestString+", &"+MPIStatusName+")";
		
		ICPPNodeFactory factory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
		IASTName WaitExpression = factory.newName(waitString.toCharArray());
		IASTDeclarator declarator = factory.newDeclarator(WaitExpression);
		IASTSimpleDeclSpecifier declSpec = factory.newSimpleDeclSpecifier();
		IASTSimpleDeclaration simple = factory.newSimpleDeclaration(declSpec);

		simple.addDeclarator(declarator);
		return simple;
	}
	
	//Find MPI_* variables within the current function of the selected statement
	private ArrayList<String> getMPIVariableNames(String variableType, boolean isInitialCheck) {
		String MPIVariableName = variableType.replace("MPI_", "").toLowerCase();
		
		IASTNode functionBody = findCurrentFunctionNode(target);
		IASTTranslationUnit ast = target.getTranslationUnit();

		IASTName[] nameRef = findReferenceNamesInAST(variableType, ast);
		ArrayList<String> variableNames = new ArrayList<String>();
		
		if (nameRef != null) {
			for (int i = 0; i < nameRef.length; i++) {
				if (functionBody.contains(nameRef[i])) {
					variableNames.add(nameRef[i].getParent().getParent().getChildren()[1].getRawSignature());
				}
			}
		}
		if (variableNames.size() == 0) {
			if (isInitialCheck)
				variableNames.add(MPIVariableName);
			if (variableType.equals("MPI_Status")) {
				variableNames.add(MPIVariableName);
				addStatusVariable = MPIVariableName;
			}
		}
		
		return variableNames;
	}
	
	private IASTNode findCurrentFunctionNode(IASTNode target) {
		IASTNode functionBody = target;
		
		while (!functionBody.getPropertyInParent().toString().contains("FUNCTION_BODY")) {
			functionBody = functionBody.getParent();
		}
		
		return functionBody;
	}
	
	private IASTSimpleDeclaration createMPIVariableDeclaration(String variableType, String newName) {
		//Declares a new MPI parameter to be used in the asynchronous call	
		String MPIVariable = variableType + " " + newName;
		
		ICPPNodeFactory factory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
		IASTName RequestName = factory.newName(MPIVariable.toCharArray());
		IASTDeclarator declarator = factory.newDeclarator(RequestName);
		IASTSimpleDeclSpecifier declSpec = factory.newSimpleDeclSpecifier();
		IASTSimpleDeclaration simple = factory.newSimpleDeclaration(declSpec);

		simple.addDeclarator(declarator);
		return simple;
	}
	
	public RefactoringStatus checkInitialConditions(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 12);
		
		RefactoringStatus status = super.checkInitialConditions(subMonitor.split(8));
		if (status.hasError()){
			return status;
		}
		
		if (selectedRegion == null){
			status.addFatalError("No selected region");
			return status;
		}
		
		IASTExpression selectedExpression = findSelectedExpression(subMonitor.split(1), tu);  
		String expressionString = ASTStringUtil.getExpressionString(selectedExpression);
		
		String parent = "";
		String propertyInParent = "";
		if(!expressionString.isEmpty()) {
			parent = selectedExpression.getParent().toString();
			propertyInParent = selectedExpression.getPropertyInParent().toString();
		}
		
		if (!parent.contains("FunctionCallExpression") || !propertyInParent.contains("IASTFunctionCallExpression.FUNCTION_NAME")) {
			status.addFatalError(Messages.NonMPISendRecvSelected);
			return status;
		}
		
		if (!expressionString.equals("MPI_Send") && !expressionString.equals("MPI_Recv")){
			status.addFatalError(Messages.NonMPISendRecvSelected);
			return status;
		}

		target = selectedExpression;

		ArrayList<String> MPIRequestStrings = getMPIVariableNames("MPI_Request", true);
		if (info.getName().isEmpty()) {
			info.setName(MPIRequestStrings.get(0));
		}

		info.setMethodContext(NodeHelper.findMethodContext(target, refactoringContext, subMonitor.split(1)));
		subMonitor.split(1);
		IScope containingScope = CVisitor.getContainingScope(target);
		IASTTranslationUnit ast = target.getTranslationUnit();
		info.setNameUsedChecker((String name) -> { 
			if (MPIRequestStrings.contains(name))
				return false;
			
			IBinding[] bindingsForName = containingScope.find(name, ast);
			return bindingsForName.length != 0;
		});
		return status;
	}
	
	public IASTExpression findSelectedExpression(IProgressMonitor monitor, ITranslationUnit tunit)
			throws OperationCanceledException, CoreException {
		IASTTranslationUnit ast = getAST(tunit, monitor);
		ast.accept(new ASTVisitor(true){
			@Override
			public int visit(IASTExpression expression) {
				if (SelectionHelper.nodeMatchesSelection(expression, selectedRegion)) {
					selectedExpression = expression;
					return PROCESS_ABORT;
				} else if (expression instanceof IASTLiteralExpression &&
						SelectionHelper.isSelectionInsideNode(expression, selectedRegion)) {
					selectedExpression = expression;
					return PROCESS_ABORT;
				}
				return super.visit(expression);
			}
		});
		return selectedExpression;
	}
	
	public IASTTranslationUnit getAST(ITranslationUnit tunit) throws Exception {
		return getAST(tunit, new NullProgressMonitor());
	}
	
	public SyncToAsyncInfo getRefactoringInfo() {
		return info;
	}
	
	
}