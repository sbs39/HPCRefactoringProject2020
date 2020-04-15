/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 * Copyright (c) 2020 Sunna Berglind Sigurdardottir
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package is.hi.cs.hpcrefactoring.synctoasync;

import org.eclipse.osgi.util.NLS;

final class Messages extends NLS {
	public static String NonFunctionSelected;
	public static String NonMPISendRecvSelected;
	public static String InputPage_NameAlreadyDefined;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
