/*******************************************************************************
 * Copyright (c) 2020 Sunna Berglind Sigurdardottir
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *******************************************************************************/

package unittest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.osgi.framework.Bundle;
import org.junit.Assert;

public class HPCRefactoringTestProject {
	protected IWorkspaceRoot root;
	protected IProject project;
	protected String projectName;
	private String resourcePath;

	public HPCRefactoringTestProject(String resourcePath, String projectName) throws Exception {
		this.resourcePath = resourcePath;
		this.projectName = projectName;
		createWorkspace();
		fillWorkspace();
		
		project.open(new NullProgressMonitor());
		root.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
	}
	
	private void createWorkspace() throws WorkbenchException, CoreException, Exception {
        IWorkbenchPage[] pages = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getPages();

        for (int i = 0; i < pages.length; i++) {
            if (pages[i].getActivePart().getTitle().equals("Welcome"))
                pages[i].close();
        }
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getWorkbench()
               .showPerspective("org.eclipse.cdt.ui.CPerspective", PlatformUI.getWorkbench().getActiveWorkbenchWindow());

        IWorkspace ws = ResourcesPlugin.getWorkspace();
        Assert.assertNotNull("Workspace does not exist", ws);

        root = ws.getRoot();
        Assert.assertNotNull("Resource root does not exist", root);
        project = root.getProject(projectName);
        project.create(null);
        Assert.assertNotNull("Project does not exist", project);
        project.close(new NullProgressMonitor());
    }
	
	private void fillWorkspace() throws Exception {
		FileUtils.copyDirectory(new File("C:\\Users\\Sunna\\workspace_new_2019\\is.hi.cs.hpcrefactoringtests"), new File("C:\\Users\\Sunna\\junit-workspace\\" + projectName));
	}

	protected static String getPluginDir(String pluginId) throws IOException {
		Bundle bundle = Platform.getBundle(pluginId);
		Assert.assertNotNull("Could not resolve plugin", bundle);
		
		URL pluginURL = FileLocator.resolve(bundle.getEntry("/"));;
		String pluginInstallDir = pluginURL.getPath().trim();
		
		Assert.assertTrue("Could not get installation directory of plugin", pluginInstallDir.length() != 0);
		
		if (Platform.getOS().compareTo(Platform.OS_WIN32) == 0) {
			pluginInstallDir = pluginInstallDir.substring(1);
		}
		
		return pluginInstallDir;
	}
	
	public String getResourcePath() {
		return resourcePath;
	}
}
