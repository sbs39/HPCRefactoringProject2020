/*******************************************************************************
 * Copyright (c) 2020 Sunna Berglind Sigurdardottir
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *******************************************************************************/

package unittest;

import org.junit.After;
import org.junit.Test;

import unittest.SyncToAsyncRefactoringTests;

public class AllTests {
	SyncToAsyncRefactoringTests testRefactoring = new SyncToAsyncRefactoringTests();
	@Test
	public void testA() throws Exception {
		//Test A: MPI_Send using existing MPI_Request
		testRefactoring.testSyncToAsyncRefactoring("src/unittest/beforeWithRequest.c", "testprojectA", "MPI_Send", "request", 
				"C:\\Users\\Sunna\\junit-workspace\\testprojectA\\src\\unittest\\beforeWithRequest.c", 
				"C:\\Users\\Sunna\\junit-workspace\\testprojectA\\src\\unittest\\resultA.c", 505, "Change MPI_Send using existing MPI_Request failed.");
		
	}
	@Test
	public void testB() throws Exception {
		//Test B: MPI_Send using other than existing MPI_Request
		testRefactoring.testSyncToAsyncRefactoring("src/unittest/beforeWithRequest.c", "testprojectB", "MPI_Send", "newRequest", 
				"C:\\Users\\Sunna\\junit-workspace\\testprojectB\\src\\unittest\\beforeWithRequest.c", 
				"C:\\Users\\Sunna\\junit-workspace\\testprojectB\\src\\unittest\\resultB.c", 505, "Change MPI_Send with other than existing MPI_Request failed.");
		
	}
	@Test
	public void testC() throws Exception {
		//Test C: MPI_Send creating new MPI_Request when none exists
		testRefactoring.testSyncToAsyncRefactoring("src/unittest/beforeWithoutRequest.c", "testprojectC", "MPI_Send", "newRequest", 
				"C:\\Users\\Sunna\\junit-workspace\\testprojectC\\src\\unittest\\beforeWithoutRequest.c", 
				"C:\\Users\\Sunna\\junit-workspace\\testprojectC\\src\\unittest\\resultC.c", 482, "Change MPI_Send with new MPI_Request failed.");
		
	}
	@Test
	public void testD() throws Exception {
		//Test D: MPI_Recv using existing MPI_Request
		testRefactoring.testSyncToAsyncRefactoring("src/unittest/beforeWithRequest.c", "testprojectD", "MPI_Recv", "request", 
				"C:\\Users\\Sunna\\junit-workspace\\testprojectD\\src\\unittest\\beforeWithRequest.c", 
				"C:\\Users\\Sunna\\junit-workspace\\testprojectD\\src\\unittest\\resultD.c", 725, "Change MPI_Recv using existing MPI_Request failed.");
		
	}
	@Test
	public void testE() throws Exception {
		//Test E: MPI_Recv using other than existing MPI_Request
		testRefactoring.testSyncToAsyncRefactoring("src/unittest/beforeWithRequest.c", "testprojectE", "MPI_Recv", "newRequest", 
				"C:\\Users\\Sunna\\junit-workspace\\testprojectE\\src\\unittest\\beforeWithRequest.c", 
				"C:\\Users\\Sunna\\junit-workspace\\testprojectE\\src\\unittest\\resultE.c", 725, "Change MPI_Recv with other than existing MPI_Request failed.");
		
	}
	@Test
	public void testF() throws Exception {
		//Test F: MPI_Recv creating new MPI_Request when none exists
		testRefactoring.testSyncToAsyncRefactoring("src/unittest/beforeWithoutRequest.c", "testprojectF", "MPI_Recv", "newRequest", 
				"C:\\Users\\Sunna\\junit-workspace\\testprojectF\\src\\unittest\\beforeWithoutRequest.c", 
				"C:\\Users\\Sunna\\junit-workspace\\testprojectF\\src\\unittest\\resultF.c", 702, "Change MPI_Recv with new MPI_Request failed.");
		
	}
	
	
	@After
	public void tearDown() throws Exception {
		if (testRefactoring.testProject != null) {
			testRefactoring.testProject.project.delete(true, true, null);
		}
	}
}
