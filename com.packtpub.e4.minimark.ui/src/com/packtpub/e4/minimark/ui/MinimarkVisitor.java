/*
 * Copyright (c) 2016, Alex Blewitt, Bandlem Ltd
 * Copyright (c) 2016, Packt Publishing Ltd
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.packtpub.e4.minimark.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

public class MinimarkVisitor implements IResourceProxyVisitor, IResourceDeltaVisitor {
	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource resource = delta.getResource();
		if (resource.getName().endsWith(".minimark")) {
			processResource(resource);
		}
		return true;
	}

	public boolean visit(IResourceProxy proxy) throws CoreException {
		String name = proxy.getName();
		if (name != null && name.endsWith(".minimark")) {
			// System.out.println("Processing " + name);
			processResource(proxy.requestResource());
		}
		return true;
	}

	private void processResource(IResource resource) throws CoreException {
		if (resource instanceof IFile) {
			try {
				IFile file = (IFile) resource;
				InputStream in = file.getContents();
				String htmlName = file.getName().replace(".minimark", ".html");
				IContainer container = file.getParent();
				IFile htmlFile = container.getFile(new Path(htmlName));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				MinimarkTranslator.convert(new InputStreamReader(in), new OutputStreamWriter(baos));
				ByteArrayInputStream contents = new ByteArrayInputStream(baos.toByteArray());
				if (htmlFile.exists()) {
					htmlFile.setContents(contents, true, false, null);
				} else {
					htmlFile.create(contents, true, null);
				}
				htmlFile.setDerived(true, null);
			} catch (IOException e) {
				throw new CoreException(
						new Status(Status.ERROR, Activator.PLUGIN_ID, "Failed to generate resource", e));
			}
		}
	}
}
